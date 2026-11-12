package com.dotproxy.network.server.handler.spectator.incoming;

import com.dotproxy.network.codec.PacketHandler;
import com.dotproxy.network.server.ServerSession;
import org.geysermc.mcprotocollib.protocol.packet.common.serverbound.ServerboundKeepAlivePacket;

public class KeepAliveSpectatorHandler implements PacketHandler<ServerboundKeepAlivePacket, ServerSession> {
    public static final KeepAliveSpectatorHandler INSTANCE = new KeepAliveSpectatorHandler();
    @Override
    public ServerboundKeepAlivePacket apply(final ServerboundKeepAlivePacket packet, final ServerSession session) {
        if (packet.getPingId() == session.getLastKeepAliveId()) {
            session.setPing(System.currentTimeMillis() - session.getLastKeepAliveTime());
        }
        return null;
    }
}
