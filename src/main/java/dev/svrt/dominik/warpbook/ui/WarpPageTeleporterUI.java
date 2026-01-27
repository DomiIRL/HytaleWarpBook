package dev.svrt.dominik.warpbook.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.svrt.dominik.warpbook.WarpBookMod;
import dev.svrt.dominik.warpbook.services.WarpPageBindingService;

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
