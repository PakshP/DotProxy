package com.dotproxy.network.client.handler.incoming;

import com.dotproxy.Proxy;
import com.dotproxy.event.client.ClientConfigurationEvent;
import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.PacketHandler;
import org.geysermc.mcprotocollib.protocol.data.ProtocolState;
import org.geysermc.mcprotocollib.protocol.packet.configuration.clientbound.ClientboundFinishConfigurationPacket;
import org.geysermc.mcprotocollib.protocol.packet.configuration.serverbound.ServerboundFinishConfigurationPacket;

import static com.dotproxy.Globals.EVENT_BUS;

public class CFinishConfigurationHandler implements PacketHandler<ClientboundFinishConfigurationPacket, ClientSession> {
    @Override
    public ClientboundFinishConfigurationPacket apply(final ClientboundFinishConfigurationPacket packet, final ClientSession session) {
        EVENT_BUS.post(ClientConfigurationEvent.Exiting.INSTANCE);
        session.switchInboundState(ProtocolState.GAME);
        if (!Proxy.getInstance().hasActivePlayer()) {
            session.send(new ServerboundFinishConfigurationPacket());
            return null;
        }
        return packet;
    }
}
