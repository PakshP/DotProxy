package com.dotproxy.feature.actionlimiter.handlers.outbound;

import com.dotproxy.network.codec.PacketHandler;
import com.dotproxy.network.server.ServerSession;
import com.dotproxy.util.math.MathHelper;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundForgetLevelChunkPacket;

import static com.dotproxy.Globals.CACHE;
import static com.dotproxy.Globals.CONFIG;

public class ALForgetLevelChunkHandler implements PacketHandler<ClientboundForgetLevelChunkPacket, ServerSession> {
    @Override
    public ClientboundForgetLevelChunkPacket apply(final ClientboundForgetLevelChunkPacket packet, final ServerSession session) {
        if (CONFIG.client.extra.actionLimiter.allowMovement) return packet;
        var blockX = packet.getX() << 4;
        var blockZ = packet.getZ() << 4;

        if (MathHelper.distance2d(
            blockX,
            blockZ,
            CONFIG.client.extra.actionLimiter.movementHomeX,
            CONFIG.client.extra.actionLimiter.movementHomeZ
        ) > CONFIG.client.extra.actionLimiter.movementDistance + 64 + (CACHE.getChunkCache().getServerViewDistance() * 16)) {
            session.disconnect("ActionLimiter: Movement not allowed");
            return null;
        }
        return packet;
    }
}
