package dev.svrt.dominik.warpbook.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class Teleporter implements Component<ChunkStore> {

    @Nullable
    private UUID worldUuid;
    @Nullable
    private Transform transform;
    private byte relativeMask = 0;
    @Nullable
    private String warp;

    public static final BuilderCodec<Teleporter> CODEC = BuilderCodec.builder(Teleporter.class, Teleporter::new)
            .append(new KeyedCodec<>("World", Codec.UUID_BINARY), (c, v) -> c.worldUuid = v, c -> c.worldUuid)
            .add()
            .append(new KeyedCodec<>("Transform", Transform.CODEC), (c, v) -> c.transform = v, c -> c.transform)
            .add()
            .append(new KeyedCodec<>("Relative", Codec.BYTE), (c, v) -> c.relativeMask = v, c -> c.relativeMask)
            .add()
            .append(new KeyedCodec<>("Warp", Codec.STRING), (c, v) -> c.warp = v, c -> c.warp)
            .add()
            .build();

    @Nullable
    public UUID getWorldUuid() {
        return this.worldUuid;
    }

    public void setWorldUuid(@Nullable UUID worldUuid) {
        this.worldUuid = worldUuid;
    }

    @Nullable
    public Transform getTransform() {
        return this.transform;
    }

    public void setTransform(@Nullable Transform transform) {
        this.transform = transform;
    }

    public byte getRelativeMask() {
        return this.relativeMask;
    }

    public void setRelativeMask(byte relativeMask) {
        this.relativeMask = relativeMask;
    }

    @Nullable
    public String getWarp() {
        return this.warp;
    }

    public void setWarp(@Nullable String warp) {
        this.warp = warp != null && !warp.isEmpty() ? warp : null;
    }

    public boolean isValid() {
        if (this.warp != null && !this.warp.isEmpty()) {
            return true;
        } else if (this.transform != null) {
            if (this.worldUuid != null) {
                return Universe.get().getWorld(this.worldUuid) != null;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    @Nonnull
    @Override
    public Component<ChunkStore> clone() {
        Teleporter teleporter = new Teleporter();
        teleporter.worldUuid = this.worldUuid;
        teleporter.transform = this.transform != null ? this.transform.clone() : null;
        teleporter.relativeMask = this.relativeMask;
        teleporter.warp = this.warp;
        return teleporter;
    }

    @Nullable
    public Teleport toTeleport(@Nonnull Vector3d currentPosition, @Nonnull Vector3f currentRotation, @Nonnull Vector3i blockPosition) {
        if (this.warp != null && !this.warp.isEmpty()) {
             return null;
        } else if (this.transform != null) {
            World world = null;
            if (this.worldUuid != null) {
                world = Universe.get().getWorld(this.worldUuid);
            }

            if (world != null) {
                if (this.relativeMask != 0) {
                    Transform teleportTransform = this.transform.clone();
                    Transform.applyMaskedRelativeTransform(teleportTransform, this.relativeMask, currentPosition, currentRotation, blockPosition);
                    return Teleport.createForPlayer(world, teleportTransform);
                }
                return Teleport.createForPlayer(world, this.transform);
            } else if (this.worldUuid == null) {
                 if (this.relativeMask != 0) {
                    Transform teleportTransform = this.transform.clone();
                    Transform.applyMaskedRelativeTransform(teleportTransform, this.relativeMask, currentPosition, currentRotation, blockPosition);
                    return Teleport.createForPlayer(teleportTransform);
                } else {
                    return Teleport.createForPlayer(this.transform);
                }
            }
            return null;
        } else {
            return null;
        }
    }
}

