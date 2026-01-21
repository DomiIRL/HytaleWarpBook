package dev.svrt.dominik.warpbook.common;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.svrt.dominik.warpbook.WarpBookMod;
import dev.svrt.dominik.warpbook.services.TeleportationService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
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
  private final List<ScheduledFuture<Void>> scheduledTasks = new LinkedList<>();

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

  public void start() {
    if (!scheduledTasks.isEmpty()) {
      LOGGER.at(Level.WARNING).log("Teleportation already started!");
      return;
    }

    Player player = entityStore.getComponent(entityRef, Player.getComponentType());
    if (player == null) {
      LOGGER.at(Level.WARNING).log("Failed to get player component for teleportation!");
      return;
    }

    TeleportationService teleportationService = WarpBookMod.getInstance().getTeleportationService();
    UUIDComponent uuidComponent = entityStore.getComponent(entityRef, UUIDComponent.getComponentType());
    if (uuidComponent == null) {
      LOGGER.at(Level.WARNING).log("Failed to get UUID component for teleportation!");
      return;
    }
    UUID playerUUID = uuidComponent.getUuid();
    if (teleportationService.hasActiveTeleport(playerUUID)) {
      player.sendMessage(Message.raw("Another teleportation is already in progress!"));
      return;
    }

    World world = player.getWorld();
    if (world == null) {
      LOGGER.at(Level.WARNING).log("Failed to get world for teleportation!");
      return;
    }

    boolean instantTeleport = WarpBookMod.getInstance().getConfig().get().isInstantTeleport();
    scheduledTasks.add(HytaleServer.SCHEDULED_EXECUTOR.schedule(
      () -> {
        // Execute teleport on the world thread
        world.execute(() -> {
          if (!entityRef.isValid()) {
            LOGGER.at(Level.WARNING).log("Failed to teleport player! Entity is no longer valid.");
            teleportationService.removeTeleportTask(playerUUID);
            return;
          }

          if (!teleportationService.validateTeleportationRequest(player, binding)) {
            teleportationService.removeTeleportTask(playerUUID);
            return;
          }

          Transform transform = binding.transform;
          entityStore.addComponent(
            entityRef,
            Teleport.getComponentType(),
            new Teleport(world, transform.getPosition(), transform.getRotation())
          );

          teleportationService.removeTeleportTask(playerUUID);
        });
        return null;
      },
      instantTeleport ? 0 : 2,
      TimeUnit.SECONDS
    ));
    scheduledTasks.add(HytaleServer.SCHEDULED_EXECUTOR.schedule(
      () -> {
        world.execute(() -> {
          PlayerRef playerRef = entityStore.getComponent(entityRef, PlayerRef.getComponentType());
          if (playerRef == null) {
            LOGGER.at(Level.WARNING).log("Failed to get player component for teleportation!");
            return;
          }
          SoundUtil.playSoundEvent2dToPlayer(
            playerRef,
            SoundEvent.getAssetMap().getIndex("SFX_Portal_Neutral_Open"),
            SoundCategory.SFX,
            5, 1f
          );
        });
        return null;
      },
      instantTeleport ? 0 : 1,
      TimeUnit.SECONDS
    ));

    for (ScheduledFuture<Void> task : scheduledTasks) {
      WarpBookMod.getInstance().getTaskRegistry().registerTask(task);
    }

    teleportationService.registerTeleportTask(playerUUID, this);

    TransformComponent component = entityStore.getComponent(entityRef, TransformComponent.getComponentType());
    if (component == null) return;
    Vector3d position = component.getPosition();
    if (!instantTeleport) {
      ParticleUtil.spawnParticleEffect("Warp_Portal_Entry", position.clone(), entityStore);
    }
    SoundUtil.playSoundEvent3d(
      SoundEvent.getAssetMap().getIndex("SFX_Skeleton_Mage_Spellbook_Charge"),
      SoundCategory.SFX,
      position.getX(), position.getY(), position.getZ(),
      3, 0.5f,
      entityStore
    );
  }

  public void cancel() {
    for (ScheduledFuture<Void> task : scheduledTasks) {
      if (!task.isDone()) {
        task.cancel(false);
      }
    }
    scheduledTasks.clear();

    PlayerRef component = entityStore.getComponent(entityRef, PlayerRef.getComponentType());
    if (component != null) {
      component.sendMessage(Message.raw("Teleportation cancelled."));
    }
  }

  public Instant getStartTimestamp() {
    return startTimestamp;
  }

  public Vector3d getStartPosition() {
    return startPosition;
  }

}
