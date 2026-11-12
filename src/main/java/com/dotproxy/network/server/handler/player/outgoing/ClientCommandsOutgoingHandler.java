package com.dotproxy.network.server.handler.player.outgoing;

import com.dotproxy.command.brigadier.McplCommandTreeMerger;
import com.dotproxy.network.codec.PacketHandler;
import com.dotproxy.network.server.ServerSession;
import org.geysermc.mcprotocollib.protocol.data.game.command.CommandNode;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundCommandsPacket;

import static com.dotproxy.Globals.*;

public class ClientCommandsOutgoingHandler implements PacketHandler<ClientboundCommandsPacket, ServerSession> {
    @Override
    public ClientboundCommandsPacket apply(final ClientboundCommandsPacket packet, final ServerSession session) {
        if (CONFIG.inGameCommands.enable && CONFIG.inGameCommands.slashCommands) {
            CommandNode[] dotproxyCommandNodes = COMMAND.getMcplCommandNodes();
            if (CONFIG.inGameCommands.slashCommandsReplacesServerCommands) {
                return new ClientboundCommandsPacket(
                    dotproxyCommandNodes,
                    0
                );
            }
            if (packet.getFirstNodeIndex() != 0) {
                SERVER_LOG.warn("Unexpected root index on server command nodes: {}", packet.getFirstNodeIndex());
                SERVER_LOG.warn("Skipping nodes combination.");
                return new ClientboundCommandsPacket(
                    dotproxyCommandNodes,
                    0
                );
            }
            return new ClientboundCommandsPacket(
                McplCommandTreeMerger.mergeCommandNodes(dotproxyCommandNodes, packet.getNodes()),
                0
            );
        }
        return packet;
    }
}
