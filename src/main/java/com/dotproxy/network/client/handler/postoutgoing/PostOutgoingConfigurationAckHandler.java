package com.dotproxy.network.client.handler.postoutgoing;

import com.dotproxy.cache.CacheResetType;
import com.dotproxy.event.client.ClientConfigurationEvent;
import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.PostOutgoingPacketHandler;
import org.geysermc.mcprotocollib.protocol.data.ProtocolState;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundConfigurationAcknowledgedPacket;

import static com.dotproxy.Globals.CACHE;
import static com.dotproxy.Globals.EVENT_BUS;

public class PostOutgoingConfigurationAckHandler implements PostOutgoingPacketHandler<ServerboundConfigurationAcknowledgedPacket, ClientSession> {
    @Override
    public void accept(final ServerboundConfigurationAcknowledgedPacket packet, final ClientSession session) {
        session.getPacketProtocol().setOutboundState(ProtocolState.CONFIGURATION); // GAME -> CONFIGURATION
        CACHE.reset(CacheResetType.PROTOCOL_SWITCH);
        EVENT_BUS.post(ClientConfigurationEvent.Entered.INSTANCE);
    }
}
