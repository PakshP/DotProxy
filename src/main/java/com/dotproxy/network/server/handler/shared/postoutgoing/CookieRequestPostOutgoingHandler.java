package com.dotproxy.network.server.handler.shared.postoutgoing;

import com.dotproxy.network.codec.PostOutgoingPacketHandler;
import com.dotproxy.network.server.ServerSession;
import org.geysermc.mcprotocollib.protocol.packet.common.clientbound.ClientboundCookieRequestPacket;

public class CookieRequestPostOutgoingHandler implements PostOutgoingPacketHandler<ClientboundCookieRequestPacket, ServerSession> {
    @Override
    public void accept(final ClientboundCookieRequestPacket packet, final ServerSession session) {
        session.getCookieCache().getRequestedCookies().add(packet.getKey());
    }
}
