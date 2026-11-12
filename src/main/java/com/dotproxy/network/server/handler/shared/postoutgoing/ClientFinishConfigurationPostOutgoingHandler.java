package com.dotproxy.network.server.handler.shared.postoutgoing;

import com.dotproxy.network.codec.PostOutgoingPacketHandler;
import com.dotproxy.network.server.ServerSession;
import org.geysermc.mcprotocollib.protocol.data.ProtocolState;
import org.geysermc.mcprotocollib.protocol.packet.configuration.clientbound.ClientboundFinishConfigurationPacket;

public class ClientFinishConfigurationPostOutgoingHandler implements PostOutgoingPacketHandler<ClientboundFinishConfigurationPacket, ServerSession> {
    @Override
    public void accept(final ClientboundFinishConfigurationPacket packet, final ServerSession session) {
        session.getPacketProtocol().setOutboundState(ProtocolState.GAME);
    }
}
