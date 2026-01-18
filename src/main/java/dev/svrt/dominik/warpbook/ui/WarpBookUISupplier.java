package dev.svrt.dominik.warpbook.ui;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public class WarpBookUISupplier implements OpenCustomUIInteraction.CustomPageSupplier {

    public static final BuilderCodec<WarpBookUISupplier> CODEC =
        BuilderCodec.builder(WarpBookUISupplier.class, WarpBookUISupplier::new)
            .build();

    public WarpBookUISupplier() {
    }

    @Nonnull
    @Override
    public CustomUIPage tryCreate(
        Ref<EntityStore> ref,
        ComponentAccessor<EntityStore> componentAccessor,
        @Nonnull PlayerRef playerRef,
        InteractionContext context
    ) {
        return new WarpBookUI(playerRef, context);
    }
}

