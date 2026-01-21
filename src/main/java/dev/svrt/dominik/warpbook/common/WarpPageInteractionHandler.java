package dev.svrt.dominik.warpbook.common;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.svrt.dominik.warpbook.WarpBookMod;
import dev.svrt.dominik.warpbook.config.WarpBookConfig;

import javax.annotation.Nonnull;
import java.util.logging.Level;

import static dev.svrt.dominik.warpbook.WarpBookMod.LOGGER;

public class WarpPageInteractionHandler {

  public static boolean startTeleportPlayer(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull ItemStack itemStack) {
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

  public static boolean startTeleportPlayer(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull WarpPageBinding binding) {
    Player player = store.getComponent(ref, Player.getComponentType());
    if (player == null || player.getWorld() == null) {
      LOGGER.at(Level.WARNING).log("Failed to get player!");
      return false;
    }
    if (!WarpPageTeleportation.validateTeleportationRequest(player, binding)) {
      return false;
    }
    WarpBookConfig config = WarpBookMod.getInstance().getConfig().get();
    if (!config.isFreeTeleport() && player.getGameMode() != GameMode.Creative) {
      CombinedItemContainer everything = player.getInventory().getCombinedEverything();
      ItemStack itemStack = new ItemStack(config.getCostItemId(), config.getCostItemAmount());
      ItemStackTransaction transaction = everything.removeItemStack(itemStack);
      if (!transaction.succeeded()) {
        Message first = Message.raw("You need ");
        Message item = Message.translation(itemStack.getItem().getTranslationKey());
        Message second = Message.raw(" ");
        Message amount = Message.raw(String.valueOf(config.getCostItemAmount()));
        player.sendMessage(Message.join(first, amount, second, item));
        return false;
      }
    }

    new WarpPageTeleportation(binding, ref, store).start();
    return true;
  }

  public static boolean bindHeldWarpPage(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull InteractionContext context, @Nonnull String name) {
    if (name.isEmpty()) {
      return false;
    }

    Player player = store.getComponent(ref, Player.getComponentType());
    if (player == null || player.getWorld() == null) {
      return false;
    }
    if (player.getWorld().getName().startsWith("instance-")) {
      player.sendMessage(Message.raw("Temporary worlds are currently not supported as warp pages."));
      return false;
    }

    ItemContainer itemContainer = context.getHeldItemContainer();
    if (itemContainer == null) {
      LOGGER.at(Level.WARNING).log("Failed to get player inventory!");
      return false;
    }
    ItemStack heldItem = context.getHeldItem();
    if (heldItem == null || heldItem.isEmpty()) {
      LOGGER.at(Level.WARNING).log("Held item is null");
      return false;
    }

    TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());
    if (transformComponent == null) {
      LOGGER.at(Level.WARNING).log("Failed to get player transform!");
      return false;
    }

    WarpPageBinding binding = new WarpPageBinding();
    binding.transform = transformComponent.getTransform().clone();
    binding.world = player.getWorld().getName();
    binding.name = name;

    ItemStack boundWarpPage = new ItemStack("Warp_Page_Bound", 1).withMetadata(WarpPageBinding.KEYED_CODEC, binding);

    byte heldItemSlot = context.getHeldItemSlot();
    boolean removed = itemContainer.removeItemStackFromSlot(heldItemSlot, heldItem, 1).succeeded();

    if (!removed) {
      LOGGER.at(Level.WARNING).log("Failed to remove warp page from inventory!");
      return false;
    }

    boolean added = itemContainer.addItemStackToSlot(heldItemSlot, boundWarpPage).succeeded();

    if (!added) {
      itemContainer.setItemStackForSlot(heldItemSlot, heldItem);
      return false;
    }

    UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
    if (uuidComponent == null) {
      LOGGER.at(Level.WARNING).log("Failed to get player UUID!");
      return false;
    }
    return true;
  }

}
