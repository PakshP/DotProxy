package com.dotproxy.network.server.handler.player.incoming;

import com.dotproxy.network.codec.PacketHandler;
import com.dotproxy.network.server.ServerSession;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatCommandSignedPacket;

import static com.dotproxy.Globals.CONFIG;
import static com.dotproxy.Globals.IN_GAME_COMMAND;

public class SignedChatCommandHandler implements PacketHandler<ServerboundChatCommandSignedPacket, ServerSession> {
    @Override
    public ServerboundChatCommandSignedPacket apply(final ServerboundChatCommandSignedPacket packet, final ServerSession session) {
        final String command = packet.getCommand();
        if (command.isBlank()) return packet;
        if (CONFIG.inGameCommands.slashCommands && CONFIG.inGameCommands.enable) {
            var dotproxyHandled = IN_GAME_COMMAND.handleInGameCommand(
                command,
                session,
                CONFIG.inGameCommands.prioritizeServerCommands
            );
            if (dotproxyHandled) {
                return null;
            }
            if (CONFIG.inGameCommands.slashCommandsReplacesServerCommands) {
                session.sendAsyncAlert("<red>Unknown command!");
                return null;
            }
        }
        return packet;
    }
}
