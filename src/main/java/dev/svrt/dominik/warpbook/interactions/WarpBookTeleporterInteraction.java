package dev.svrt.dominik.warpbook.interactions;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.PendingTeleport;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.svrt.dominik.warpbook.WarpBookMod;
import dev.svrt.dominik.warpbook.components.Teleporter;
import it.unimi.dsi.fastutil.objects.ObjectList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WarpBookTeleporterInteraction extends SimpleBlockInteraction {
    @Nonnull
    public static final BuilderCodec<WarpBookTeleporterInteraction> CODEC = BuilderCodec.builder(WarpBookTeleporterInteraction.class, WarpBookTeleporterInteraction::new, SimpleBlockInteraction.CODEC)
            .appendInherited(new KeyedCodec<>("Particle", Codec.STRING), (i, s) -> i.particle = s, i -> i.particle, (i, p) -> i.particle = p.particle)
            .build();

    @Nullable
    private String particle;

    @Nonnull
    @Override
    public WaitForDataFrom getWaitForDataFrom() {
        return WaitForDataFrom.Server;
    }

    @Override
    protected void interactWithBlock(@Nonnull World world, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nullable ItemStack itemInHand, @Nonnull Vector3i targetBlock, @Nonnull CooldownHandler cooldownHandler) {
        ChunkStore chunkStore = world.getChunkStore();
        long chunkIndex = ChunkUtil.indexChunkFromBlock(targetBlock.getX(), targetBlock.getZ());
        Ref<ChunkStore> chunkRef = chunkStore.getChunkReference(chunkIndex);
        if (chunkRef != null) {
            BlockComponentChunk blockComponentChunk = (BlockComponentChunk) chunkStore.getComponent(chunkRef, BlockComponentChunk.getComponentType());
            if (blockComponentChunk != null) {
                int blockIndex = ChunkUtil.indexBlockInColumn(targetBlock.x, targetBlock.y, targetBlock.z);
                Ref<ChunkStore> blockRef = blockComponentChunk.getEntityReference(blockIndex);
                if (blockRef != null && blockRef.isValid()) {
                    Teleporter teleporter = chunkStore.getStore().getComponent(blockRef, WarpBookMod.TELEPORTER_COMPONENT_TYPE);

                    if (teleporter != null) {
                        Ref<EntityStore> ref = context.getEntity();
                        Player playerComponent = commandBuffer.getComponent(ref, Player.getComponentType());
                        if (playerComponent != null && playerComponent.isWaitingForClientReady()) {
                            // Player is not ready (loading screen etc), assume skip?
                            // Logic from provided code says: if (playerComponent == null || !playerComponent.isWaitingForClientReady()) { ... }
                            // So if player IS waiting, we skip.
                            return;
                        }

                         Archetype<EntityStore> archetype = commandBuffer.getArchetype(ref);
                        if (!archetype.contains(Teleport.getComponentType()) && !archetype.contains(PendingTeleport.getComponentType())) {
                             TransformComponent transformComponent = commandBuffer.getComponent(ref, TransformComponent.getComponentType());
                             if (transformComponent != null) {
                                  Teleport teleportComponent = teleporter.toTeleport(transformComponent.getPosition(), transformComponent.getRotation(), targetBlock);
                                    if (teleportComponent != null) {
                                        commandBuffer.addComponent(ref, Teleport.getComponentType(), teleportComponent);
                                        if (this.particle != null) {
                                            Vector3d particlePosition = transformComponent.getPosition();
                                            SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource = (SpatialResource)commandBuffer.getResource(EntityModule.get().getPlayerSpatialResourceType());
                                            ObjectList<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
                                            playerSpatialResource.getSpatialStructure().collect(particlePosition, (double)75.0F, results);
                                            ParticleUtil.spawnParticleEffect(this.particle, particlePosition, results, commandBuffer);
                                        }

                                    }
                             }
                        }
                    }
                }
            }
        }
    }
}

