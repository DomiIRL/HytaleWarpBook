package dev.svrt.dominik.warpbook.services;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.svrt.dominik.warpbook.data.WarpPageBinding;
import dev.svrt.dominik.warpbook.data.WarpPageBindingType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;
import java.util.logging.Level;

import static dev.svrt.dominik.warpbook.WarpBookMod.LOGGER;

public class WarpPageBindingService {

  public boolean bindHeldWarpPage(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull InteractionContext context, @Nonnull String name, @Nonnull WarpPageBindingType type, @Nullable UUID targetUUID) {
    if (name.isBlank()) {
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

    WarpPageBinding binding = new WarpPageBinding();
    binding.name = name;

    if (type == WarpPageBindingType.POSITION) {
      TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());
      if (transformComponent == null) {
        LOGGER.at(Level.WARNING).log("Failed to get player transform!");
        return false;
      }
      binding.world = player.getWorld().getName();
      binding.transform = transformComponent.getTransform().clone();
    } else if (type == WarpPageBindingType.ENTITY) {
      if (targetUUID == null) {
        LOGGER.at(Level.WARNING).log("Target UUID is null for entity binding!");
        return false;
      }
      binding.targetEntityUUID = targetUUID;
    } else {
      LOGGER.at(Level.WARNING).log("Invalid warp page binding type!");
      return false;
    }

    ItemStack boundWarpPage = new ItemStack(type.itemType, 1).withMetadata(WarpPageBinding.KEYED_CODEC, binding);

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
