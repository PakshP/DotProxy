package com.dotproxy.network.server.handler.shared.incoming;

import com.dotproxy.network.UserAuthTask;
import com.dotproxy.network.codec.PacketHandler;
import com.dotproxy.network.server.ServerSession;
import org.geysermc.mcprotocollib.protocol.packet.common.clientbound.ServerboundCookieResponsePacket;
import org.geysermc.mcprotocollib.protocol.packet.login.clientbound.ClientboundHelloPacket;

import static com.dotproxy.Globals.*;

public class SCookieResponseHandler implements PacketHandler<ServerboundCookieResponsePacket, ServerSession> {
    @Override
    public ServerboundCookieResponsePacket apply(final ServerboundCookieResponsePacket packet, final ServerSession session) {
        if (CONFIG.server.strictLoginPacketSequence && session.getLoginState() != ServerSession.LoginState.WAITING_FOR_COOKIES) {
            session.disconnect("Unexpected cookie response packet");
            return null;
        }
        if (session.isTransferring() && !session.isConfigured()) {
            var cookieKey = packet.getKey();
            if (CONFIG.server.strictLoginPacketSequence && session.getCookieCache().getCookies().containsKey(cookieKey)) {
                session.disconnect("Duplicate cookie response");
                return null;
            }
            if (!session.getCookieCache().validateCookieRequested(cookieKey) && CONFIG.server.strictLoginPacketSequence) {
                session.disconnect("Cookie key was not requested");
                return null;
            }
            session.getCookieCache().handleCookieResponse(cookieKey, packet.getPayload());
            if (session.getCookieCache().receivedAllCookieResponses()) {
                if (CONFIG.server.verifyUsers) {
                    session.setLoginState(ServerSession.LoginState.KEY);
                    session.send(new ClientboundHelloPacket(session.getServerId(), session.getKeyPair().getPublic(), session.getChallenge(), true));
                } else {
                    EXECUTOR.execute(new UserAuthTask(session, null));
                }
            }
            return null;
        }
        SERVER_LOG.debug("Received unrequested cookie response: {}", packet.getKey());
        // shouldn't ever get here
        // should these packets ever be passed through?
        return null;
    }
}
