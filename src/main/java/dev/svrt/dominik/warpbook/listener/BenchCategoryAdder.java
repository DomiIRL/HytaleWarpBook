package dev.svrt.dominik.warpbook.listener;

import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.bench.CraftingBench;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.event.events.BootEvent;

import java.lang.reflect.Field;

import static dev.svrt.dominik.warpbook.WarpBookMod.LOGGER;

public class BenchCategoryAdder {

    public static void onBoot(BootEvent ignored) {
        try {
            Item arcane = Item.getAssetMap().getAsset("Bench_Arcane");
            assert arcane != null;
            String blockId = arcane.getBlockId();
            BlockType block = BlockType.getAssetMap().getAsset(blockId);
            CraftingBench bench = (CraftingBench) block.getBench();
            CraftingBench.BenchCategory[] categories = bench.getCategories();

            Class<? extends CraftingBench> benchClass = bench.getClass();
            Field categoriesField = benchClass.getDeclaredField("categories");
            categoriesField.setAccessible(true);
            CraftingBench.BenchCategory[] newCategories = new CraftingBench.BenchCategory[categories.length + 1];
            System.arraycopy(categories, 0, newCategories, 0, categories.length);
            newCategories[categories.length] = new CraftingBench.BenchCategory("AWB_Warp_Book", "awb.benchCategories.warpBook", "Icons/ItemsGenerated/AWB_Warp_Book.png", new CraftingBench.BenchItemCategory[0]);
            categoriesField.set(bench, newCategories);
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("Something went wrong while adding custom bench category with WarpBook recipes.");
        }
    }

}
