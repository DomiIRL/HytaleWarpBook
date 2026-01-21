package dev.svrt.dominik.warpbook;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.plugin.registry.CodecMapRegistry;
import com.hypixel.hytale.server.core.util.Config;
import dev.svrt.dominik.warpbook.config.WarpBookConfig;
import dev.svrt.dominik.warpbook.interactions.OpenWarpBookInteraction;
import dev.svrt.dominik.warpbook.interactions.TeleportWarpPageInteraction;
import dev.svrt.dominik.warpbook.services.PaymentService;
import dev.svrt.dominik.warpbook.services.TeleportationService;
import dev.svrt.dominik.warpbook.systems.TeleportCancelSystem;
import dev.svrt.dominik.warpbook.ui.BindWarpPageUISupplier;
import dev.svrt.dominik.warpbook.ui.WarpBookUISupplier;

import javax.annotation.Nonnull;
import java.util.logging.Level;

public class WarpBookMod extends JavaPlugin {

    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static WarpBookMod instance;

    private final Config<WarpBookConfig> config;

    private TeleportationService teleportationService;
    private PaymentService paymentService;

    public WarpBookMod(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
        config = this.withConfig("WarpBookConfig", WarpBookConfig.CODEC);
    }

    @Override
    protected void setup() {
        this.config.save();

        this.teleportationService = new TeleportationService();
        this.paymentService = new PaymentService();

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
        teleportationService.shutdown();
    }

    public Config<WarpBookConfig> getConfig() {
        return config;
    }

    public TeleportationService getTeleportationService() {
        return teleportationService;
    }

    public PaymentService getPaymentService() {
        return paymentService;
    }

    public static WarpBookMod getInstance() {
        return instance;
    }
}