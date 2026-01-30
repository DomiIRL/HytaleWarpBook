package dev.svrt.dominik.warpbook.config;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;

import javax.annotation.Nullable;

public class Price {

    public static final BuilderCodec<Price> CODEC = BuilderCodec.builder(Price.class, Price::new)
            .append(new KeyedCodec<>("ItemId", BuilderCodec.STRING),
                    (p, v) -> p.itemId = v, p -> p.itemId)
            .add()
            .append(new KeyedCodec<>("Amount", BuilderCodec.INTEGER),
                    (p, v) -> p.amount = v, p -> p.amount)
            .add()
            .build();

    private String itemId;
    private int amount;

    public Price() {}

    public Price(String itemId, int amount) {
        this.itemId = itemId;
        this.amount = amount;
    }

    @Nullable
    public String getItemId() {
        return itemId;
    }

    public int getAmount() {
        return amount;
    }

    public boolean isFree() {
        if (amount <= 0) return true;
        if (itemId == null || itemId.isEmpty()) return true;
        Item item = Item.getAssetMap().getAsset(itemId);
      return item == null || item == Item.UNKNOWN;
    }
}

