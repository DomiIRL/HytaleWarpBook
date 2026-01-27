package dev.svrt.dominik.warpbook.entities;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.svrt.dominik.warpbook.WarpBookMod;
import dev.svrt.dominik.warpbook.data.WarpPageBinding;
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

  private static final String PARTICLE_ENTRY = "AWB_Warp_Entry";
  private static final String PARTICLE_ARRIVAL = "AWB_Warp_Arrival";
  private static final String SOUND_OPEN = "SFX_Portal_Neutral_Open";
  private static final String SOUND_CHARGE = "SFX_Skeleton_Mage_Spellbook_Charge";
  private static final double PARTICLE_DISTANCE_THRESHOLD = 100.0;

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
    UUIDComponent uuidComponent = entityStore.getComponent(entityRef, UUIDComponent.getComponentType());

    if (player == null || uuidComponent == null) {
      LOGGER.at(Level.WARNING).log("Failed to get necessary components for teleportation!");
      return;
    }

    TeleportationService teleportationService = WarpBookMod.getInstance().getTeleportationService();
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

    scheduleTeleportTask(world, playerUUID, teleportationService, instantTeleport);
    scheduleSoundTask(world, instantTeleport);
    registerTasks(playerUUID, teleportationService);
    playStartEffects(instantTeleport);
  }

  private void scheduleTeleportTask(World world, UUID playerUUID, TeleportationService service, boolean instant) {
    scheduledTasks.add(HytaleServer.SCHEDULED_EXECUTOR.schedule(
      () -> {
        world.execute(() -> executeTeleport(world, playerUUID, service));
        return null;
      },
      instant ? 0 : 2,
      TimeUnit.SECONDS
    ));
  }

  private void executeTeleport(World world, UUID playerUUID, TeleportationService service) {
    if (!validateTeleportExecution(service, playerUUID)) return;

    Transform transform = binding.transform;
    entityStore.addComponent(
      entityRef,
      Teleport.getComponentType(),
      Teleport.createForPlayer(world, transform)
    );

    double distance = calculateDistance(startPosition, transform.getPosition());
    if (distance >= PARTICLE_DISTANCE_THRESHOLD) {
      spawnDestinationParticle(transform.getPosition());
    }

    service.removeTeleportTask(playerUUID);
  }

  private boolean validateTeleportExecution(TeleportationService service, UUID playerUUID) {
    if (!entityRef.isValid()) {
      LOGGER.at(Level.WARNING).log("Failed to teleport player! Entity is no longer valid.");
      service.removeTeleportTask(playerUUID);
      return false;
    }

    Player player = entityStore.getComponent(entityRef, Player.getComponentType());
    if (player == null) {
      LOGGER.at(Level.WARNING).log("Failed to get player component during teleport execution!");
      service.removeTeleportTask(playerUUID);
      return false;
    }

    if (!service.validateTeleportationRequest(player, binding)) {
      service.removeTeleportTask(playerUUID);
      return false;
    }

    return true;
  }

  private void scheduleSoundTask(World world, boolean instant) {
    scheduledTasks.add(HytaleServer.SCHEDULED_EXECUTOR.schedule(
      () -> {
        world.execute(this::playTeleportSound);
        return null;
      },
      instant ? 0 : 1,
      TimeUnit.SECONDS
    ));
  }

  private void playTeleportSound() {
    PlayerRef playerRef = entityStore.getComponent(entityRef, PlayerRef.getComponentType());
    if (playerRef == null) {
      LOGGER.at(Level.WARNING).log("Failed to get player component for teleportation!");
      return;
    }
    SoundUtil.playSoundEvent2dToPlayer(
      playerRef,
      SoundEvent.getAssetMap().getIndex(SOUND_OPEN),
      SoundCategory.SFX,
      5, 1f
    );
  }

  private void registerTasks(UUID playerUUID, TeleportationService service) {
    for (ScheduledFuture<Void> task : scheduledTasks) {
      WarpBookMod.getInstance().getTaskRegistry().registerTask(task);
    }
    service.registerTeleportTask(playerUUID, this);
  }

  private void playStartEffects(boolean instant) {
    TransformComponent component = entityStore.getComponent(entityRef, TransformComponent.getComponentType());
    if (component == null) return;
    Vector3d from = component.getPosition();
    Vector3d to = binding.transform.getPosition();

    if (!instant) {
      ParticleUtil.spawnParticleEffect(PARTICLE_ENTRY, from.clone(), entityStore);
      ParticleUtil.spawnParticleEffect(PARTICLE_ENTRY, to.clone(), entityStore);
    }

    SoundUtil.playSoundEvent3d(
      SoundEvent.getAssetMap().getIndex(SOUND_CHARGE),
      SoundCategory.SFX,
      from.getX(), from.getY(), from.getZ(),
      3, 0.5f,
      entityStore
    );
  }

  private void spawnDestinationParticle(Vector3d position) {
    SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource = (SpatialResource) entityStore.getResource(EntityModule.get().getPlayerSpatialResourceType());
    List<Ref<EntityStore>> playerRefs = new ArrayList<>();
    playerSpatialResource.getSpatialStructure().collect(position, 75.0, playerRefs);

    if (!playerRefs.contains(entityRef)) {
      playerRefs.add(entityRef);
    }
    ParticleUtil.spawnParticleEffect(PARTICLE_ARRIVAL, position, playerRefs, entityStore);
  }

  private double calculateDistance(Vector3d from, Vector3d to) {
    if (from == null || to == null) return Double.MAX_VALUE;
    return Math.sqrt(Math.pow(from.x - to.x, 2) + Math.pow(from.y - to.y, 2) + Math.pow(from.z - to.z, 2));
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
