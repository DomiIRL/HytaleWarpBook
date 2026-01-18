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

public class BindWarpPageUISupplier implements OpenCustomUIInteraction.CustomPageSupplier {

    public static final BuilderCodec<BindWarpPageUISupplier> CODEC =
        BuilderCodec.builder(BindWarpPageUISupplier.class, BindWarpPageUISupplier::new)
            .build();

    public BindWarpPageUISupplier() {
    }

    @Nonnull
    @Override
    public CustomUIPage tryCreate(
        Ref<EntityStore> ref,
        ComponentAccessor<EntityStore> componentAccessor,
        @Nonnull PlayerRef playerRef,
        InteractionContext context
    ) {
        return new BindWarpPageUI(playerRef, context);
    }
}

