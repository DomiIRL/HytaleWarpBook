package dev.svrt.dominik.warpbook.data;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Transform;

public class WarpPageBinding {

  public static final BuilderCodec<WarpPageBinding> CODEC = BuilderCodec.builder(WarpPageBinding.class, WarpPageBinding::new)
    .append(new KeyedCodec<>("Name", Codec.STRING),
      (c, v) -> c.name = v, c -> c.name)
    .add()
    .append(new KeyedCodec<>("X", Transform.CODEC),
      (c, v) -> c.transform = v, c -> c.transform)
    .add()

    .append(new KeyedCodec<>("World", Codec.STRING),
      (c, v) -> c.world = v, c -> c.world)
    .add()
    .build();
  public static final KeyedCodec<WarpPageBinding> KEYED_CODEC = new KeyedCodec<>("Warp_Page_Binding", CODEC);

  public String name;
  public Transform transform;
  public String world;

}
