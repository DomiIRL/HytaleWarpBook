package dev.svrt.dominik.warpbook.ui;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.svrt.dominik.warpbook.WarpBookMod;
import dev.svrt.dominik.warpbook.components.Teleporter;
import dev.svrt.dominik.warpbook.data.WarpPageBinding;
import com.hypixel.hytale.server.core.entity.entities.Player;

import javax.annotation.Nonnull;

public class TeleporterConfirmationUI extends InteractiveCustomUIPage<TeleporterConfirmationUI.EventData> {

    private final Ref<ChunkStore> blockRef;
    private final WarpPageBinding binding;
    private final short slot;

    public TeleporterConfirmationUI(PlayerRef playerRef, Ref<ChunkStore> blockRef, WarpPageBinding binding, short slot) {
        super(playerRef, CustomPageLifetime.CanDismiss, EventData.CODEC);
        this.blockRef = blockRef;
        this.binding = binding;
        this.slot = slot;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commands, @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        commands.append("TeleporterConfirmation.ui");

        events.on("ConfirmButton", (eventData, event) -> {
            ChunkStore chunkStore = blockRef.getStore();
            // We need to ensure we are on server thread or handling component change correctly.
            // Component updates should ideally happen via CommandBuffer or direct set if allowed.
            // Direct set is allowed in callbacks if they run in appropriate context.
            // InteractiveCustomUIPage events run on server tick usually.

            Teleporter teleporter = chunkStore.getComponent(blockRef, WarpBookMod.TELEPORTER_COMPONENT_TYPE);
            if (teleporter != null) {
                teleporter.setTransform(binding.transform);
                World targetWorld = Universe.get().getWorld(binding.world);
                if (targetWorld != null) {
                    teleporter.setWorldUuid(targetWorld.getRef().getUuid());
                }

                teleporter.setWarp(null);
                teleporter.setRelativeMask((byte)0);

                Player player = store.getComponent(ref, Player.getComponentType());
                if (player != null) {
                    ItemContainer inventory = player.getInventory();
                    inventory.setStack(slot, ItemStack.EMPTY);
                }
            }
            this.close();
        });

        events.on("CancelButton", (eventData, event) -> {
            this.close();
        });
    }

    public static class EventData implements com.hypixel.hytale.server.core.ui.builder.EventData {
        public static final BuilderCodec<EventData> CODEC = BuilderCodec.builder(EventData.class, EventData::new).build();
    }
}

