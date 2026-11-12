package com.dotproxy.feature.actionlimiter.handlers.inbound;

import com.dotproxy.feature.player.World;
import com.dotproxy.mc.block.BlockRegistry;
import com.dotproxy.network.codec.PacketHandler;
import com.dotproxy.network.server.ServerSession;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundUseItemOnPacket;

import static com.dotproxy.Globals.CONFIG;

public class ALUseItemOnHandler implements PacketHandler<ServerboundUseItemOnPacket, ServerSession> {
    @Override
    public ServerboundUseItemOnPacket apply(final ServerboundUseItemOnPacket packet, final ServerSession session) {
        if (!CONFIG.client.extra.actionLimiter.allowUseItem) return null;
        if (!CONFIG.client.extra.actionLimiter.allowEnderChest) {
            var blockAtBlockPos = World.getBlock(packet.getX(), packet.getY(), packet.getZ());
            if (blockAtBlockPos.equals(BlockRegistry.ENDER_CHEST)) {
                return null;
            }
        }
        return packet;
    }
}
