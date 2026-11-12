package com.dotproxy.network.client.handler.incoming;

import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.PacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.login.clientbound.ClientboundCustomQueryPacket;
import org.geysermc.mcprotocollib.protocol.packet.login.serverbound.ServerboundCustomQueryAnswerPacket;

public class CCustomQueryHandler implements PacketHandler<ClientboundCustomQueryPacket, ClientSession> {
    @Override
    public ClientboundCustomQueryPacket apply(final ClientboundCustomQueryPacket packet, final ClientSession session) {
        session.send(new ServerboundCustomQueryAnswerPacket(packet.getMessageId()));
        return null;
    }
}
