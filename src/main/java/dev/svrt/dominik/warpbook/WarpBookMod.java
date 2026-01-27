package dev.svrt.dominik.warpbook;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.event.events.BootEvent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.plugin.registry.CodecMapRegistry;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.util.Config;
import dev.svrt.dominik.warpbook.components.WarpPageTeleporter;
import dev.svrt.dominik.warpbook.config.WarpBookConfig;
import dev.svrt.dominik.warpbook.interactions.OpenWarpBookInteraction;
import dev.svrt.dominik.warpbook.interactions.TeleportWarpPageInteraction;
import dev.svrt.dominik.warpbook.interactions.WarpPageTeleporterInteraction;
import dev.svrt.dominik.warpbook.listener.RandomWarpPagesDropListAdder;
import dev.svrt.dominik.warpbook.services.*;
import dev.svrt.dominik.warpbook.systems.TeleportCancelSystem;
import dev.svrt.dominik.warpbook.ui.BindWarpPageUISupplier;
import dev.svrt.dominik.warpbook.ui.WarpBookUISupplier;
import dev.svrt.dominik.warpbook.ui.WarpPageTeleporterUISupplier;

import javax.annotation.Nonnull;

public class WarpBookMod extends JavaPlugin {

    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    public static ComponentType<ChunkStore, WarpPageTeleporter> TELEPORTER_COMPONENT_TYPE;

    private static WarpBookMod instance;

    private final Config<WarpBookConfig> config;

    private WarpPageBindingService warpPageBindingService;
    private WarpPageUsageService warpPageUsageService;
    private WarpBookService warpBookService;
    private TeleportationService teleportationService;
    private RandomDestinationService randomDestinationService;
    private PaymentService paymentService;

    public WarpBookMod(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
        config = this.withConfig("WarpBookConfig", WarpBookConfig.CODEC);
    }

    @Override
    protected void setup() {
        this.config.save();

        this.warpPageBindingService = new WarpPageBindingService();
        this.warpPageUsageService = new WarpPageUsageService();
        this.warpBookService = new WarpBookService();
        this.randomDestinationService = new RandomDestinationService();
        this.teleportationService = new TeleportationService();
        this.paymentService = new PaymentService();

        ComponentRegistryProxy<ChunkStore> chunkStoreRegistry = getChunkStoreRegistry();
        TELEPORTER_COMPONENT_TYPE = chunkStoreRegistry.registerComponent(WarpPageTeleporter.class, "AWBWarpPageTeleporter", WarpPageTeleporter.CODEC);

        CodecMapRegistry.Assets<Interaction, ?> interactionRegistry = getCodecRegistry(Interaction.CODEC);
        interactionRegistry.register("AWBTeleportWarpPage", TeleportWarpPageInteraction.class, TeleportWarpPageInteraction.CODEC);
        interactionRegistry.register("AWBOpenWarpBook", OpenWarpBookInteraction.class, OpenWarpBookInteraction.CODEC);
        interactionRegistry.register("AWBWarpPageTeleporter", WarpPageTeleporterInteraction.class, WarpPageTeleporterInteraction.CODEC);

        CodecMapRegistry<OpenCustomUIInteraction.CustomPageSupplier, Codec<? extends OpenCustomUIInteraction.CustomPageSupplier>> customPageRegistry = getCodecRegistry(OpenCustomUIInteraction.PAGE_CODEC);
        customPageRegistry.register("AWBWarpBook", WarpBookUISupplier.class, WarpBookUISupplier.CODEC);
        customPageRegistry.register("AWBBindWarpPage", BindWarpPageUISupplier.class, BindWarpPageUISupplier.CODEC);
        customPageRegistry.register("AWBWarpPageTeleporter", WarpPageTeleporterUISupplier.class, WarpPageTeleporterUISupplier.CODEC);

        getEventRegistry().registerGlobal(BootEvent.class, RandomWarpPagesDropListAdder::onBoot);

        LOGGER.atInfo().log("Warp Book plugin loaded!");
    }

    @Override
    protected void start() {
        getEntityStoreRegistry().registerSystem(new TeleportCancelSystem());

        LOGGER.atInfo().log("Warp Book plugin started!");
    }

    @Override
    protected void shutdown() {
        teleportationService.shutdown();
    }

    public Config<WarpBookConfig> getConfig() {
        return config;
    }

    public WarpPageBindingService getWarpPageBindingService() {
        return warpPageBindingService;
    }

    public WarpPageUsageService getWarpPageUsageService() {
        return warpPageUsageService;
    }

    public WarpBookService getWarpBookService() {
        return warpBookService;
    }

    public TeleportationService getTeleportationService() {
        return teleportationService;
    }

    public RandomDestinationService getRandomDestinationService() {
        return randomDestinationService;
    }

    public PaymentService getPaymentService() {
        return paymentService;
    }

    public static WarpBookMod getInstance() {
        return instance;
    }
}
