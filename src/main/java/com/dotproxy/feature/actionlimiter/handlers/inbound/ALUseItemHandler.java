package com.dotproxy.feature.actionlimiter.handlers.inbound;

import com.dotproxy.network.codec.PacketHandler;
import com.dotproxy.network.server.ServerSession;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundUseItemPacket;

import static com.dotproxy.Globals.CONFIG;

public class ALUseItemHandler implements PacketHandler<ServerboundUseItemPacket, ServerSession> {
    @Override
    public ServerboundUseItemPacket apply(final ServerboundUseItemPacket packet, final ServerSession session) {
        if (!CONFIG.client.extra.actionLimiter.allowUseItem) return null;
        return packet;
    }
}
