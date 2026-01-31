package dev.svrt.dominik.warpbook.ui;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.svrt.dominik.warpbook.WarpBookMod;
import dev.svrt.dominik.warpbook.config.TeleportationPrice;
import dev.svrt.dominik.warpbook.data.WarpPageBinding;

import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.block.BlockModule.BlockStateInfo;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import dev.svrt.dominik.warpbook.components.WarpPageTeleporter;
import com.hypixel.hytale.server.core.ui.builder.EventData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WarpPageTeleporterUI extends InteractiveCustomUIPage<WarpPageTeleporterUI.BindWarpPortalEventData> {

    public String name;

    private final InteractionContext context;
    private final Ref<ChunkStore> blockRef;
    private final String activeState;

    public WarpPageTeleporterUI(PlayerRef playerRef, InteractionContext context, @Nonnull Ref<ChunkStore> blockRef, @Nullable String activeState) {
        super(playerRef, CustomPageLifetime.CanDismiss, BindWarpPortalEventData.CODEC);
        this.context = context;
        this.blockRef = blockRef;
        this.activeState = activeState;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commands,
                      @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {

        WarpPageBinding binding = getWarpPageBinding();

        if (binding == null) {
            commands.append("Pages/AWB_WarpPageTeleporterError.ui");
            commands.set("#UsageErrorTitle.Text", Message.translation("awb.customUI.warpPageTeleporter.needWarpPage"));
            commands.set("#UsageErrorLabel.Text", Message.translation("awb.customUI.warpPageTeleporter.needBoundWarpPage"));
            return;
        }

        if (binding.transform == null || binding.world == null) {
            commands.append("Pages/AWB_WarpPageTeleporterError.ui");
            commands.set("#UsageErrorTitle.Text", Message.translation("awb.customUI.warpPageTeleporter.needWarpPage"));
            commands.set("#UsageErrorLabel.Text", Message.translation("awb.customUI.warpPageTeleporter.needValidWarpPage"));
            return;
        }

        commands.append("Pages/AWB_WarpPageTeleporter.ui");
        commands.set("#BindButton.Text", Message.translation("awb.customUI.warpPageTeleporter.bind").param("name", binding.name));

        TeleportationPrice bindingPrice = WarpBookMod.get().getConfig().get().getTeleporterBindingPrice();
        boolean isFree = bindingPrice == null || bindingPrice.isFree();
        String costTranslationLabel = isFree ? "consumesPage" : "consumesPageCost";
        Message costMessage = Message.translation("awb.customUI.warpPageTeleporter." + costTranslationLabel);

        if (!isFree) {
            costMessage.param("amount", bindingPrice.getAmount());
            assert bindingPrice.getItemId() != null;
            Item item = Item.getAssetMap().getAsset(bindingPrice.getItemId());
            // For some reason translations dont work in params using workaround for now
            assert item != null;
            commands.set("#CostItem.Text", Message.translation(item.getTranslationKey()));
        }
        commands.set("#CostLabel.Text", costMessage);

        events.addEventBinding(CustomUIEventBindingType.Activating, "#BindButton", new EventData());
    }

    private WarpPageBinding getWarpPageBinding() {
        ItemContainer container = context.getHeldItemContainer();
        if (container == null) {
            return null;
        }
        ItemStack itemStack = container.getItemStack(context.getHeldItemSlot());
        if (itemStack == null) {
            return null;
        }
        return itemStack.getFromMetadataOrNull(WarpPageBinding.KEYED_CODEC);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
                                @Nonnull BindWarpPortalEventData data) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) {
            return;
        }

        WarpPageBinding binding = getWarpPageBinding();
        BlockStateInfo blockStateInfo = this.blockRef.getStore().getComponent(this.blockRef, BlockStateInfo.getComponentType());
        WarpPageTeleporter teleporter = this.blockRef.getStore().getComponent(this.blockRef, WarpPageTeleporter.getComponentType());

        if (binding == null || blockStateInfo == null || teleporter == null) {
            player.getPageManager().setPage(ref, store, Page.None);
            return;
        }

        // Payment
        if (!WarpBookMod.get().getPaymentService().processTeleporterBindingPayment(player)) {
            player.getPageManager().setPage(ref, store, Page.None);
            return;
        }

        // Consume item
        ItemContainer container = context.getHeldItemContainer();
        if (container != null) {
            ItemStack itemStack = container.getItemStack(context.getHeldItemSlot());
            if (itemStack != null) {
                container.removeItemStackFromSlot(context.getHeldItemSlot(), itemStack, 1);
            }
        }

        // Update component
        teleporter.setWarpPageBinding(binding);

        // Close page
        player.getPageManager().setPage(ref, store, Page.None);

        // Update block state
        updateBlockState(blockStateInfo);

        blockStateInfo.markNeedsSaving();
    }

    private void updateBlockState(BlockStateInfo blockStateInfo) {
        if (this.activeState == null) {
            return;
        }

        Ref<ChunkStore> chunkRef = blockStateInfo.getChunkRef();
        if (!chunkRef.isValid()) {
            return;
        }

        WorldChunk worldChunk = chunkRef.getStore().getComponent(chunkRef, WorldChunk.getComponentType());
        if (worldChunk == null) {
            return;
        }

        int index = blockStateInfo.getIndex();
        int targetX = ChunkUtil.xFromBlockInColumn(index);
        int targetY = ChunkUtil.yFromBlockInColumn(index);
        int targetZ = ChunkUtil.zFromBlockInColumn(index);

        BlockType blockType = worldChunk.getBlockType(targetX, targetY, targetZ);
        if (blockType == null) {
            return;
        }

        String currentState = blockType.getStateForBlock(blockType);
        if (currentState != null && currentState.equals(this.activeState)) {
            return;
        }

        BlockType variantBlockType = blockType.getBlockForState(this.activeState);
        if (variantBlockType != null) {
            worldChunk.setBlockInteractionState(targetX, targetY, targetZ, variantBlockType, this.activeState, true);
        }
    }

    public static class BindWarpPortalEventData {
        public static final BuilderCodec<BindWarpPortalEventData> CODEC =
          BuilderCodec.builder(BindWarpPortalEventData.class, BindWarpPortalEventData::new)
            .build();
    }
}
