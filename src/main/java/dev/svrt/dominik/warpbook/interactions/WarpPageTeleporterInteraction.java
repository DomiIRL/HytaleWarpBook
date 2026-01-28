package dev.svrt.dominik.warpbook.interactions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.block.BlockModule.BlockStateInfo;
import com.hypixel.hytale.server.core.modules.entity.teleport.PendingTeleport;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.entity.teleport.TeleportRecord;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.svrt.dominik.warpbook.WarpBookMod;
import dev.svrt.dominik.warpbook.components.WarpPageTeleporter;
import dev.svrt.dominik.warpbook.data.WarpPageBinding;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;

public class WarpPageTeleporterInteraction extends SimpleBlockInteraction {

    public static final BuilderCodec<WarpPageTeleporterInteraction> CODEC =
      BuilderCodec.builder(
          WarpPageTeleporterInteraction.class,
          WarpPageTeleporterInteraction::new,
          SimpleBlockInteraction.CODEC
        )
        .build();

    private static final Duration TELEPORT_GLOBAL_COOLDOWN = Duration.ofMillis(250L);

    @Override
    protected void interactWithBlock(@Nonnull World world, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull InteractionType interactionType, @Nonnull InteractionContext context, @Nullable ItemStack itemStack, @Nonnull Vector3i targetBlock, @Nonnull CooldownHandler cooldownHandler) {
        ChunkStore chunkStore = world.getChunkStore();
        long chunkIndex = ChunkUtil.indexChunkFromBlock(targetBlock.getX(), targetBlock.getZ());
        BlockComponentChunk blockComponentChunk = chunkStore.getChunkComponent(chunkIndex, BlockComponentChunk.getComponentType());

        if (blockComponentChunk == null) {
            return;
        }

        int blockIndex = ChunkUtil.indexBlockInColumn(targetBlock.x, targetBlock.y, targetBlock.z);
        Ref<ChunkStore> blockRef = blockComponentChunk.getEntityReference(blockIndex);

        if (blockRef == null || !blockRef.isValid()) {
            return;
        }

        BlockStateInfo blockStateInfoComponent = blockRef.getStore().getComponent(blockRef, BlockStateInfo.getComponentType());
        if (blockStateInfoComponent == null) {
            return;
        }

        WarpPageTeleporter teleporter = blockRef.getStore().getComponent(blockRef, WarpPageTeleporter.getComponentType());
        if (teleporter == null) {
            return;
        }

        WarpPageBinding binding = teleporter.getWarpPageBinding();
        if (binding == null || binding.transform == null) {
            return;
        }

        Ref<EntityStore> entityRef = context.getEntity();
        Player player = commandBuffer.getComponent(entityRef, Player.getComponentType());

        if (player == null || player.isWaitingForClientReady()) {
            return;
        }

        Archetype<EntityStore> archetype = commandBuffer.getArchetype(entityRef);
        if (archetype.contains(Teleport.getComponentType()) || archetype.contains(PendingTeleport.getComponentType())) {
            return;
        }

        TeleportRecord recorder = commandBuffer.getComponent(entityRef, TeleportRecord.getComponentType());
        if (recorder != null && !recorder.hasElapsedSinceLastTeleport(TELEPORT_GLOBAL_COOLDOWN)) {
            return;
        }

        if (!WarpBookMod.get().getTeleportationService().validateTeleportationRequest(player, binding)) {
            return;
        }

        world.execute(() -> {
            if (entityRef.isValid()) {
                Store<EntityStore> store = entityRef.getStore();
                store.addComponent(entityRef, Teleport.getComponentType(), Teleport.createForPlayer(world, binding.transform));
            }
        });
    }

    @Override
    protected void simulateInteractWithBlock(@Nonnull InteractionType interactionType, @Nonnull InteractionContext interactionContext, @Nullable ItemStack itemStack, @Nonnull World world, @Nonnull Vector3i vector3i) {

    }

    @Nonnull
    public WaitForDataFrom getWaitForDataFrom() {
        return WaitForDataFrom.Server;
    }
}
