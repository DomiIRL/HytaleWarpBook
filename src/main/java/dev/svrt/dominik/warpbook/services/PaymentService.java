package dev.svrt.dominik.warpbook.services;

import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import dev.svrt.dominik.warpbook.WarpBookMod;
import dev.svrt.dominik.warpbook.config.TeleportationPrice;
import dev.svrt.dominik.warpbook.config.WarpBookConfig;

public class PaymentService {

    public boolean processPayment(Player player) {
        WarpBookConfig config = WarpBookMod.get().getConfig().get();
        return processPayment(player, config.getTeleportPrice());
    }

    public boolean processTeleporterBindingPayment(Player player) {
        WarpBookConfig config = WarpBookMod.get().getConfig().get();
        return processPayment(player, config.getTeleporterBindingPrice());
    }

    private boolean processPayment(Player player, TeleportationPrice price) {
        if (price == null || price.isFree() || player.getGameMode() == GameMode.Creative) {
            return true;
        }

        CombinedItemContainer inventory = player.getInventory().getCombinedEverything();
        assert price.getItemId() != null;
        ItemStack costItem = new ItemStack(price.getItemId(), price.getAmount());
        ItemStackTransaction transaction = inventory.removeItemStack(costItem);

        if (!transaction.succeeded()) {
            Message first = Message.raw("You need ");
            Message item = Message.translation(costItem.getItem().getTranslationKey());
            Message second = Message.raw(" ");
            Message amountMsg = Message.raw(String.valueOf(price.getAmount()));
            player.sendMessage(Message.join(first, amountMsg, second, item));
            return false;
        }

        return true;
    }
}
