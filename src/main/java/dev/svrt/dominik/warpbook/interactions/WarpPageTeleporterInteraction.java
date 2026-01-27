package dev.svrt.dominik.warpbook.interactions;

import com.hypixel.hytale.builtin.adventure.teleporter.interaction.server.TeleporterInteraction;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;

public class WarpPageTeleporterInteraction extends SimpleBlockInteraction {

  public static final BuilderCodec<TeleporterInteraction> CODEC =
    BuilderCodec.builder(
        TeleporterInteraction.class,
        TeleporterInteraction::new,
        SimpleBlockInteraction.CODEC
      )
      .build();
  private static final Duration TELEPORT_GLOBAL_COOLDOWN = Duration.ofMillis(250L);

  @Override
  protected void interactWithBlock(@Nonnull World world, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull InteractionType interactionType, @Nonnull InteractionContext interactionContext, @Nullable ItemStack itemStack, @Nonnull Vector3i vector3i, @Nonnull CooldownHandler cooldownHandler) {

  }

  @Override
  protected void simulateInteractWithBlock(@Nonnull InteractionType interactionType, @Nonnull InteractionContext interactionContext, @Nullable ItemStack itemStack, @Nonnull World world, @Nonnull Vector3i vector3i) {

  }

  @Nonnull
  public WaitForDataFrom getWaitForDataFrom() {
    return WaitForDataFrom.Server;
  }
}
