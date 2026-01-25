package dev.svrt.dominik.warpbook.data;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemDrop;
import com.hypixel.hytale.server.core.asset.type.item.config.container.ItemDropContainer;
import org.bson.BsonDocument;

import java.util.List;
import java.util.Set;
import java.util.function.DoubleSupplier;
import javax.annotation.Nonnull;

public class WarpPageItemDropContainer extends ItemDropContainer {

  public static final BuilderCodec<WarpPageItemDropContainer> CODEC;

  public WarpPageItemDropContainer(double chance) {
    super(chance);
  }

  public WarpPageItemDropContainer() {

  }

  protected void populateDrops(@Nonnull List<ItemDrop> drops, DoubleSupplier chanceProvider, Set<String> droplistReferences) {
    drops.add(new ItemDrop("Bound_Warp_Page", createRandomWarpPage(), 1, 1));
  }

  @Nonnull
  public List<ItemDrop> getAllDrops(@Nonnull List<ItemDrop> list) {
    list.add(new ItemDrop("Bound_Warp_Page", createRandomWarpPage(), 1, 1));
    return list;
  }

  private BsonDocument createRandomWarpPage() {
    BsonDocument doc = new BsonDocument();
    return doc;
  }

  @Nonnull
  public String toString() {
    return "WarpPageItemDropContainer{drop=" + "Warp_Page" + ", weight=" + this.weight + "}";
  }

  static {
    CODEC = BuilderCodec.builder(WarpPageItemDropContainer.class, WarpPageItemDropContainer::new, ItemDropContainer.DEFAULT_CODEC).build();
  }
}
