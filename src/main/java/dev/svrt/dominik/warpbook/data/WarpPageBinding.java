package dev.svrt.dominik.warpbook.data;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Transform;

import java.util.UUID;

public class WarpPageBinding {

  public static final BuilderCodec<WarpPageBinding> CODEC = BuilderCodec.builder(WarpPageBinding.class, WarpPageBinding::new)
    .append(new KeyedCodec<>("Name", Codec.STRING),
      (c, v) -> c.name = v, c -> c.name)
    .add()
    .append(new KeyedCodec<>("Position", Transform.CODEC),
      (c, v) -> c.transform = v, c -> c.transform)
    .add()
    .append(new KeyedCodec<>("World", Codec.STRING),
      (c, v) -> c.world = v, c -> c.world)
    .add()
    .append(new KeyedCodec<>("Random", Codec.BOOLEAN),
      (c, v) -> c.random = v, c -> c.random)
    .add()
    .append(new KeyedCodec<>("Target_Entity_UUID", Codec.UUID_STRING),
      (c, v) -> c.targetEntityUUID = v, c -> c.targetEntityUUID)
    .add()
    .build();
  public static final KeyedCodec<WarpPageBinding> KEYED_CODEC = new KeyedCodec<>("Warp_Page_Binding", CODEC);

  public String name;
  public Transform transform;
  public String world;
  public UUID targetEntityUUID;
  public boolean random = false;

  @Override
  public WarpPageBinding clone() {
    WarpPageBinding binding = new WarpPageBinding();
    binding.name = this.name;
    binding.transform = this.transform != null ? this.transform.clone() : null;
    binding.world = this.world;
    binding.random = this.random;
    binding.targetEntityUUID = this.targetEntityUUID;
    return binding;
  }
}
