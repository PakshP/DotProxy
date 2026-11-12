package com.dotproxy.network.server.handler.spectator.outgoing;

import com.dotproxy.command.brigadier.McplCommandTreeMerger;
import com.dotproxy.network.codec.PacketHandler;
import com.dotproxy.network.server.ServerSession;
import org.geysermc.mcprotocollib.protocol.data.game.command.CommandNode;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundCommandsPacket;

import static com.dotproxy.Globals.*;

public class ClientCommandsSpectatorOutgoingHandler implements PacketHandler<ClientboundCommandsPacket, ServerSession> {
    @Override
    public ClientboundCommandsPacket apply(final ClientboundCommandsPacket packet, final ServerSession session) {
        if (CONFIG.inGameCommands.enable && CONFIG.inGameCommands.slashCommands && CONFIG.server.spectator.fullCommandsEnabled && CONFIG.server.spectator.fullCommandsAcceptSlashCommands) {
            if (CONFIG.server.spectator.fullCommandsRequireRegularWhitelist && !PLAYER_LISTS.getWhitelist().contains(session.getProfileCache().getProfile().getId())) {
                return null;
            }
            CommandNode[] dotproxyCommandNodes = COMMAND.getMcplCommandNodes();
            if (!CONFIG.server.spectator.fullCommandsServerCommands || packet.getFirstNodeIndex() != 0) {
                return new ClientboundCommandsPacket(
                    dotproxyCommandNodes,
                    0
                );
            } else {
                return new ClientboundCommandsPacket(
                    McplCommandTreeMerger.mergeCommandNodes(dotproxyCommandNodes, packet.getNodes()),
                    0
                );
            }
        }
        return null;
    }
}
