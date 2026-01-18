package dev.svrt.dominik.warpbook.common;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.logging.Level;

import static dev.svrt.dominik.warpbook.WarpBookMod.LOGGER;

public class WarpPageInteractions {

  public static boolean teleportPlayer(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull ItemStack itemStack) {
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
    return teleportPlayer(ref, store, positionBinding);
  }

  public static boolean teleportPlayer(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull WarpPageBinding binding) {
    Player player = store.getComponent(ref, Player.getComponentType());
    if (player == null || player.getWorld() == null) {
      LOGGER.at(Level.WARNING).log("Failed to get player!");
      return false;
    }
    World currentWorld = player.getWorld();
    if (!currentWorld.getName().equals(binding.world)) {
      player.sendMessage(Message.raw(String.format(
        "You are not in the correct world! (%s)", binding.world
      )));
      return false;
    }

    currentWorld.execute(() -> {
      if (!ref.isValid()) {
        LOGGER.at(Level.WARNING).log("Failed to teleport player! Entity is no longer valid.");
        return;
      }

      Transform transform = binding.transform;
      store.addComponent(ref, Teleport.getComponentType(), new Teleport(currentWorld, transform.getPosition(), transform.getRotation()));
    });

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
    if (heldItem == null || !heldItem.getItemId().equals("Warp_Page")) {
      LOGGER.at(Level.WARNING).log("Held item is not a warp page!");
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
