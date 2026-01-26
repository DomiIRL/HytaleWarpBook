package dev.svrt.dominik.warpbook.services;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.svrt.dominik.warpbook.data.WarpPageBinding;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import static dev.svrt.dominik.warpbook.WarpBookMod.LOGGER;

public class RandomDestinationService {

    private final Map<UUID, CompletableFuture<WarpPageBinding>> activeProcesses = new ConcurrentHashMap<>();

    public boolean hasActiveProcess(UUID playerUUID) {
        return activeProcesses.containsKey(playerUUID);
    }

    public CompletableFuture<WarpPageBinding> processRandomDestination(@Nonnull Player player, @Nonnull WarpPageBinding binding) {
        if (!binding.random || binding.transform != null) {
            return CompletableFuture.completedFuture(null);
        }

        Ref<EntityStore> reference = player.getReference();
        if (reference == null || !reference.isValid()) {
            LOGGER.at(Level.WARNING).log("Failed to get valid player reference for random destination!");
            return CompletableFuture.completedFuture(null);
        }

        UUIDComponent uuidComponent = reference.getStore().getComponent(reference, UUIDComponent.getComponentType());
        if (uuidComponent == null) {
            LOGGER.at(Level.WARNING).log("Failed to get UUID component for random destination!");
            return CompletableFuture.completedFuture(null);
        }

        UUID playerUUID = uuidComponent.getUuid();
        if (hasActiveProcess(playerUUID)) {
            player.sendMessage(Message.raw("A random destination is already being calculated!"));
            return CompletableFuture.completedFuture(null);
        }

        World world = player.getWorld();
        if (world == null) {
            LOGGER.at(Level.WARNING).log("Failed to get player world for random destination!");
            return CompletableFuture.completedFuture(null);
        }

        if (world.getName().startsWith("instance-")) {
            player.sendMessage(Message.raw("Return to a permanent world to use this."));
            return CompletableFuture.completedFuture(null);
        }

        TransformComponent transformComponent = reference.getStore().getComponent(reference, TransformComponent.getComponentType());
        if (transformComponent == null) {
            LOGGER.at(Level.WARNING).log("Failed to get TransformComponent for random destination!");
            return CompletableFuture.completedFuture(null);
        }

        binding.world = world.getName();

        int minRadius = 750;
        int radius = 3000;
        double startX = transformComponent.getPosition().x;
        double startZ = transformComponent.getPosition().z;

        CompletableFuture<WarpPageBinding> process = searchSafeDestination(world, binding, startX, startZ, minRadius, radius);
        activeProcesses.put(playerUUID, process);

        // Clean up from tracking map when complete
        process.whenComplete((result, throwable) -> activeProcesses.remove(playerUUID));

        return process;
    }

    private CompletableFuture<WarpPageBinding> searchSafeDestination(World world, WarpPageBinding binding, double startX, double startZ, int minRadius, int radius) {
        CompletableFuture<WarpPageBinding> resultFuture = new CompletableFuture<>();
        int parallelAttempts = 10;
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < parallelAttempts; i++) {
            futures.add(tryFindParams(world, binding, startX, startZ, minRadius, radius, resultFuture));
        }

        // Complete with null if all attempts fail
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenRun(() -> {
                if (!resultFuture.isDone()) {
                    LOGGER.at(Level.WARNING).log("Failed to find safe random destination after parallel attempts.");
                    resultFuture.complete(null);
                }
            });

        // Return immediately - will complete as soon as first success is found via resultFuture.complete() in tryFindParams
        return resultFuture;
    }

    private CompletableFuture<Void> tryFindParams(World world, WarpPageBinding binding, double startX, double startZ, int minRadius, int radius, CompletableFuture<WarpPageBinding> resultFuture) {
        if (resultFuture.isDone()) return CompletableFuture.completedFuture(null);

        double angle = Math.random() * 2 * Math.PI;
        double distance = minRadius + Math.random() * (radius - minRadius);
        int initialDestX = (int) Math.round(startX + distance * Math.cos(angle));
        int initialDestZ = (int) Math.round(startZ + distance * Math.sin(angle));

        return world.getChunkAsync(initialDestX, initialDestZ).thenCompose(chunk -> {
            if (resultFuture.isDone() || chunk == null) return CompletableFuture.completedFuture(null);

            CompletableFuture<Void> checkTask = new CompletableFuture<>();
            world.execute(() -> {
                try {
                    if (resultFuture.isDone()) return;

                    // Try multiple positions within the same loaded chunk
                    for (int attempt = 0; attempt < 5; attempt++) {
                        if (resultFuture.isDone()) break;

                        int destX, destZ;
                        if (attempt == 0) {
                            destX = initialDestX;
                            destZ = initialDestZ;
                        } else {
                            // Pick a new random position within the same chunk (0-31)
                            int chunkX = ChunkUtil.chunkCoordinate(initialDestX);
                            int chunkZ = ChunkUtil.chunkCoordinate(initialDestZ);
                            int localX = (int) (Math.random() * ChunkUtil.SIZE);
                            int localZ = (int) (Math.random() * ChunkUtil.SIZE);
                            destX = ChunkUtil.worldCoordFromLocalCoord(chunkX, localX);
                            destZ = ChunkUtil.worldCoordFromLocalCoord(chunkZ, localZ);
                        }

                        int localX = ChunkUtil.localCoordinate(destX);
                        int localZ = ChunkUtil.localCoordinate(destZ);
                        int maxY = chunk.getHeight(localX, localZ);

                        int fluidAtMax = world.getFluidId(destX, maxY, destZ);
                        int fluidAbove = world.getFluidId(destX, maxY + 1, destZ);

                        // Check for water (either at height or directly above ground)
                        if (fluidAtMax != 0 || fluidAbove != 0) {
                            // Water Logic: Find top of water
                            int waterY = fluidAtMax != 0 ? maxY : maxY + 1;
                            while (waterY < 255) {
                                if (world.getFluidId(destX, waterY + 1, destZ) == 0) break;
                                waterY++;
                            }

                            // Check headspace above water
                            if (isHeadSpaceClear(world, destX, waterY, destZ)) {
                                // "Don't add 1 block on y there so the player stands halfway in the water"
                                binding.transform = new Transform(new Vector3d(destX + 0.5, waterY + 0.5, destZ + 0.5));
                                resultFuture.complete(binding);
                                return;
                            }
                        } else {
                            // Ground Logic: Scan downwards for valid spot (handling trees)
                            Integer bestY = null;
                            // Scan down 15 blocks to find ground below trees
                            for (int y = maxY; y >= Math.max(0, maxY - 15); y--) {
                                int block = world.getBlock(destX, y, destZ);
                                int fluid = world.getFluidId(destX, y, destZ);

                                if (block != 0 && fluid == 0) {
                                    if (isHeadSpaceClear(world, destX, y, destZ)) {
                                        bestY = y;
                                        // Continue scanning to find the LOWEST valid spot
                                    }
                                }
                            }

                            if (bestY != null) {
                                binding.transform = new Transform(new Vector3d(destX + 0.5, bestY + 1.0, destZ + 0.5));
                                resultFuture.complete(binding);
                                return;
                            }
                        }
                    }
                } finally {
                    checkTask.complete(null);
                }
            });
            return checkTask;
        });
    }

    private boolean isHeadSpaceClear(World world, int x, int y, int z) {
        return world.getBlock(x, y + 1, z) == 0 && world.getFluidId(x, y + 1, z) == 0 &&
               world.getBlock(x, y + 2, z) == 0 && world.getFluidId(x, y + 2, z) == 0;
    }
}
