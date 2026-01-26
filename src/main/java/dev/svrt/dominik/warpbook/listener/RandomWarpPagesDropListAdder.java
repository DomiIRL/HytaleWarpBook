package dev.svrt.dominik.warpbook.listener;

import com.hypixel.hytale.server.core.asset.type.item.config.ItemDrop;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemDropList;
import com.hypixel.hytale.server.core.asset.type.item.config.container.ItemDropContainer;
import com.hypixel.hytale.server.core.asset.type.item.config.container.MultipleItemDropContainer;
import com.hypixel.hytale.server.core.asset.type.item.config.container.SingleItemDropContainer;
import com.hypixel.hytale.server.core.event.events.BootEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import dev.svrt.dominik.warpbook.data.WarpPageBinding;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dev.svrt.dominik.warpbook.WarpBookMod.LOGGER;

public class RandomWarpPagesDropListAdder {

  public static void onBoot(BootEvent event) {
    Map<String, ItemDropList> map = ItemDropList.getAssetMap().getAssetMap();

    WarpPageBinding binding = new WarpPageBinding();
    binding.random = true;
    binding.name = "Ancient Destination";

    ItemStack boundWarpPage = new ItemStack("Warp_Page_Bound", 1).withMetadata(WarpPageBinding.KEYED_CODEC, binding);
    ItemDrop warpPageBound = new ItemDrop("Warp_Page_Bound", boundWarpPage.getMetadata(), 1, 1);
    SingleItemDropContainer itemDropContainer = new SingleItemDropContainer(warpPageBound, 5);

    for (Map.Entry<String, ItemDropList> entry : map.entrySet()) {
      String list = entry.getKey();
      if (!list.startsWith("Zone")) {
        continue;
      }
      ItemDropList value = map.get(list);
      if (value == null) {
        LOGGER.atSevere().log("Could not find prefab drop list: " + list);
        continue;
      }
      try {
        Class<ItemDropList> dropListClass = ItemDropList.class;
        Field containerField = dropListClass.getDeclaredField("container");
        containerField.setAccessible(true);
        ItemDropContainer o = (ItemDropContainer) containerField.get(value);
        if (o instanceof MultipleItemDropContainer multiContainer) {
          Class<MultipleItemDropContainer> multiClass = MultipleItemDropContainer.class;
          Field containersField = multiClass.getDeclaredField("containers");
          containersField.setAccessible(true);
          ItemDropContainer[] containers = (ItemDropContainer[]) containersField.get(multiContainer);
          List<ItemDropContainer> containersList = new ArrayList<>(List.of(containers));
          containersList.add(itemDropContainer);
          containersField.set(multiContainer, containersList.toArray(new ItemDropContainer[0]));
          LOGGER.atInfo().log("Added Warpbook Loot entry to: " + value.getId());
        }
      } catch (Exception e) {
        LOGGER.atSevere().withCause(e).log("Something went wrong while adding custom drop list entry");
      }
    }
  }
}
