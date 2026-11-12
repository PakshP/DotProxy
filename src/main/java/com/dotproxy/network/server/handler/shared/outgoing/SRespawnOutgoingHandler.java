package com.dotproxy.network.server.handler.shared.outgoing;

import com.dotproxy.network.codec.PacketHandler;
import com.dotproxy.network.server.ServerSession;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundRespawnPacket;

public class SRespawnOutgoingHandler implements PacketHandler<ClientboundRespawnPacket, ServerSession> {
    @Override
    public ClientboundRespawnPacket apply(final ClientboundRespawnPacket packet, final ServerSession session) {
        session.setClientLoaded(false);
        return packet;
    }
}
