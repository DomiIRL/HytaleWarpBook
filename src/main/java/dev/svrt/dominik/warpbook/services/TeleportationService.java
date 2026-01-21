package dev.svrt.dominik.warpbook.services;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.svrt.dominik.warpbook.common.WarpPageBinding;
import dev.svrt.dominik.warpbook.common.WarpPageTeleportation;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import static dev.svrt.dominik.warpbook.WarpBookMod.LOGGER;

public class TeleportationService {

    private final Map<UUID, WarpPageTeleportation> activeTeleports = new ConcurrentHashMap<>();

    public void shutdown() {
        activeTeleports.values().forEach(WarpPageTeleportation::cancel);
        activeTeleports.clear();
    }

    public WarpPageTeleportation getTeleportTask(UUID playerUUID) {
        return activeTeleports.get(playerUUID);
    }

    public void teleportPlayer(Ref<EntityStore> ref, Store<EntityStore> store, WarpPageBinding binding) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) {
            LOGGER.at(Level.WARNING).log("Failed to get player for teleportation!");
            return;
        }

        UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
        if (uuidComponent == null) {
            LOGGER.at(Level.WARNING).log("Failed to get UUID component for teleportation!");
            return;
        }

        UUID playerUUID = uuidComponent.getUuid();
        if (hasActiveTeleport(playerUUID)) {
            player.sendMessage(Message.raw("Another teleportation is already in progress!"));
            return;
        }

        if (!validateTeleportationRequest(player, binding)) {
            return;
        }

        WarpPageTeleportation teleportation = new WarpPageTeleportation(binding, ref, store);
        teleportation.start();
    }

    public void registerTeleportTask(UUID playerUUID, WarpPageTeleportation teleportation) {
        cancelTeleport(playerUUID);
        activeTeleports.put(playerUUID, teleportation);
    }

    public void removeTeleportTask(UUID playerUUID) {
        activeTeleports.remove(playerUUID);
    }

    public void cancelTeleport(UUID playerUUID) {
        WarpPageTeleportation teleportation = activeTeleports.remove(playerUUID);
        if (teleportation != null) {
            teleportation.cancel();
        }
    }

    public boolean hasActiveTeleport(UUID playerUUID) {
        return activeTeleports.containsKey(playerUUID);
    }

    public boolean validateTeleportationRequest(Player player, WarpPageBinding binding) {
        World currentWorld = player.getWorld();
        if (currentWorld == null) {
            LOGGER.at(Level.WARNING).log("Failed to get current world!");
            return false;
        }
        if (!currentWorld.getName().equals(binding.world)) { // Simplification: Accessing field directly if public
             player.sendMessage(Message.raw(String.format(
                "You are not in the correct world! (%s)", binding.world
            )));
            return false;
        }
        return true;
    }
}
