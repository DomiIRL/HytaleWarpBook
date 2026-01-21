package dev.svrt.dominik.warpbook.config;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class WarpBookConfig {

  public static final BuilderCodec<WarpBookConfig> CODEC = BuilderCodec.builder(
    WarpBookConfig.class, WarpBookConfig::new)
    .append(new KeyedCodec<>("Instant_Teleport", BuilderCodec.BOOLEAN),
      (c, v) -> c.instantTeleport = v, c -> c.instantTeleport)
    .add()
    .append(new KeyedCodec<>("Free_Teleport", BuilderCodec.BOOLEAN),
      (c, v) -> c.freeTeleport = v, c -> c.freeTeleport)
    .add()
    .append(new KeyedCodec<>("Cost_Item_Id", BuilderCodec.STRING),
      (c, v) -> c.costItemId = v, c -> c.costItemId)
    .add()
    .append(new KeyedCodec<>("Cost_Item_Amount", BuilderCodec.INTEGER),
      (c, v) -> c.costItemAmount = v, c -> c.costItemAmount)
    .add()
    .build();

  private boolean instantTeleport = false;
  private boolean freeTeleport = false;
  private String costItemId = "Ingredient_Void_Essence";
  private int costItemAmount = 1;

  public boolean isInstantTeleport() {
    return instantTeleport;
  }

  public boolean isFreeTeleport() {
    return freeTeleport;
  }

  public String getCostItemId() {
    return costItemId;
  }

  public int getCostItemAmount() {
    return costItemAmount;
  }
}
