package com.dotproxy.network.server.handler.shared.incoming;

import com.dotproxy.event.player.PlayerConfigurationEvent;
import com.dotproxy.network.SKeepAliveTask;
import com.dotproxy.network.codec.PacketHandler;
import com.dotproxy.network.server.ServerSession;
import com.dotproxy.network.server.handler.ProxyServerLoginHandler;
import org.geysermc.mcprotocollib.protocol.data.ProtocolState;
import org.geysermc.mcprotocollib.protocol.packet.configuration.serverbound.ServerboundFinishConfigurationPacket;

import java.util.concurrent.TimeUnit;

import static com.dotproxy.Globals.EVENT_BUS;

public class FinishConfigurationHandler implements PacketHandler<ServerboundFinishConfigurationPacket, ServerSession> {
    @Override
    public ServerboundFinishConfigurationPacket apply(final ServerboundFinishConfigurationPacket packet, final ServerSession session) {
        session.switchInboundState(ProtocolState.GAME);
        session.setClientLoaded(false);
        if (!session.isConfigured()) {
            ProxyServerLoginHandler.INSTANCE.loggedIn(session);
            session.getEventLoop().scheduleAtFixedRate(new SKeepAliveTask(session), 0, 2, TimeUnit.SECONDS);
            return null;
        }
        EVENT_BUS.post(new PlayerConfigurationEvent.Exited(session));
        return packet;
    }
}
