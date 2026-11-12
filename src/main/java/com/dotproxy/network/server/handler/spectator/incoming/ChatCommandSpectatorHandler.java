package com.dotproxy.network.server.handler.spectator.incoming;

import com.dotproxy.Proxy;
import com.dotproxy.command.api.CommandContext;
import com.dotproxy.network.codec.PacketHandler;
import com.dotproxy.network.server.ServerSession;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatCommandPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatCommandSignedPacket;

import static com.dotproxy.Globals.*;

public class ChatCommandSpectatorHandler implements PacketHandler<ServerboundChatCommandPacket, ServerSession> {
    @Override
    public ServerboundChatCommandPacket apply(final ServerboundChatCommandPacket packet, final ServerSession session) {
        final String command = packet.getCommand();
        if (command.isBlank()) return packet;
        if (CONFIG.inGameCommands.slashCommands
            && CONFIG.inGameCommands.enable
            && CONFIG.server.spectator.fullCommandsEnabled
            && CONFIG.server.spectator.fullCommandsAcceptSlashCommands
            && (CONFIG.server.spectator.fullCommandsRequireRegularWhitelist
                ? PLAYER_LISTS.getWhitelist().contains(session.getProfileCache().getProfile().getId())
                : true)
        ) {
            var dotproxyHandled = IN_GAME_COMMAND.handleInGameCommandSpectator(
                command,
                session,
                CONFIG.server.spectator.fullCommandsPrioritizeServerCommands
            );
            if (dotproxyHandled) {
                return null;
            }
            if (CONFIG.server.spectator.fullCommandsServerCommands
                && IN_GAME_COMMAND.matchesServerCommand(command, CommandContext.createSpectatorContext(command, session))
            ) {
                if (CONFIG.client.chatSigning.signCommands && CACHE.getChatCache().isSignableCommand(command)) {
                    // if we overwrite a server command with a dotproxy command, the signing state won't match
                    // for example: `/say hello` is both a dotproxy and server command
                    // from the client perspective, their command tree (now from dotproxy) says it's no longer a signed arg
                    session.callPacketReceived(new ServerboundChatCommandSignedPacket(command));
                    return null;
                }
                Proxy.getInstance().getClient().sendAsync(packet);
                return null;
            } else {
                session.sendAsyncAlert("<red>Unknown command!");
            }
        }
        return null;
    }
}
