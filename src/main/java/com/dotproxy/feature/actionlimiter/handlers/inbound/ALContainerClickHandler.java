package com.dotproxy.feature.actionlimiter.handlers.inbound;

import com.dotproxy.network.codec.PacketHandler;
import com.dotproxy.network.server.ServerSession;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.inventory.ServerboundContainerClickPacket;

import static com.dotproxy.Globals.CONFIG;

public class ALContainerClickHandler implements PacketHandler<ServerboundContainerClickPacket, ServerSession> {
    @Override
    public ServerboundContainerClickPacket apply(final ServerboundContainerClickPacket packet, final ServerSession session) {
        if (!CONFIG.client.extra.actionLimiter.allowInventory) return null;
        return packet;
    }
}
