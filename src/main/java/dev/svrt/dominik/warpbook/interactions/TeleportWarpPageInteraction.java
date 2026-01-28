package dev.svrt.dominik.warpbook.interactions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.svrt.dominik.warpbook.WarpBookMod;
import dev.svrt.dominik.warpbook.services.WarpPageUsageService;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;

public class TeleportWarpPageInteraction extends SimpleInstantInteraction {

    public static final BuilderCodec<TeleportWarpPageInteraction> CODEC =
      BuilderCodec.builder(
          TeleportWarpPageInteraction.class,
          TeleportWarpPageInteraction::new,
          SimpleInstantInteraction.CODEC
        )
        .build();

    protected TeleportWarpPageInteraction() {
        super();
    }

    @Nonnull
    @Override
    public WaitForDataFrom getWaitForDataFrom() {
        return WaitForDataFrom.Server;
    }

    @Override
    protected void firstRun(
      @Nonnull InteractionType type,
      @Nonnull InteractionContext context,
      @Nonnull CooldownHandler cooldownHandler
    ) {
        Ref<EntityStore> entityRef = context.getEntity();
        Player player = entityRef.getStore().getComponent(entityRef, Player.getComponentType());
        if (player == null) {
            context.getState().state = InteractionState.Failed;
            return;
        }

        ItemStack heldItem = context.getHeldItem();
        if (heldItem == null) {
            context.getState().state = InteractionState.Failed;
            return;
        }

        if (context.getHeldItemContainer() == null) {
            context.getState().state = InteractionState.Failed;
            return;
        }

        WarpPageUsageService usageService = WarpBookMod.getInstance().getWarpPageUsageService();
        boolean success = usageService.startTeleportPlayer(entityRef, entityRef.getStore(), heldItem, context.getHeldItemContainer(), context.getHeldItemSlot());
        if (success) {
            context.getState().state = InteractionState.Finished;
        } else {
            context.getState().state = InteractionState.Failed;
        }

    }

    @Override
    protected void simulateFirstRun(@NonNullDecl InteractionType type, @NonNullDecl InteractionContext context, @NonNullDecl CooldownHandler cooldownHandler) {

    }

    @Nonnull
    @Override
    public String toString() {
        return "TeleportWarpPageInteraction{} " + super.toString();
    }
}

