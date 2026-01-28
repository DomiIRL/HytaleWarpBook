package dev.svrt.dominik.warpbook.ui;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.svrt.dominik.warpbook.data.WarpPageBinding;

import javax.annotation.Nonnull;

public class WarpPageTeleporterUI extends InteractiveCustomUIPage<WarpPageTeleporterUI.BindWarpPortalEventData> {

    public String name;

    private InteractionContext context;

    public WarpPageTeleporterUI(PlayerRef playerRef, InteractionContext context) {
        super(playerRef, CustomPageLifetime.CanDismiss, BindWarpPortalEventData.CODEC);
        this.context = context;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commands,
                      @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {

        WarpPageBinding binding = getWarpPageBinding();

        if (binding == null) {
            // TODO: Show "requires a warp page ui"
            commands.append("Pages/AWB_WarpPageTeleporterError.ui");
            commands.set("#UsageErrorTitle.Text", Message.translation("awb.customUI.warpPageTeleporter.needWarpPage"));
            commands.set("#UsageErrorLabel.Text", Message.translation("awb.customUI.warpPageTeleporter.needBoundWarpPage"));
            return;
        }

        if (binding.transform == null || binding.world == null) {
            commands.append("Pages/AWB_WarpPageTeleporterError.ui");
            commands.set("#UsageErrorTitle.Text", Message.translation("awb.customUI.warpPageTeleporter.needWarpPage"));
            commands.set("#UsageErrorLabel.Text", Message.translation("awb.customUI.warpPageTeleporter.needValidWarpPage"));
            // TODO: Show "This warp pages destination is unknown"
            return;
        }

        commands.append("Pages/AWB_WarpPageTeleporter.ui");
        // TODO: Show "Do you want to bind this Warp Page to the Portal for eternity until overwritten?"
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

    }

    public static class BindWarpPortalEventData {
        public static final BuilderCodec<BindWarpPortalEventData> CODEC =
          BuilderCodec.builder(BindWarpPortalEventData.class, BindWarpPortalEventData::new)
            .build();
    }
}
