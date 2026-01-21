package dev.svrt.dominik.warpbook.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.damage.DamageDataComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.svrt.dominik.warpbook.WarpBookMod;
import dev.svrt.dominik.warpbook.common.TeleportationStorage;
import dev.svrt.dominik.warpbook.common.WarpPageTeleportation;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.time.Instant;
import java.util.UUID;

public class TeleportCancelSystem extends EntityTickingSystem<EntityStore> {

  private final ComponentType<EntityStore, PlayerRef> playerRefComponentType = PlayerRef.getComponentType();
  private final ComponentType<EntityStore, Player> playerComponentType = Player.getComponentType();
  private final ComponentType<EntityStore, EntityStatMap> entityStatMapComponentType = EntityStatMap.getComponentType();
  private final ComponentType<EntityStore, DamageDataComponent> damageComponentType = DamageDataComponent.getComponentType();
  private final ComponentType<EntityStore, UUIDComponent> uuidComponentComponentType = UUIDComponent.getComponentType();
  private final ComponentType<EntityStore, TransformComponent> transformComponentType = TransformComponent.getComponentType();
  private final Query<EntityStore> QUERY = Query.and(this.playerRefComponentType, this.entityStatMapComponentType, this.damageComponentType, this.uuidComponentComponentType);

  @Override
  public void tick(float v, int index, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {
    UUIDComponent uuidComponent = archetypeChunk.getComponent(index, uuidComponentComponentType);
    if (uuidComponent == null) return;
    UUID uuid = uuidComponent.getUuid();

    TeleportationStorage storage = WarpBookMod.getInstance().getTeleportationStorage();
    WarpPageTeleportation teleportTask = storage.getTeleportTask(uuid);
    if (teleportTask == null) {
      return;
    }

    PlayerRef ref = archetypeChunk.getComponent(index, playerRefComponentType);
    if (ref == null) return;
    EntityStatMap stats = archetypeChunk.getComponent(index, entityStatMapComponentType);
    if (stats == null) return;
    DamageDataComponent damage = archetypeChunk.getComponent(index, damageComponentType);
    if (damage == null) return;

    Instant teleportationStart = teleportTask.getStartTimestamp();
    if (damage.getLastCombatAction().isAfter(teleportationStart) || damage.getLastDamageTime().isAfter(teleportationStart)) {
      storage.cancelTeleport(uuid);
      return;
    }

    TransformComponent transformComponent = archetypeChunk.getComponent(index, transformComponentType);
    if (transformComponent == null) {
      return;
    }
    Vector3d startPosition = teleportTask.getStartPosition();
    Transform transform = transformComponent.getTransform();
    Vector3d currentPos = transform.getPosition();
    double distance = Math.sqrt(
      Math.pow(currentPos.x - startPosition.x, 2) +
        Math.pow(currentPos.y - startPosition.y, 2) +
        Math.pow(currentPos.z - startPosition.z, 2)
    );

    if (distance > 1.0) {
      storage.cancelTeleport(uuid);
    }
  }

  @NullableDecl
  @Override
  public Query<EntityStore> getQuery() {
    return QUERY;
  }
}
