package dev.svrt.dominik.warpbook.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.ItemStackItemContainer;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.svrt.dominik.warpbook.WarpBookMod;
import dev.svrt.dominik.warpbook.data.WarpPageBinding;
import dev.svrt.dominik.warpbook.services.WarpBookService;
import dev.svrt.dominik.warpbook.services.WarpPageUsageService;

import javax.annotation.Nonnull;

public class WarpBookUI extends InteractiveCustomUIPage<WarpBookUI.WarpBookEventData> {

    private final ItemStackItemContainer container;

    public WarpBookUI(PlayerRef playerRef, InteractionContext context) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, WarpBookEventData.CODEC);

        ItemContainer itemContainer = context.getHeldItemContainer();
        if (itemContainer == null) {
            this.container = null;
            return;
        }
        WarpBookService warpBookService = WarpBookMod.get().getWarpBookService();
        this.container = warpBookService.ensureWarpBookContainer(itemContainer, context.getHeldItemSlot());
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commands,
                      @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        commands.append("Pages/AWB_WarpBook.ui");

        commands.clear("#WarpList");

        if (container == null) {
            playerRef.sendMessage(Message.raw("You need to hold a book in your hand!"));
            return;
        }

        int uiIndex = 0;
        for (short i = 0; i < container.getCapacity(); i++) {
            ItemStack warpPage = container.getItemStack(i);

            if (warpPage == null || warpPage.isEmpty()) {
                continue;
            }

            WarpPageBinding pageBinding = warpPage.getFromMetadataOrNull(WarpPageBinding.KEYED_CODEC);
            if (pageBinding == null) {
                continue;
            }

            String selector = "#WarpList[" + uiIndex + "]";

            commands.append("#WarpList", "Pages/AWB_WarpPage.ui");

            String warpName = pageBinding.name != null ? pageBinding.name : "Ancient Destination";
            Transform transform = pageBinding.transform;

            String position = "Unknown";
            if (!pageBinding.random) {
                position = String.format("X: %.1f, Y: %.1f, Z: %.1f",
                  transform.getPosition().x, transform.getPosition().y, transform.getPosition().z);
            }

            commands.set(selector + " #Name.Text", warpName);
            commands.set(selector + " #Position.Text", position);

            events.addEventBinding(
              CustomUIEventBindingType.Activating,
              selector + " #TeleportButton",
              EventData.of("Slot", Integer.toString(i)),
              false
            );
            uiIndex++;
        }

        events.addEventBinding(CustomUIEventBindingType.Dismissing, "#Content");

    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
                                @Nonnull WarpBookEventData data) {
        if (data.slot != null) {
            try {
                short slotIndex = Short.parseShort(data.slot);
                teleportToWarp(ref, store, slotIndex);
            } catch (NumberFormatException e) {
                playerRef.sendMessage(Message.raw("Invalid slot number!"));
            }
        } else {
            playerRef.sendMessage(Message.raw("Unknown event data received!"));
        }
    }

    private void teleportToWarp(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, short slot) {
        ItemStack warpPage = container.getItemStack(slot);

        if (warpPage == null || warpPage.isEmpty() || !warpPage.getItemId().equals("Warp_Page_Bound")) {
            playerRef.sendMessage(Message.raw("Invalid warp page itemstack!"));
            sendUpdate();
            return;
        }

        WarpPageUsageService usageService = WarpBookMod.get().getWarpPageUsageService();
        boolean success = usageService.startTeleportPlayer(ref, store, warpPage, container, slot);
        if (success) {
            close();
        } else {
            sendUpdate();
        }
    }

    public static class WarpBookEventData {
        public static final BuilderCodec<WarpBookEventData> CODEC =
            BuilderCodec.builder(WarpBookEventData.class, WarpBookEventData::new)
                .append(new KeyedCodec<>("Slot", Codec.STRING),
                    (d, v) -> d.slot = v, d -> d.slot)
                .add()
                .build();

        public String slot;
    }
}
