package dev.svrt.dominik.warpbook.window;

import com.hypixel.hytale.server.core.entity.entities.player.windows.ItemStackContainerWindow;
import com.hypixel.hytale.server.core.inventory.container.ItemStackItemContainer;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class WarpBookContainerWindow extends ItemStackContainerWindow {
  public WarpBookContainerWindow(@NonNullDecl ItemStackItemContainer itemStackItemContainer) {
    super(itemStackItemContainer);
  }
}
