package dev.svrt.dominik.warpbook.services;

import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import dev.svrt.dominik.warpbook.WarpBookMod;
import dev.svrt.dominik.warpbook.config.WarpBookConfig;

public class PaymentService {

    public boolean processPayment(Player player) {
        WarpBookConfig config = WarpBookMod.get().getConfig().get();

        if (config.isFreeTeleport() || player.getGameMode() == GameMode.Creative) {
            return true;
        }

        CombinedItemContainer inventory = player.getInventory().getCombinedEverything();
        ItemStack costItem = new ItemStack(config.getCostItemId(), config.getCostItemAmount());
        ItemStackTransaction transaction = inventory.removeItemStack(costItem);

        if (!transaction.succeeded()) {
            Message first = Message.raw("You need ");
            Message item = Message.translation(costItem.getItem().getTranslationKey());
            Message second = Message.raw(" ");
            Message amount = Message.raw(String.valueOf(config.getCostItemAmount()));
            player.sendMessage(Message.join(first, amount, second, item));
            return false;
        }

        return true;
    }
}

