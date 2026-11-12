package com.dotproxy.feature.actionlimiter.handlers.inbound;

import com.dotproxy.network.codec.PacketHandler;
import com.dotproxy.network.server.ServerSession;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatPacket;

import static com.dotproxy.Globals.CONFIG;

public class ALChatHandler implements PacketHandler<ServerboundChatPacket, ServerSession> {
    @Override
    public ServerboundChatPacket apply(final ServerboundChatPacket packet, final ServerSession session) {
        if (CONFIG.client.extra.actionLimiter.allowChat) return packet;
        else return null;
    }
}
