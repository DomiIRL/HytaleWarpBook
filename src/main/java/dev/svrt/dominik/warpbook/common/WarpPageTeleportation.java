package dev.svrt.dominik.warpbook.common;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.svrt.dominik.warpbook.WarpBookMod;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static dev.svrt.dominik.warpbook.WarpBookMod.LOGGER;

public class WarpPageTeleportation {

  private final WarpPageBinding binding;
  private final Ref<EntityStore> entityRef;
  private final Store<EntityStore> entityStore;
  private final Instant startTimestamp;
  private final Vector3d startPosition;
  private ScheduledFuture<Void> scheduledTask;

  public WarpPageTeleportation(WarpPageBinding binding, Ref<EntityStore> entityRef, Store<EntityStore> entityStore) {
    this.binding = binding;
    this.entityRef = entityRef;
    this.entityStore = entityStore;
    this.startTimestamp = entityStore.getResource(TimeResource.getResourceType()).getNow();

    TransformComponent transformComponent = entityStore.getComponent(entityRef, TransformComponent.getComponentType());
    if (transformComponent != null) {
      Vector3d pos = transformComponent.getTransform().getPosition();
      this.startPosition = new Vector3d(pos.x, pos.y, pos.z);
    } else {
      this.startPosition = null;
    }
  }

  public void start(World world) {
    Player player = entityStore.getComponent(entityRef, Player.getComponentType());
    if (player == null) {
      LOGGER.at(Level.WARNING).log("Failed to get player component for teleportation!");
      return;
    }

    UUIDComponent uuidComponent = entityStore.getComponent(entityRef, UUIDComponent.getComponentType());
    if (uuidComponent == null) {
      LOGGER.at(Level.WARNING).log("Failed to get UUID component for teleportation!");
      return;
    }
    UUID playerUUID = uuidComponent.getUuid();

    TeleportationStorage storage = WarpBookMod.getInstance().getTeleportationStorage();

    storage.cancelTeleport(playerUUID);

    player.sendMessage(Message.raw("Teleporting in 2 seconds..."));

    scheduledTask = HytaleServer.SCHEDULED_EXECUTOR.schedule(
      () -> {
        // Execute teleport on the world thread
        world.execute(() -> {
          if (!entityRef.isValid()) {
            LOGGER.at(Level.WARNING).log("Failed to teleport player! Entity is no longer valid.");
            storage.removeTeleportTask(playerUUID);
            return;
          }

          Transform transform = binding.transform;
          entityStore.addComponent(
            entityRef,
            Teleport.getComponentType(),
            new Teleport(world, transform.getPosition(), transform.getRotation())
          );

          player.sendMessage(Message.raw("Teleported successfully!"));
          storage.removeTeleportTask(playerUUID);
        });
        return null;
      },
      2,
      TimeUnit.SECONDS
    );

    WarpBookMod.getInstance().getTaskRegistry().registerTask(scheduledTask);
    storage.registerTeleportTask(playerUUID, this);

    // spawn particle
    world.execute(() -> {
    });
  }

  public void cancel() {
    if (scheduledTask != null && !scheduledTask.isDone()) {
      scheduledTask.cancel(false);

      PlayerRef component = entityStore.getComponent(entityRef, PlayerRef.getComponentType());
      if (component != null) {
        component.sendMessage(Message.raw("Teleportation cancelled."));
      }
    }
  }

  public Instant getStartTimestamp() {
    return startTimestamp;
  }

  public Vector3d getStartPosition() {
    return startPosition;
  }
}
