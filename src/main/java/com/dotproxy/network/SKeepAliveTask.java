package com.dotproxy.network;

import com.dotproxy.network.server.ServerSession;
import com.dotproxy.util.config.Config.Client.KeepAliveHandling.KeepAliveMode;
import org.geysermc.mcprotocollib.protocol.data.ProtocolState;
import org.geysermc.mcprotocollib.protocol.packet.common.clientbound.ClientboundKeepAlivePacket;

import static com.dotproxy.Globals.CONFIG;

public class SKeepAliveTask implements Runnable {
    private final ServerSession session;

    public SKeepAliveTask(ServerSession session) {
        this.session = session;
    }

    @Override
    public void run() {
        if (session.getChannel() == null || !session.getChannel().isActive()) {
            throw new RuntimeException("Task completed");
        }
        if (!session.isSpectator() && CONFIG.client.keepAliveHandling.keepAliveMode != KeepAliveMode.INDEPENDENT) return;
        var state = session.getPacketProtocol().getOutboundState();
        if (state != ProtocolState.CONFIGURATION && state != ProtocolState.GAME) return;
        this.session.send(new ClientboundKeepAlivePacket(System.currentTimeMillis()));
    }
}
