package dev.svrt.dominik.warpbook;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.plugin.registry.CodecMapRegistry;
import dev.svrt.dominik.warpbook.interactions.OpenWarpBookInteraction;
import dev.svrt.dominik.warpbook.interactions.TeleportWarpPageInteraction;
import dev.svrt.dominik.warpbook.ui.BindWarpPageUISupplier;
import dev.svrt.dominik.warpbook.ui.WarpBookUISupplier;

import javax.annotation.Nonnull;

public class WarpBookMod extends JavaPlugin {

    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public WarpBookMod(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        CodecMapRegistry.Assets<Interaction, ?> interactionRegistry = getCodecRegistry(Interaction.CODEC);
        interactionRegistry.register("TeleportWarpPageInteraction", TeleportWarpPageInteraction.class, TeleportWarpPageInteraction.CODEC);
        interactionRegistry.register("OpenWarpBookInteraction", OpenWarpBookInteraction.class, OpenWarpBookInteraction.CODEC);

        getCodecRegistry(OpenCustomUIInteraction.PAGE_CODEC).register("Warp_Book_UI", WarpBookUISupplier.class, WarpBookUISupplier.CODEC);
        getCodecRegistry(OpenCustomUIInteraction.PAGE_CODEC).register("Bind_Warp_Page_UI", BindWarpPageUISupplier.class, BindWarpPageUISupplier.CODEC);
    }
}
