package dev.svrt.dominik.warpbook.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.svrt.dominik.warpbook.components.WarpPageTeleporter;

import javax.annotation.Nullable;

public class WarpPageTeleporterUISupplier implements OpenCustomUIInteraction.CustomPageSupplier {

  @Nullable
  private String activeState;

  public static final BuilderCodec<WarpPageTeleporterUISupplier> CODEC =
    BuilderCodec.builder(WarpPageTeleporterUISupplier.class, WarpPageTeleporterUISupplier::new)
      .appendInherited(new KeyedCodec<>("ActiveState", Codec.STRING),
        (supplier, o) -> supplier.activeState = o,
        (supplier) -> supplier.activeState,
        (supplier, parent) -> supplier.activeState = parent.activeState)
      .add()
      .build();

  @Nullable
  @Override
  public CustomUIPage tryCreate(Ref<EntityStore> ref, ComponentAccessor<EntityStore> componentAccessor, PlayerRef playerRef, InteractionContext context) {

    BlockPosition targetBlock = context.getTargetBlock();
    if (targetBlock == null) {
      return null;
    }

    Store<EntityStore> store = ref.getStore();
    World world = store.getExternalData().getWorld();
    ChunkStore chunkStore = world.getChunkStore();
    Ref<ChunkStore> chunkRef = chunkStore.getChunkReference(ChunkUtil.indexChunkFromBlock(targetBlock.x, targetBlock.z));
    BlockComponentChunk blockComponentChunk = chunkRef == null ? null : chunkStore.getStore().getComponent(chunkRef, BlockComponentChunk.getComponentType());
    if (blockComponentChunk == null) {
      return null;
    }
    int blockIndex = ChunkUtil.indexBlockInColumn(targetBlock.x, targetBlock.y, targetBlock.z);
    Ref<ChunkStore> blockRef = blockComponentChunk.getEntityReference(blockIndex);
    if (blockRef == null || !blockRef.isValid()) {
      Holder<ChunkStore> holder = ChunkStore.REGISTRY.newHolder();
      holder.putComponent(BlockModule.BlockStateInfo.getComponentType(), new BlockModule.BlockStateInfo(blockIndex, chunkRef));
      holder.ensureComponent(WarpPageTeleporter.getComponentType());
      blockRef = world.getChunkStore().getStore().addEntity(holder, AddReason.SPAWN);
    }

    if (blockRef == null) {
      return null;
    }

    return new WarpPageTeleporterUI(playerRef, context, blockRef, this.activeState);
  }
}
