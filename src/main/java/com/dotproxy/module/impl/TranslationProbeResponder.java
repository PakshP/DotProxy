package com.dotproxy.module.impl;

import com.dotproxy.Proxy;
import com.dotproxy.event.client.ClientBotTick;
import com.dotproxy.event.client.ClientDisconnectEvent;
import com.dotproxy.mc.language.VanillaTextRenderer;
import com.dotproxy.module.api.Module;
import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.PacketHandlerCodec;
import com.dotproxy.network.codec.PacketHandlerStateCodec;
import com.github.rfresh2.EventConsumer;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.viaversion.nbt.io.MNBTIO;
import com.viaversion.nbt.tag.ByteTag;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import net.kyori.adventure.text.Component;
import org.geysermc.mcprotocollib.protocol.data.ProtocolState;
import org.geysermc.mcprotocollib.protocol.data.game.entity.attribute.AttributeType;
import org.geysermc.mcprotocollib.protocol.data.game.inventory.ContainerType;
import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponentTypes;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockEntityInfo;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockEntityType;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundLoginPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundRespawnPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundStartConfigurationPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerCombatKillPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundContainerClosePacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundContainerSetContentPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundContainerSetSlotPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundOpenBookPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundOpenScreenPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundOpenSignEditorPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.inventory.ServerboundContainerClosePacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.inventory.ServerboundRenameItemPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.level.ServerboundSignUpdatePacket;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.dotproxy.Globals.BOT;
import static com.dotproxy.Globals.CACHE;
import static com.dotproxy.Globals.CONFIG;
import static com.github.rfresh2.EventConsumer.of;
import static net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson;

/**
 * Answers translation-key probes the way a vanilla client would while no player is controlling.
 *
 * Servers detect clients/mods by putting translatable or keybind text on a sign (then opening the sign editor) or in
 * an anvil item's custom name. A vanilla client resolves the text and sends it back: on closing the sign editor
 * (ServerboundSignUpdate) or immediately when the anvil input changes (ServerboundRenameItem). Without this module a
 * headless proxy never replies, which is itself detectable.
 *
 * While a player is controlling, the probe packets are forwarded untouched and the player's client answers.
 *
 * Behavior mirrors the vanilla client (verified against 26.2):
 * - ClientPacketListener.handleOpenSignEditor ignores the request if there is no sign block entity at the position
 * - the sign editor reads its lines when opened and sends them in removed(): whenever it is replaced by another screen
 *   (open screen / book / death / level change) or closed (container close), or when isValid() fails on tick
 *   (sign removed, or the player is out of block interaction range + 4)
 * - AnvilScreen sets the name box to the input's hover name when slot 0 changes, blanks it unless the item has a
 *   custom name, and AnvilMenu.setItemName sends a rename only for a valid (filtered, <=50 chars) changed name
 *
 * All module state is only touched on the client event loop, ordered with the cache updates.
 */
public class TranslationProbeResponder extends Module {
    private static final int ANVIL_INPUT_SLOT = 0;
    private static final int ANVIL_MAX_NAME_LENGTH = 50;
    private static final double SIGN_EDIT_EXTRA_RANGE = 4.0;

    private record OpenSignEditor(int x, int y, int z, boolean frontText, String[] lines) { }

    private static final class OpenAnvil {
        final int containerId;
        @Nullable ItemStack lastInput = null; // AbstractContainerMenu.lastSlots[0]
        @Nullable String itemName = null;     // AnvilMenu.itemName

        OpenAnvil(final int containerId) {
            this.containerId = containerId;
        }
    }

    private @Nullable OpenSignEditor openSign = null;
    private @Nullable OpenAnvil openAnvil = null;

    @Override
    public boolean enabledSetting() {
        return CONFIG.client.extra.translationProbes.enabled;
    }

    @Override
    public List<EventConsumer<?>> registerEvents() {
        return List.of(
            of(ClientBotTick.class, e -> onEventLoop(this::tickOpenSign)),
            of(ClientBotTick.Stopped.class, e -> onEventLoop(() -> closeSign("player took control"))),
            of(ClientDisconnectEvent.class, e -> onEventLoop(this::reset))
        );
    }

    @Override
    public void onDisable() {
        onEventLoop(this::reset);
    }

    @Override
    public PacketHandlerCodec registerClientPacketHandlerCodec() {
        return PacketHandlerCodec.clientBuilder()
            .setId("translation-probe-responder")
            .setPriority(10) // see packets before the main client codec
            .state(ProtocolState.GAME, PacketHandlerStateCodec.clientBuilder()
                .inbound(ClientboundOpenSignEditorPacket.class, this::onOpenSignEditor)
                .inbound(ClientboundContainerClosePacket.class, (p, s) -> sync(p, s, () -> {
                    closeSign("container close");
                    closeAnvil(p.getContainerId());
                }))
                .inbound(ClientboundOpenScreenPacket.class, (p, s) -> sync(p, s, () -> onOpenScreen(p)))
                .inbound(ClientboundContainerSetContentPacket.class, (p, s) -> sync(p, s, () -> {
                    final var items = p.getItems();
                    if (items.length > ANVIL_INPUT_SLOT) onAnvilInput(p.getContainerId(), items[ANVIL_INPUT_SLOT]);
                }))
                .inbound(ClientboundContainerSetSlotPacket.class, (p, s) -> sync(p, s, () -> {
                    if (p.getSlot() == ANVIL_INPUT_SLOT) onAnvilInput(p.getContainerId(), p.getItem());
                }))
                .inbound(ClientboundOpenBookPacket.class, (p, s) -> sync(p, s, () -> closeSign("book opened")))
                .inbound(ClientboundRespawnPacket.class, (p, s) -> sync(p, s, () -> closeSign("respawn")))
                .inbound(ClientboundPlayerCombatKillPacket.class, (p, s) -> sync(p, s, () -> {
                    if (p.getPlayerId() == CACHE.getPlayerCache().getEntityId()) closeSign("death screen");
                }))
                // the connection leaves the game state: vanilla can't send game packets anymore
                .inbound(ClientboundStartConfigurationPacket.class, (p, s) -> sync(p, s, this::reset))
                .inbound(ClientboundLoginPacket.class, (p, s) -> sync(p, s, this::reset))
                // the proxy itself closed the container (e.g. an inventory action)
                .outbound(ServerboundContainerClosePacket.class, (p, s) -> sync(p, s, () -> closeAnvil(p.getContainerId())))
                .build())
            .build();
    }

    /* ---------------------------------------- signs ---------------------------------------- */

    private ClientboundOpenSignEditorPacket onOpenSignEditor(final ClientboundOpenSignEditorPacket packet, final ClientSession session) {
        if (!CONFIG.client.extra.translationProbes.signs || Proxy.getInstance().hasActivePlayer()) {
            return packet; // forwarded: the controlling player's client answers
        }
        session.executeInEventLoop(() -> {
            try {
                openSignEditor(packet, session);
            } catch (final Throwable e) {
                error("Failed handling sign editor probe", e);
            }
        });
        return null;
    }

    private void openSignEditor(final ClientboundOpenSignEditorPacket packet, final ClientSession session) {
        final var blockEntity = CACHE.getChunkCache().getBlockEntity(packet.getX(), packet.getY(), packet.getZ());
        if (!isSign(blockEntity)) {
            // vanilla: "Ignoring openTextEdit on an invalid entity" - no screen, no reply
            debug("Ignoring sign editor open at [{}, {}, {}]: no sign block entity", packet.getX(), packet.getY(), packet.getZ());
            return;
        }
        final var renderer = VanillaTextRenderer.forProtocol(session.getProtocolVersion().getVersion());
        final String[] lines = renderSignLines(readNbt(blockEntity), packet.isFrontText(), renderer);
        // opening a screen replaces the current one, which sends the previous editor's text first
        closeSign("replaced by another sign editor");
        openSign = new OpenSignEditor(packet.getX(), packet.getY(), packet.getZ(), packet.isFrontText(), lines);
        // a player attached between the packet arriving and now: they never saw this editor, close it like they would
        if (Proxy.getInstance().hasActivePlayer()) closeSign("player took control");
    }

    private void tickOpenSign() {
        final var sign = openSign;
        if (sign == null) return;
        if (!isSign(CACHE.getChunkCache().getBlockEntity(sign.x, sign.y, sign.z))) {
            closeSign("sign removed");
        } else if (tooFarToEdit(sign)) {
            closeSign("out of range");
        }
    }

    // Player.isWithinBlockInteractionRange(pos, 4.0): eye -> block AABB distance
    private boolean tooFarToEdit(final OpenSignEditor sign) {
        final var player = CACHE.getPlayerCache();
        final double range = BOT.getAttributeValue(AttributeType.Builtin.BLOCK_INTERACTION_RANGE, 4.5f) + SIGN_EDIT_EXTRA_RANGE;
        final double dx = axisDistance(player.getX(), sign.x);
        final double dy = axisDistance(player.getEyeY(), sign.y);
        final double dz = axisDistance(player.getZ(), sign.z);
        return dx * dx + dy * dy + dz * dz >= range * range;
    }

    private static double axisDistance(final double pos, final int blockMin) {
        return Math.max(Math.max(blockMin - pos, pos - (blockMin + 1)), 0.0);
    }

    private void closeSign(final String reason) {
        final var sign = openSign;
        if (sign == null) return;
        openSign = null;
        if (!Proxy.getInstance().isConnected()) return;
        sendClientPacketAsync(new ServerboundSignUpdatePacket(sign.x, sign.y, sign.z, sign.lines, sign.frontText));
        if (CONFIG.client.extra.translationProbes.logProbes) {
            info("Answered sign probe at [{}, {}, {}] ({}): {}", sign.x, sign.y, sign.z, reason, Arrays.toString(sign.lines));
        }
    }

    private static boolean isSign(final @Nullable BlockEntityInfo blockEntity) {
        return blockEntity != null
            && (blockEntity.getType() == BlockEntityType.SIGN || blockEntity.getType() == BlockEntityType.HANGING_SIGN);
    }

    private @Nullable Tag readNbt(final BlockEntityInfo blockEntity) {
        if (blockEntity.getNbt() == null) return null;
        try {
            return MNBTIO.read(blockEntity.getNbt());
        } catch (final Exception e) {
            warn("Failed reading sign NBT", e);
            return null;
        }
    }

    /**
     * SignBlockEntity.getText(front).getMessage(i, false).getString() for i in 0..3
     */
    public static String[] renderSignLines(final @Nullable Tag nbt, final boolean frontText, final VanillaTextRenderer renderer) {
        final String[] lines = {"", "", "", ""};
        if (!(nbt instanceof CompoundTag root)) return lines;
        final CompoundTag text = root.getCompoundTag(frontText ? "front_text" : "back_text");
        if (text == null) return lines;
        final ListTag<?> messages = text.getListTag("messages");
        if (messages == null) return lines;
        for (int i = 0; i < lines.length && i < messages.size(); i++) {
            lines[i] = renderer.render(parseComponent(messages.get(i)));
        }
        return lines;
    }

    // <=1.21.4: JSON strings. 1.21.5+ (if ever received unconverted): NBT components
    private static Component parseComponent(final Tag tag) {
        try {
            if (tag instanceof StringTag s) {
                try {
                    return gson().deserialize(s.getValue());
                } catch (final JsonParseException | IllegalStateException e) {
                    return Component.text(s.getValue());
                }
            }
            return gson().deserializeFromTree(toJson(tag));
        } catch (final Exception e) {
            return Component.empty();
        }
    }

    private static JsonElement toJson(final Tag tag) {
        if (tag instanceof StringTag s) return new JsonPrimitive(s.getValue());
        if (tag instanceof ByteTag b) return new JsonPrimitive(b.asByte() != 0); // nbt booleans
        if (tag instanceof NumberTag n) return new JsonPrimitive(n.getValue());
        if (tag instanceof ListTag<?> list) {
            final var arr = new JsonArray();
            for (Tag t : list) arr.add(toJson(t));
            return arr;
        }
        if (tag instanceof CompoundTag compound) {
            final var obj = new JsonObject();
            for (var entry : compound) obj.add(entry.getKey(), toJson(entry.getValue()));
            return obj;
        }
        return JsonNull.INSTANCE;
    }

    /* ---------------------------------------- anvils ---------------------------------------- */

    private void onOpenScreen(final ClientboundOpenScreenPacket packet) {
        closeSign("replaced by container screen");
        openAnvil = packet.getType() == ContainerType.ANVIL ? new OpenAnvil(packet.getContainerId()) : null;
    }

    private void closeAnvil(final int containerId) {
        if (openAnvil != null && openAnvil.containerId == containerId) openAnvil = null;
    }

    // AbstractContainerMenu.triggerSlotListeners -> AnvilScreen.slotChanged -> onNameChanged -> AnvilMenu.setItemName
    private void onAnvilInput(final int containerId, final @Nullable ItemStack item) {
        final var anvil = openAnvil;
        if (anvil == null || anvil.containerId != containerId) return;
        final @Nullable ItemStack input = isEmpty(item) ? null : item;
        if (Objects.equals(anvil.lastInput, input)) return; // ItemStack.matches: no change, no listener call
        anvil.lastInput = input;
        if (input == null) return; // name box set to "", but onNameChanged returns early on an empty slot
        if (!CONFIG.client.extra.translationProbes.anvil || Proxy.getInstance().hasActivePlayer()) return;

        final Component customName = input.getDataComponentsOrEmpty().get(DataComponentTypes.CUSTOM_NAME);
        // hover name is only kept when it is a custom name, otherwise onNameChanged blanks it
        final String name;
        if (customName != null) {
            final var session = Proxy.getInstance().getClient();
            if (session == null) return;
            name = VanillaTextRenderer.forProtocol(session.getProtocolVersion().getVersion()).render(customName);
        } else {
            name = "";
        }
        final String validated = VanillaTextRenderer.filterText(name);
        if (validated.length() > ANVIL_MAX_NAME_LENGTH) return; // validateName -> null: no rename sent
        if (validated.equals(anvil.itemName)) return;
        anvil.itemName = validated;
        sendClientPacketAsync(new ServerboundRenameItemPacket(validated));
        if (CONFIG.client.extra.translationProbes.logProbes && customName != null) {
            info("Answered anvil probe: \"{}\"", validated);
        }
    }

    private static boolean isEmpty(final @Nullable ItemStack item) {
        return item == null || item.getId() == 0 || item.getAmount() <= 0;
    }

    /* ---------------------------------------- util ---------------------------------------- */

    private void reset() {
        openSign = null;
        openAnvil = null;
    }

    private <P> P sync(final P packet, final ClientSession session, final Runnable action) {
        session.executeInEventLoop(() -> {
            try {
                action.run();
            } catch (final Throwable e) {
                error("Error handling {}", packet.getClass().getSimpleName(), e);
            }
        });
        return packet;
    }

    private void onEventLoop(final Runnable action) {
        final var session = Proxy.getInstance().getClient();
        if (session == null) {
            action.run();
            return;
        }
        session.executeInEventLoop(() -> {
            try {
                action.run();
            } catch (final Throwable e) {
                error("Translation probe responder error", e);
            }
        });
    }
}
