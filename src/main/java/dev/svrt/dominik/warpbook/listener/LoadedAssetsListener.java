package dev.svrt.dominik.warpbook.listener;

import com.hypixel.hytale.assetstore.event.LoadedAssetsEvent;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemDropList;
import com.hypixel.hytale.server.core.asset.type.item.config.container.ItemDropContainer;
import com.hypixel.hytale.server.core.asset.type.item.config.container.MultipleItemDropContainer;
import com.hypixel.hytale.server.core.event.events.BootEvent;
import dev.svrt.dominik.warpbook.data.WarpPageItemDropContainer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dev.svrt.dominik.warpbook.WarpBookMod.LOGGER;

public class LoadedAssetsListener {

  public static final String[] PREFAB_LISTS = {"Zone1_Encounters_Tier1"};

  public static void onLoadedAssets(BootEvent event) {
    Map<String, ItemDropList> map = ItemDropList.getAssetMap().getAssetMap();

    for (String list : PREFAB_LISTS) {
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
          containersList.add(new WarpPageItemDropContainer(5));
          containersField.set(multiContainer, containersList.toArray(new ItemDropContainer[0]));
          LOGGER.atInfo().log("Added Warpbook Loot entry to: " + value.getId());
        }
      } catch (Exception e) {
        LOGGER.atSevere().withCause(e).log("Something went wrong while adding custom drop list entry");
      }
    }
  }

}
