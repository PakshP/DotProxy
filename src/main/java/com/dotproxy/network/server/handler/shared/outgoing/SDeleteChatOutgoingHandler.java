package com.dotproxy.network.server.handler.shared.outgoing;

import com.dotproxy.network.codec.PacketHandler;
import com.dotproxy.network.server.ServerSession;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundDeleteChatPacket;

import static com.dotproxy.Globals.CONFIG;

public class SDeleteChatOutgoingHandler implements PacketHandler<ClientboundDeleteChatPacket, ServerSession> {
    @Override
    public ClientboundDeleteChatPacket apply(final ClientboundDeleteChatPacket packet, final ServerSession session) {
        return switch (CONFIG.server.chatSigning.mode) {
            case PASSTHROUGH -> packet;
            case DISGUISED, SYSTEM -> null;
        };
    }
}
