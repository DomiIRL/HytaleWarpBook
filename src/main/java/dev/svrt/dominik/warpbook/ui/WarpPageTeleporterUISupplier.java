package dev.svrt.dominik.warpbook.ui;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nullable;

public class WarpPageTeleporterUISupplier implements OpenCustomUIInteraction.CustomPageSupplier {

  public static final BuilderCodec<WarpPageTeleporterUISupplier> CODEC =
    BuilderCodec.builder(WarpPageTeleporterUISupplier.class, WarpPageTeleporterUISupplier::new)
      .build();

  @Nullable
  @Override
  public CustomUIPage tryCreate(Ref<EntityStore> ref, ComponentAccessor<EntityStore> componentAccessor, PlayerRef playerRef, InteractionContext interactionContext) {
    return new WarpPageTeleporterUI(playerRef, interactionContext);
  }
}
