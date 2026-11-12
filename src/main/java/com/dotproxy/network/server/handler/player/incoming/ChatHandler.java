package com.dotproxy.network.server.handler.player.incoming;

import com.dotproxy.network.codec.PacketHandler;
import com.dotproxy.network.server.ServerSession;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatPacket;
import org.jspecify.annotations.NonNull;

import static com.dotproxy.Globals.CONFIG;
import static com.dotproxy.Globals.IN_GAME_COMMAND;

public class ChatHandler implements PacketHandler<ServerboundChatPacket, ServerSession> {
    @Override
    public ServerboundChatPacket apply(@NonNull ServerboundChatPacket packet, @NonNull ServerSession session) {
        if (CONFIG.inGameCommands.enable) {
            final String message = packet.getMessage();
            if (IN_GAME_COMMAND.isDoubleCommandPrefixed(message)) {
                return packet.withMessage(message.substring(CONFIG.inGameCommands.prefix.length()));
            }
            if (IN_GAME_COMMAND.isCommandPrefixed(message)) {
                var dotproxyHandled = IN_GAME_COMMAND.handleInGameCommand(
                    message.substring(CONFIG.inGameCommands.prefix.length()),
                    session,
                    false // chat prefixed commands can never be server commands
                );
                if (!dotproxyHandled) {
                    session.sendAsyncAlert("<red>Unknown command!");
                }
                return null;
            }
        }
        // todo: help players by trying to converting `/` prefixed chats to commands?
        //  might be some situations where merged command trees could make this needed
        return packet;
    }
}
