package com.dotproxy.network.client.handler.incoming.level;

import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.ClientEventLoopPacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundBlockChangedAckPacket;

import static com.dotproxy.Globals.CACHE;

public class BlockChangedAckHandler implements ClientEventLoopPacketHandler<ClientboundBlockChangedAckPacket, ClientSession> {
    @Override
    public boolean applyAsync(final ClientboundBlockChangedAckPacket packet, final ClientSession session) {
        CACHE.getChunkCache().getBlockStatePredictionHandler().endPredictionsUpTo(packet.getSequence());
        return true;
    }
}
