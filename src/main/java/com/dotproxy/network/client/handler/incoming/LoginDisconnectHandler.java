package com.dotproxy.network.client.handler.incoming;

import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.PacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.login.clientbound.ClientboundLoginDisconnectPacket;

public class LoginDisconnectHandler implements PacketHandler<ClientboundLoginDisconnectPacket, ClientSession> {
    @Override
    public ClientboundLoginDisconnectPacket apply(final ClientboundLoginDisconnectPacket packet, final ClientSession session) {
        session.disconnect(packet.getReason());
        return null;
    }
}
