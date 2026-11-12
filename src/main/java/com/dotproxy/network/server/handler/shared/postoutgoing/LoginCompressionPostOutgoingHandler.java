package com.dotproxy.network.server.handler.shared.postoutgoing;

import com.dotproxy.network.codec.PostOutgoingPacketHandler;
import com.dotproxy.network.server.ServerSession;
import org.geysermc.mcprotocollib.protocol.packet.login.clientbound.ClientboundLoginCompressionPacket;
import org.geysermc.mcprotocollib.protocol.packet.login.clientbound.ClientboundLoginFinishedPacket;

import static com.dotproxy.Globals.CONFIG;

public class LoginCompressionPostOutgoingHandler implements PostOutgoingPacketHandler<ClientboundLoginCompressionPacket, ServerSession> {
    @Override
    public void accept(final ClientboundLoginCompressionPacket packet, final ServerSession session) {
        session.setCompressionThreshold(packet.getThreshold(), CONFIG.server.compressionLevel, true);
        var profile = session.getProfileCache().getProfile();
        if (profile == null) {
            session.disconnect("Failed to get profile");
            return;
        }
        session.sendAsync(new ClientboundLoginFinishedPacket(profile));
    }
}
