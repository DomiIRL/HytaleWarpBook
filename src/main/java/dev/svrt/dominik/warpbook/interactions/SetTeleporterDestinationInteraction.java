package dev.svrt.dominik.warpbook.interactions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.svrt.dominik.warpbook.WarpBookMod;
import dev.svrt.dominik.warpbook.components.Teleporter;
import dev.svrt.dominik.warpbook.data.WarpPageBinding;
import dev.svrt.dominik.warpbook.ui.TeleporterConfirmationUI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class SetTeleporterDestinationInteraction extends SimpleBlockInteraction {

    public static final BuilderCodec<SetTeleporterDestinationInteraction> CODEC = BuilderCodec.builder(SetTeleporterDestinationInteraction.class, SetTeleporterDestinationInteraction::new, SimpleBlockInteraction.CODEC).build();

    @Nonnull
    @Override
    public WaitForDataFrom getWaitForDataFrom() {
        return WaitForDataFrom.Server;
    }

    @Override
    protected void interactWithBlock(@Nonnull World world, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nullable ItemStack itemInHand, @Nonnull Vector3i targetBlock, @Nonnull CooldownHandler cooldownHandler) {
        if (itemInHand == null) {
             context.getState().state = InteractionState.Failed;
             return;
        }

        WarpPageBinding binding = itemInHand.getFromMetadataOrNull(WarpPageBinding.KEYED_CODEC);
        if (binding == null) {
             context.getState().state = InteractionState.Failed;
             return;
        }

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
                         Ref<EntityStore> playerEntityRef = context.getEntity();
                         Player player = commandBuffer.getComponent(playerEntityRef, Player.getComponentType());
                         UUIDComponent uuidComponent = commandBuffer.getComponent(playerEntityRef, UUIDComponent.getComponentType());

                         if (player != null && uuidComponent != null) {
                              UUID playerUuid = uuidComponent.getUuid();
                              // Assuming Universe.get().getPlayerReference(uuid) is the way to get PlayerRef needed for UI
                              // If not, we might need to find another way.
                              // player.openCustomUI expects a CustomUIPage.
                              player.openCustomUI(new TeleporterConfirmationUI(Universe.get().getPlayerReference(playerUuid), blockRef, binding, context.getHeldItemSlot()));
                              context.getState().state = InteractionState.Success;
                              return;
                         }
                    }
                }
            }
        }

        context.getState().state = InteractionState.Failed;
    }
}

