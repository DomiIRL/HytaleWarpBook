package dev.svrt.dominik.warpbook.components;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import dev.svrt.dominik.warpbook.WarpBookMod;
import dev.svrt.dominik.warpbook.data.WarpPageBinding;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WarpPageTeleporter implements Component<ChunkStore> {

    @Nullable
    private WarpPageBinding warpPageBinding;

    public static ComponentType<ChunkStore, WarpPageTeleporter> getComponentType() {
        return WarpBookMod.get().getWarpPageTeleporterComponentType();
    }

    public static final BuilderCodec<WarpPageTeleporter> CODEC = BuilderCodec.builder(WarpPageTeleporter.class, WarpPageTeleporter::new)
            .append(WarpPageBinding.KEYED_CODEC, (c, v) -> c.warpPageBinding = v, c -> c.warpPageBinding)
            .add()
            .build();

    @Nonnull
    @Override
    public Component<ChunkStore> clone() {
        WarpPageTeleporter warpPageTeleporter = new WarpPageTeleporter();
        if (this.warpPageBinding != null) {
            warpPageTeleporter.warpPageBinding = this.warpPageBinding.clone();
        }
        return warpPageTeleporter;
    }

    @Nullable
    public WarpPageBinding getWarpPageBinding() {
        return warpPageBinding;
    }

    public void setWarpPageBinding(@Nullable WarpPageBinding warpPageBinding) {
        this.warpPageBinding = warpPageBinding;
    }
}
