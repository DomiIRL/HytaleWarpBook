package dev.svrt.dominik.warpbook.listener;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemDrop;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemDropList;
import com.hypixel.hytale.server.core.asset.type.item.config.container.ItemDropContainer;
import com.hypixel.hytale.server.core.asset.type.item.config.container.MultipleItemDropContainer;
import com.hypixel.hytale.server.core.asset.type.item.config.container.SingleItemDropContainer;
import com.hypixel.hytale.server.core.event.events.BootEvent;
import dev.svrt.dominik.warpbook.data.WarpPageBinding;
import org.bson.BsonDocument;
import org.bson.BsonValue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dev.svrt.dominik.warpbook.WarpBookMod.LOGGER;

public class RandomWarpPagesDropListAdder {

    private static final String ITEM_ID = "Warp_Page_Bound";
    private static final String TARGET_LIST_PREFIX = "Zone";
    private static final int WEIGHT = 5;

    public static void onBoot(BootEvent ignored) {
        Map<String, ItemDropList> map = ItemDropList.getAssetMap().getAssetMap();
        ItemDropContainer warpPageContainer = createWarpPageContainer();

        for (Map.Entry<String, ItemDropList> entry : map.entrySet()) {
            if (!entry.getKey().startsWith(TARGET_LIST_PREFIX)) {
                continue;
            }

            injectWarpPage(entry.getValue(), warpPageContainer);
        }
    }

    private static ItemDropContainer createWarpPageContainer() {
        BsonDocument itemDropMetaData = new BsonDocument();

        WarpPageBinding binding = new WarpPageBinding();
        binding.random = true;

        KeyedCodec<WarpPageBinding> bindingKeyedCodec = WarpPageBinding.KEYED_CODEC;
        String bindingKey = bindingKeyedCodec.getKey();
        Codec<WarpPageBinding> bindingCodec = bindingKeyedCodec.getChildCodec();

        BsonValue bindingBsonValue = bindingCodec.encode(binding, new ExtraInfo());
        itemDropMetaData.put(bindingKey, bindingBsonValue);

        ItemDrop randomWarpPageItemDrop = new ItemDrop(ITEM_ID, itemDropMetaData, 1, 1);
        return new SingleItemDropContainer(randomWarpPageItemDrop, WEIGHT);
    }

    private static void injectWarpPage(ItemDropList dropList, ItemDropContainer warpPageContainer) {
        try {
            Field containerField = ItemDropList.class.getDeclaredField("container");
            containerField.setAccessible(true);
            ItemDropContainer container = (ItemDropContainer) containerField.get(dropList);

            if (container instanceof MultipleItemDropContainer multiContainer) {
                addItemToMultiContainer(multiContainer, warpPageContainer);
                LOGGER.atInfo().log("Added Warpbook Loot entry to: " + dropList.getId());
            }
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("Something went wrong while adding custom drop list entry to " + dropList.getId());
        }
    }

    private static void addItemToMultiContainer(MultipleItemDropContainer multiContainer, ItemDropContainer newItem) throws NoSuchFieldException, IllegalAccessException {
        Field containersField = MultipleItemDropContainer.class.getDeclaredField("containers");
        containersField.setAccessible(true);

        ItemDropContainer[] existingContainers = (ItemDropContainer[]) containersField.get(multiContainer);
        List<ItemDropContainer> containersList = new ArrayList<>(List.of(existingContainers));
        containersList.add(newItem);

        containersField.set(multiContainer, containersList.toArray(new ItemDropContainer[0]));
    }
}
