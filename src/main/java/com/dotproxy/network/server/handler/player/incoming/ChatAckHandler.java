package com.dotproxy.network.server.handler.player.incoming;

import com.dotproxy.network.codec.PacketHandler;
import com.dotproxy.network.server.ServerSession;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatAckPacket;

public class ChatAckHandler implements PacketHandler<ServerboundChatAckPacket, ServerSession> {
    @Override
    public ServerboundChatAckPacket apply(final ServerboundChatAckPacket packet, final ServerSession session) {
        return null;
    }
}
