package com.dotproxy.network.client.handler.incoming;

import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.PacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.common.clientbound.ClientboundCookieRequestPacket;

public class CCookieRequestHandler implements PacketHandler<ClientboundCookieRequestPacket, ClientSession> {
    @Override
    public ClientboundCookieRequestPacket apply(final ClientboundCookieRequestPacket packet, final ClientSession session) {
        return null;
    }
}
