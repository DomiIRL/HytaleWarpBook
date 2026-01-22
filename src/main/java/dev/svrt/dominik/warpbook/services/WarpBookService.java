package dev.svrt.dominik.warpbook.services;

import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.ItemStackItemContainer;
import com.hypixel.hytale.server.core.inventory.container.filter.FilterActionType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WarpBookService {

  @Nullable
  public ItemStackItemContainer ensureWarpBookContainer(@Nonnull ItemContainer container, short slot) {
    ItemStackItemContainer itemStackItemContainer = ItemStackItemContainer.ensureContainer(container, slot, (short) 27);
    if (itemStackItemContainer == null) {
      return null;
    }
    for (short i = 0; i < 27; i++) {
      itemStackItemContainer.setSlotFilter(FilterActionType.ADD, i,
        (_, _, _, itemStack) -> itemStack != null && itemStack.getItemId().equals("Warp_Page_Bound"));
    }
    return itemStackItemContainer;
  }

}
