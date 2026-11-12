package com.dotproxy.network.server.handler.shared.incoming;

import com.dotproxy.network.codec.PacketHandler;
import com.dotproxy.network.server.ServerSession;
import org.geysermc.mcprotocollib.protocol.data.ProtocolState;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundConfigurationAcknowledgedPacket;

public class ConfigurationAckHandler implements PacketHandler<ServerboundConfigurationAcknowledgedPacket, ServerSession> {
    @Override
    public ServerboundConfigurationAcknowledgedPacket apply(final ServerboundConfigurationAcknowledgedPacket packet, final ServerSession session) {
        session.switchInboundState(ProtocolState.CONFIGURATION);
        session.setClientLoaded(false);
        return packet;
    }
}
