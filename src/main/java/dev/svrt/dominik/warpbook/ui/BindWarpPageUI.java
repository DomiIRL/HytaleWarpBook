package dev.svrt.dominik.warpbook.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.svrt.dominik.warpbook.common.WarpPageInteractions;

import javax.annotation.Nonnull;

public class BindWarpPageUI extends InteractiveCustomUIPage<BindWarpPageUI.BindWarpPageEventData> {

    public String name;

    private InteractionContext context;

    public BindWarpPageUI(PlayerRef playerRef, InteractionContext context) {
        super(playerRef, CustomPageLifetime.CanDismiss, BindWarpPageEventData.CODEC);
        this.context = context;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commands,
                      @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        commands.append("BindWarpPage.ui");

        events.addEventBinding(
          CustomUIEventBindingType.ValueChanged,
          "#Name",
          EventData.of("@Name", "#Name.Value"),
          false
        );

        events.addEventBinding(
          CustomUIEventBindingType.Activating,
          "#BindButton",
          EventData.of("Action", "confirm"),
          false
        );
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
                                @Nonnull BindWarpPageEventData data) {
        if (data.name == null) {
            boolean success = WarpPageInteractions.bindHeldWarpPage(ref, store, context, this.name);
            if (success) {
                close();
            }
        } else {
            this.name = data.name;
        }
    }

    public static class BindWarpPageEventData {
        public static final BuilderCodec<BindWarpPageEventData> CODEC =
          BuilderCodec.builder(BindWarpPageEventData.class, BindWarpPageEventData::new)
            .append(new KeyedCodec<>("@Name", Codec.STRING), (c, v) -> c.name = v, c -> c.name)
            .add()
            .build();

        public String name;
    }
}
