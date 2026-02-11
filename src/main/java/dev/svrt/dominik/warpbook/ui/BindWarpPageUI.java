package dev.svrt.dominik.warpbook.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.LocalizableString;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import dev.svrt.dominik.warpbook.WarpBookMod;
import dev.svrt.dominik.warpbook.data.WarpPageBindingType;
import dev.svrt.dominik.warpbook.services.WarpPageBindingService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static dev.svrt.dominik.warpbook.WarpBookMod.LOGGER;

public class BindWarpPageUI extends InteractiveCustomUIPage<BindWarpPageUI.BindWarpPageEventData> {

    public static final String TYPE_POSITION = "POSITION", TYPE_ENTITY = "ENTITY";
    public static final float ENTITY_RADIUS = 5;

    public String name = "";
    public String type = TYPE_POSITION;
    public String target = null;

    private InteractionContext context;

    public BindWarpPageUI(PlayerRef playerRef, InteractionContext context) {
        super(playerRef, CustomPageLifetime.CanDismiss, BindWarpPageEventData.CODEC);
        this.context = context;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commands,
                      @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        commands.append("Pages/AWB_BindWarpPage.ui");

        LinkedList<DropdownEntryInfo> typeEntries = new LinkedList<>();
        typeEntries.add(new DropdownEntryInfo(LocalizableString.fromMessageId("awb.customUI.warpPage.type.position"), TYPE_POSITION));
        typeEntries.add(new DropdownEntryInfo(LocalizableString.fromMessageId("awb.customUI.warpPage.type.entity"), TYPE_ENTITY));
        commands.set("#BindType.Entries", typeEntries);
        commands.set("#BindType.Value", type);

        events.addEventBinding(
          CustomUIEventBindingType.ValueChanged,
          "#BindType",
          EventData.of("@Type", "#BindType.Value"),
          false
        );

        this.buildTargetDropdown(ref, store, commands, events);

        events.addEventBinding(
          CustomUIEventBindingType.ValueChanged,
          "#Name",
          EventData.of("@Name", "#Name.Value"),
          false
        );

        events.addEventBinding(
          CustomUIEventBindingType.Activating,
          "#BindButton",
          EventData.of("Action", "confirm"),
          false
        );
    }

    private void buildTargetDropdown(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
                                     @Nonnull UICommandBuilder commands, @Nonnull UIEventBuilder events) {
        if (type == null || type.equals(TYPE_POSITION)) {
            commands.set("#TargetGroup.Visible", false);
        } else {
            LinkedList<DropdownEntryInfo> targetEntries = new LinkedList<>();

            Player player = store.getComponent(ref, Player.getComponentType());
            TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());
            if (player != null && transformComponent != null) {
                UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
                if (uuidComponent == null) {
                    LOGGER.atSevere().log("Failed to build target dropdown: UUIDComponent is null");
                    return;
                }
                targetEntries.add(new DropdownEntryInfo(LocalizableString.fromString(player.getDisplayName()), uuidComponent.getUuid().toString()));

                World world = player.getWorld();

                List<Ref<EntityStore>> entities = TargetUtil.getAllEntitiesInSphere(transformComponent.getPosition(), ENTITY_RADIUS, world.getEntityStore().getStore());
                for (Ref<EntityStore> entityRef : entities) {
                    try {
                        DropdownEntryInfo entry = this.createEntityTargetEntry(entityRef);
                        if (entry != null) {
                            targetEntries.add(entry);
                        }
                    } catch (Exception e) {
                        LOGGER.atSevere().withCause(e).log("Failed to create entity target entry");
                    }
                }
            }

            commands.set("#TargetGroup.Visible", true);
            commands.set("#BindTarget.Entries", targetEntries);

            events.addEventBinding(
              CustomUIEventBindingType.ValueChanged,
              "#BindTarget",
              EventData.of("@Target", "#BindTarget.Value"),
              false
            );
        }
    }

    @Nullable
    private DropdownEntryInfo createEntityTargetEntry(@Nonnull Ref<EntityStore> ref) {
        Store<EntityStore> store = ref.getStore();
        NPCEntity npcEntity = store.getComponent(ref, Objects.requireNonNull(NPCEntity.getComponentType()));
        if (npcEntity == null) {
            return null;
        }

        UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());

        if (uuidComponent == null) {
            LOGGER.atSevere().log("Failed to create entity target entry: UUIDComponent is null");
            return null;
        }

        if (npcEntity.getRole() != null) {
            return new DropdownEntryInfo(LocalizableString.fromMessageId(npcEntity.getRole().getNameTranslationKey()), uuidComponent.getUuid().toString());
        } else {
            LOGGER.atSevere().log("Failed to create entity target entry: missing role for NPCEntity");
        }

        return null;
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
                                @Nonnull BindWarpPageEventData data) {
        if (data.name != null) {
            this.name = data.name;
        } else if (data.type != null) {
            this.type = data.type;

            UICommandBuilder commandBuilder = new UICommandBuilder();
            UIEventBuilder eventBuilder = new UIEventBuilder();
            this.buildTargetDropdown(ref, store, commandBuilder, eventBuilder);
            this.sendUpdate(commandBuilder, eventBuilder, false);
        } else if (data.target != null) {
            this.target = data.target;
            LOGGER.atInfo().log("Selected target: " + this.target);
        } else if (this.name != null && !name.isBlank()) {
            WarpPageBindingService bindingService = WarpBookMod.get().getWarpPageBindingService();

            UUID targetUUID = null;
            if (type.equals(TYPE_ENTITY)) {
                try {
                    targetUUID = UUID.fromString(this.target);
                } catch (IllegalArgumentException e) {
                    LOGGER.atSevere().withCause(e).log("Invalid target UUID: " + this.target);
                    return;
                }
            }

            WarpPageBindingType bindingType = WarpPageBindingType.valueOf(type);

            boolean success = bindingService.bindHeldWarpPage(ref, store, context, this.name, bindingType, targetUUID);
            if (success) {
                close();
            }
        }
    }

    public static class BindWarpPageEventData {
        public static final BuilderCodec<BindWarpPageEventData> CODEC =
          BuilderCodec.builder(BindWarpPageEventData.class, BindWarpPageEventData::new)
            .append(new KeyedCodec<>("@Name", Codec.STRING), (c, v) -> c.name = v, c -> c.name)
            .add()
            .append(new KeyedCodec<>("@Type", Codec.STRING), (c, v) -> c.type = v, c -> c.type)
            .add()
            .append(new KeyedCodec<>("@Target", Codec.STRING), (c, v) -> c.target = v, c -> c.target)
            .add()
            .build();

        public String name;
        public String type;
        public String target;
    }
}
