package dev.svrt.dominik.warpbook.services;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.svrt.dominik.warpbook.WarpBookMod;
import dev.svrt.dominik.warpbook.data.WarpPageBinding;

import javax.annotation.Nonnull;
import java.util.logging.Level;

import static dev.svrt.dominik.warpbook.WarpBookMod.LOGGER;

public class WarpPageUsageService {

  public boolean startTeleportPlayer(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull ItemStack itemStack) {
    Player player = store.getComponent(ref, Player.getComponentType());
    if (player == null) {
      LOGGER.at(Level.WARNING).log("Failed to get player!");
      return false;
    }
    WarpPageBinding positionBinding = itemStack.getFromMetadataOrNull(WarpPageBinding.KEYED_CODEC);
    if (positionBinding == null) {
      player.sendMessage(Message.raw("Warp page has no binding!"));
      return false;
    }
    return startTeleportPlayer(ref, store, positionBinding);
  }

  public boolean startTeleportPlayer(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull WarpPageBinding binding) {
    Player player = store.getComponent(ref, Player.getComponentType());
    if (player == null || player.getWorld() == null) {
      LOGGER.at(Level.WARNING).log("Failed to get player!");
      return false;
    }

    TeleportationService teleportationService = WarpBookMod.getInstance().getTeleportationService();
    PaymentService paymentService = WarpBookMod.getInstance().getPaymentService();

    if (!teleportationService.validateTeleportationRequest(player, binding)) {
      return false;
    }

    if (!paymentService.processPayment(player)) {
      return false;
    }

    teleportationService.teleportPlayer(ref, store, binding);
    return true;
  }
}
