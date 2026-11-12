package com.dotproxy.network.server.handler.spectator.outgoing;

import com.dotproxy.network.codec.PacketHandler;
import com.dotproxy.network.server.ServerSession;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundStartConfigurationPacket;

import static com.dotproxy.Globals.SERVER_LOG;

public class StartConfigurationSpectatorOutgoingHandler implements PacketHandler<ClientboundStartConfigurationPacket, ServerSession> {
    @Override
    public ClientboundStartConfigurationPacket apply(final ClientboundStartConfigurationPacket packet, final ServerSession session) {
        if (session.isConfigured()) {
            if (session.canTransfer()) {
                SERVER_LOG.info("Reconnecting spectator: {} because client is switching servers", session.getName());
                session.transferToSpectator();
            } else {
                session.disconnect("Client is switching servers");
            }
            return null;
        }
        return packet;
    }
}
