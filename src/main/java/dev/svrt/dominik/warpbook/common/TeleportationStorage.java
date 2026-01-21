package dev.svrt.dominik.warpbook.common;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TeleportationStorage {

  private final Map<UUID, WarpPageTeleportation> activeTeleports = new ConcurrentHashMap<>();

  public void shutdown() {
    activeTeleports.values().forEach(WarpPageTeleportation::cancel);
    activeTeleports.clear();
  }

  public WarpPageTeleportation getTeleportTask(UUID playerUUID) {
    return activeTeleports.get(playerUUID);
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

}
