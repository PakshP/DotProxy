package com.dotproxy.feature.actionlimiter.handlers.inbound;

import com.dotproxy.network.codec.PacketHandler;
import com.dotproxy.network.server.ServerSession;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatCommandSignedPacket;

import static com.dotproxy.Globals.CONFIG;

public class ALSignedChatCommandHandler implements PacketHandler<ServerboundChatCommandSignedPacket, ServerSession> {
    @Override
    public ServerboundChatCommandSignedPacket apply(final ServerboundChatCommandSignedPacket packet, final ServerSession session) {
        if (CONFIG.client.extra.actionLimiter.allowServerCommands) return packet;
        else return null;
    }
}
