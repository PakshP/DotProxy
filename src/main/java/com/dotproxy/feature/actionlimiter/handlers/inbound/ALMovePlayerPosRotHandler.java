package com.dotproxy.feature.actionlimiter.handlers.inbound;

import com.dotproxy.network.codec.PacketHandler;
import com.dotproxy.network.server.ServerSession;
import com.dotproxy.util.math.MathHelper;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerPosRotPacket;

import static com.dotproxy.Globals.CONFIG;

public class ALMovePlayerPosRotHandler implements PacketHandler<ServerboundMovePlayerPosRotPacket, ServerSession> {
    @Override
    public ServerboundMovePlayerPosRotPacket apply(final ServerboundMovePlayerPosRotPacket packet, final ServerSession session) {
        if (CONFIG.client.extra.actionLimiter.allowMovement || !session.isSpawned())
            return packet;
        if (packet.getY() <= CONFIG.client.extra.actionLimiter.movementMinY) {
            session.disconnect("ActionLimiter: Movement not allowed");
            return null;
        }
        if (MathHelper.distance2d(CONFIG.client.extra.actionLimiter.movementHomeX,
                                  CONFIG.client.extra.actionLimiter.movementHomeZ,
                                  packet.getX(),
                                  packet.getZ()) > CONFIG.client.extra.actionLimiter.movementDistance) {
            session.disconnect("ActionLimiter: Movement not allowed");
            return null;
        }
        return packet;
    }
}
