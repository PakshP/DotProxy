package com.dotproxy.network.server.handler.shared.incoming;

import com.dotproxy.network.codec.PacketHandler;
import com.dotproxy.network.server.ServerSession;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatSessionUpdatePacket;

import static com.dotproxy.Globals.CONFIG;

public class SChatSessionUpdateHandler implements PacketHandler<ServerboundChatSessionUpdatePacket, ServerSession> {
    @Override
    public ServerboundChatSessionUpdatePacket apply(final ServerboundChatSessionUpdatePacket packet, final ServerSession session) {
        return switch (CONFIG.server.chatSigning.mode) {
            case PASSTHROUGH -> packet;
            case DISGUISED, SYSTEM -> null;
        };
    }
}
