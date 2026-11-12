package com.dotproxy.network.server.handler.shared.incoming;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.dotproxy.network.codec.PacketHandler;
import com.dotproxy.network.server.ServerSession;
import com.dotproxy.network.server.DotProxyServerInfoBuilder;
import org.geysermc.mcprotocollib.protocol.data.status.ServerStatusInfo;
import org.geysermc.mcprotocollib.protocol.packet.status.clientbound.ClientboundStatusResponsePacket;
import org.geysermc.mcprotocollib.protocol.packet.status.serverbound.ServerboundStatusRequestPacket;

import static com.dotproxy.Globals.CONFIG;
import static com.dotproxy.Globals.SERVER_LOG;

public class StatusRequestHandler implements PacketHandler<ServerboundStatusRequestPacket, ServerSession> {
    @Override
    public ServerboundStatusRequestPacket apply(final ServerboundStatusRequestPacket packet, final ServerSession session) {
        if (session.isStatusRequested()) {
            session.disconnect("Duplicate status request");
            return null;
        }
        session.setStatusRequested(true);
        if (CONFIG.server.ping.logPings)
            SERVER_LOG.info("[Ping] Request from: {} [{}] to: {}:{}",
                            session.getRemoteAddress(),
                            ProtocolVersion.getProtocol(session.getProtocolVersionId()).getName(),
                            session.getConnectingServerAddress(),
                            session.getConnectingServerPort());
        ServerStatusInfo info = DotProxyServerInfoBuilder.INSTANCE.buildInfo(session);
        if (info == null) {
            session.disconnect("bye");
        } else {
            session.send(new ClientboundStatusResponsePacket(info));
        }
        return null;
    }
}
