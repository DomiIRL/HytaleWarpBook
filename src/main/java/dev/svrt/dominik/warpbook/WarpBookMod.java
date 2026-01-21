package dev.svrt.dominik.warpbook;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.plugin.registry.CodecMapRegistry;
import dev.svrt.dominik.warpbook.common.TeleportationStorage;
import dev.svrt.dominik.warpbook.interactions.OpenWarpBookInteraction;
import dev.svrt.dominik.warpbook.interactions.TeleportWarpPageInteraction;
import dev.svrt.dominik.warpbook.systems.TeleportCancelSystem;
import dev.svrt.dominik.warpbook.ui.BindWarpPageUISupplier;
import dev.svrt.dominik.warpbook.ui.WarpBookUISupplier;

import javax.annotation.Nonnull;
import java.util.logging.Level;

public class WarpBookMod extends JavaPlugin {

    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static WarpBookMod instance;

    private TeleportationStorage teleportationStorage;

    public WarpBookMod(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
    }

    @Override
    protected void setup() {
        this.teleportationStorage = new TeleportationStorage();

        CodecMapRegistry.Assets<Interaction, ?> interactionRegistry = getCodecRegistry(Interaction.CODEC);
        interactionRegistry.register("TeleportWarpPageInteraction", TeleportWarpPageInteraction.class, TeleportWarpPageInteraction.CODEC);
        interactionRegistry.register("OpenWarpBookInteraction", OpenWarpBookInteraction.class, OpenWarpBookInteraction.CODEC);

        getCodecRegistry(OpenCustomUIInteraction.PAGE_CODEC).register("Warp_Book_UI", WarpBookUISupplier.class, WarpBookUISupplier.CODEC);
        getCodecRegistry(OpenCustomUIInteraction.PAGE_CODEC).register("Bind_Warp_Page_UI", BindWarpPageUISupplier.class, BindWarpPageUISupplier.CODEC);

        getEntityStoreRegistry().registerSystem(new TeleportCancelSystem());

        LOGGER.at(Level.INFO).log("Warp Book plugin loaded!");
    }

    @Override
    protected void shutdown() {
        teleportationStorage.shutdown();
    }

    public TeleportationStorage getTeleportationStorage() {
        return teleportationStorage;
    }

    public static WarpBookMod getInstance() {
        return instance;
    }
}