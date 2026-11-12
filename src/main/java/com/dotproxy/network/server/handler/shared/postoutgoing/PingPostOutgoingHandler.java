package com.dotproxy.network.server.handler.shared.postoutgoing;

import com.dotproxy.network.codec.PostOutgoingPacketHandler;
import com.dotproxy.network.server.ServerSession;
import org.geysermc.mcprotocollib.protocol.packet.common.clientbound.ClientboundPingPacket;

public class PingPostOutgoingHandler implements PostOutgoingPacketHandler<ClientboundPingPacket, ServerSession> {
    @Override
    public void accept(final ClientboundPingPacket packet, final ServerSession session) {
        session.setLastPingId(packet.getId());
        session.setLastPingTime(System.currentTimeMillis());
    }
}
