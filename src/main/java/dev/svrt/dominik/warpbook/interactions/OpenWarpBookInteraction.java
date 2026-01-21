package dev.svrt.dominik.warpbook.interactions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.windows.ItemStackContainerWindow;
import com.hypixel.hytale.server.core.entity.entities.player.windows.Window;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.ItemStackItemContainer;
import com.hypixel.hytale.server.core.inventory.container.filter.FilterActionType;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.svrt.dominik.warpbook.common.WarpPageBinding;
import dev.svrt.dominik.warpbook.ui.WarpBookUI;

import javax.annotation.Nonnull;
import java.util.UUID;

public class OpenWarpBookInteraction extends SimpleInstantInteraction {

    public static final BuilderCodec<OpenWarpBookInteraction> CODEC =
        BuilderCodec.builder(
            OpenWarpBookInteraction.class,
            OpenWarpBookInteraction::new,
            SimpleInstantInteraction.CODEC
        )
        .build();

    protected OpenWarpBookInteraction() {
        super();
    }

    @Nonnull
    @Override
    public WaitForDataFrom getWaitForDataFrom() {
        return WaitForDataFrom.Server;
    }

    @Override
    protected void firstRun(
        @Nonnull InteractionType type,
        @Nonnull InteractionContext context,
        @Nonnull CooldownHandler cooldownHandler
    ) {
        Ref<EntityStore> entityRef = context.getEntity();

        Player player = entityRef.getStore().getComponent(entityRef, Player.getComponentType());
        if (player == null) {
            context.getState().state = InteractionState.Failed;
            return;
        }

        ItemContainer hotbarContainer = context.getHeldItemContainer();
        short heldItemSlot = context.getHeldItemSlot();
        player.getWorld().execute(() -> {
            Ref<EntityStore> ref = player.getReference();
            if (ref == null || !ref.isValid()) {
                return;
            }
            Store<EntityStore> store = ref.getStore();

            if (hotbarContainer == null) {
                return;
            }

            ItemStackItemContainer container = ItemStackItemContainer.ensureContainer(hotbarContainer, heldItemSlot, (short) 27);
            if (container == null) {
                return;
            }
            for (short i = 0; i < 27; i++) {
                container.setSlotFilter(FilterActionType.ADD, i,
                  (_, _, _, itemStack) -> itemStack != null && itemStack.getFromMetadataOrNull(WarpPageBinding.KEYED_CODEC) != null);
            }

            UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
            if (uuidComponent == null) {
                return;
            }

            PlayerRef playerRef = Universe.get().getPlayer(uuidComponent.getUuid());
            if (playerRef == null) {
                return;
            }

            Window window = new ItemStackContainerWindow(container);
            player.getPageManager().setPageWithWindows(ref, store, Page.Bench, true, window);
        });

        context.getState().state = InteractionState.Finished;
    }

    @Override
    protected void simulateFirstRun(
        @Nonnull InteractionType type,
        @Nonnull InteractionContext context,
        @Nonnull CooldownHandler cooldownHandler
    ) {
    }

    @Nonnull
    @Override
    public String toString() {
        return "OpenWarpBookInteraction{} " + super.toString();
    }
}

