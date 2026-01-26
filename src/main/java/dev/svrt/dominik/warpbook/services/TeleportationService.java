package dev.svrt.dominik.warpbook.services;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.svrt.dominik.warpbook.data.WarpPageBinding;
import dev.svrt.dominik.warpbook.entities.WarpPageTeleportation;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import static dev.svrt.dominik.warpbook.WarpBookMod.LOGGER;

public class TeleportationService {

    private final Map<UUID, WarpPageTeleportation> activeTeleports = new ConcurrentHashMap<>();

    public void shutdown() {
        activeTeleports.values().forEach(WarpPageTeleportation::cancel);
        activeTeleports.clear();
    }

    public WarpPageTeleportation getTeleportTask(UUID playerUUID) {
        return activeTeleports.get(playerUUID);
    }

    public void teleportPlayer(Ref<EntityStore> ref, Store<EntityStore> store, WarpPageBinding binding) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) {
            LOGGER.at(Level.WARNING).log("Failed to get player for teleportation!");
            return;
        }

        UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
        if (uuidComponent == null) {
            LOGGER.at(Level.WARNING).log("Failed to get UUID component for teleportation!");
            return;
        }

        UUID playerUUID = uuidComponent.getUuid();
        if (hasActiveTeleport(playerUUID)) {
            player.sendMessage(Message.raw("Another teleportation is already in progress!"));
            return;
        }

        if (!validateTeleportationRequest(player, binding)) {
            return;
        }

        WarpPageTeleportation teleportation = new WarpPageTeleportation(binding, ref, store);
        teleportation.start();
    }

    public void registerTeleportTask(UUID playerUUID, WarpPageTeleportation teleportation) {
        cancelTeleport(playerUUID);
        activeTeleports.put(playerUUID, teleportation);
    }

    public void removeTeleportTask(UUID playerUUID) {
        activeTeleports.remove(playerUUID);
    }

    public void cancelTeleport(UUID playerUUID) {
        WarpPageTeleportation teleportation = activeTeleports.remove(playerUUID);
        if (teleportation != null) {
            teleportation.cancel();
        }
    }

    public boolean hasActiveTeleport(UUID playerUUID) {
        return activeTeleports.containsKey(playerUUID);
    }

    public boolean validateTeleportationRequest(Player player, WarpPageBinding binding) {
        World currentWorld = player.getWorld();
        if (currentWorld == null) {
            LOGGER.at(Level.WARNING).log("Failed to get current world!");
            return false;
        }
        if (!currentWorld.getName().equals(binding.world)) { // Simplification: Accessing field directly if public
             player.sendMessage(Message.raw(String.format(
                "You are not in the correct world! (%s)", binding.world
            )));
            return false;
        }
        return true;
    }

    public CompletableFuture<WarpPageBinding> processRandomDestination(@Nonnull Player player, @Nonnull WarpPageBinding binding) {
        if (!binding.random || binding.transform != null) {
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

        Ref<EntityStore> reference = player.getReference();
        if (reference == null || !reference.isValid()) {
            LOGGER.at(Level.WARNING).log("Failed to get valid player reference for random destination!");
            return CompletableFuture.completedFuture(null);
        }

        TransformComponent transformComponent = reference.getStore().getComponent(reference, TransformComponent.getComponentType());
        if (transformComponent == null) {
            LOGGER.at(Level.WARNING).log("Failed to get TransformComponent for random destination!");
            return CompletableFuture.completedFuture(null);
        }

        binding.world = world.getName();

        // Generate random coordinates within the radius
        int radius = 5000;
        double startX = transformComponent.getPosition().x;
        double startZ = transformComponent.getPosition().z;

        double angle = Math.random() * 2 * Math.PI;
        double distance = Math.random() * radius;

        int destX = (int) Math.round(startX + distance * Math.cos(angle));
        int destZ = (int) Math.round(startZ + distance * Math.sin(angle));

        // Load chunk to get height (Chunk size 32 -> >> 5)
        return world.getChunkAsync(destX >> 5, destZ >> 5).thenApply(chunk -> {
            if (chunk == null) {
                LOGGER.at(Level.WARNING).log("Failed to load chunk for random destination!");
                return null;
            }

            int localX = Math.floorMod(destX, 32);
            int localZ = Math.floorMod(destZ, 32);
            short height = chunk.getHeight(localX, localZ);

            binding.transform = new Transform(new Vector3d(destX + 0.5, height + 1.0, destZ + 0.5));
            return binding;
        });
    }
}
