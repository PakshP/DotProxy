package com.dotproxy.network.client.handler.incoming.level;

import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.ClientEventLoopPacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundChunksBiomesPacket;

import static com.dotproxy.Globals.CACHE;

public class ChunksBiomesHandler implements ClientEventLoopPacketHandler<ClientboundChunksBiomesPacket, ClientSession> {
    @Override
    public boolean applyAsync(final ClientboundChunksBiomesPacket packet, final ClientSession session) {
        return CACHE.getChunkCache().handleChunkBiomes(packet);
    }
}
