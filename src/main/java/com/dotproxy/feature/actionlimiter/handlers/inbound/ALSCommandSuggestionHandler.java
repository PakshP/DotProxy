package com.dotproxy.feature.actionlimiter.handlers.inbound;

import com.dotproxy.network.codec.PacketHandler;
import com.dotproxy.network.server.ServerSession;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundCommandSuggestionPacket;

import static com.dotproxy.Globals.CONFIG;

public class ALSCommandSuggestionHandler implements PacketHandler<ServerboundCommandSuggestionPacket, ServerSession> {
    @Override
    public ServerboundCommandSuggestionPacket apply(final ServerboundCommandSuggestionPacket packet, final ServerSession session) {
        if (CONFIG.client.extra.actionLimiter.allowServerCommands) return packet;
        return null;
    }
}
