package com.dotproxy.network.client.handler.incoming;

import com.dotproxy.Proxy;
import com.dotproxy.event.client.ClientConfigurationEvent;
import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.PacketHandler;
import org.geysermc.mcprotocollib.protocol.data.ProtocolState;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundStartConfigurationPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundConfigurationAcknowledgedPacket;

import static com.dotproxy.Globals.EVENT_BUS;

public class CStartConfigurationHandler implements PacketHandler<ClientboundStartConfigurationPacket, ClientSession> {
    @Override
    public ClientboundStartConfigurationPacket apply(final ClientboundStartConfigurationPacket packet, final ClientSession session) {
        EVENT_BUS.post(ClientConfigurationEvent.Entering.INSTANCE);
        session.setOnline(false);
        session.switchInboundState(ProtocolState.CONFIGURATION);
        if (!Proxy.getInstance().hasActivePlayer()) {
            session.send(new ServerboundConfigurationAcknowledgedPacket());
            return null;
        }
        return packet;
    }
}
