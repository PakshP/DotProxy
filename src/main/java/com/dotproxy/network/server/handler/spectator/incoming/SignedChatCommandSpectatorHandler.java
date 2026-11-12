package com.dotproxy.network.server.handler.spectator.incoming;

import com.dotproxy.Proxy;
import com.dotproxy.command.api.CommandContext;
import com.dotproxy.network.codec.PacketHandler;
import com.dotproxy.network.server.ServerSession;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatCommandSignedPacket;

import static com.dotproxy.Globals.*;

public class SignedChatCommandSpectatorHandler implements PacketHandler<ServerboundChatCommandSignedPacket, ServerSession> {
    @Override
    public ServerboundChatCommandSignedPacket apply(final ServerboundChatCommandSignedPacket packet, final ServerSession session) {
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
                Proxy.getInstance().getClient().sendAsync(packet);
                return null;
            } else {
                session.sendAsyncAlert("<red>Unknown command!");
            }
        }
        return null;
    }
}
