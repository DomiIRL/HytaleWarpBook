package dev.svrt.dominik.warpbook.config;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class WarpBookConfig {

  public static final BuilderCodec<WarpBookConfig> CODEC = BuilderCodec.builder(
    WarpBookConfig.class, WarpBookConfig::new)
    .append(new KeyedCodec<>("Instant_Teleport", BuilderCodec.BOOLEAN),
      (c, v) -> c.instantTeleport = v, c -> c.instantTeleport)
    .add()
    .append(new KeyedCodec<>("Teleport_Price", TeleportationPrice.CODEC),
      (c, v) -> c.teleportPrice = v, c -> c.teleportPrice)
    .add()
    .append(new KeyedCodec<>("Teleporter_Binding_Price", TeleportationPrice.CODEC),
      (c, v) -> c.teleporterBindingPrice = v, c -> c.teleporterBindingPrice)
    .add()
    .build();

  private boolean instantTeleport = false;
  private TeleportationPrice teleportPrice = new TeleportationPrice("Ingredient_Void_Essence", 1);
  private TeleportationPrice teleporterBindingPrice = new TeleportationPrice("Ingredient_Void_Essence", 5);

  public boolean isInstantTeleport() {
    return instantTeleport;
  }

  public TeleportationPrice getTeleportPrice() {
    return teleportPrice;
  }

  public TeleportationPrice getTeleporterBindingPrice() {
    return teleporterBindingPrice;
  }
}
