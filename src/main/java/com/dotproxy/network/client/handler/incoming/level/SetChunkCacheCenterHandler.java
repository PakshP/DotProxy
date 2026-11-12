package com.dotproxy.network.client.handler.incoming.level;

import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.ClientEventLoopPacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundSetChunkCacheCenterPacket;

import static com.dotproxy.Globals.CACHE;

public class SetChunkCacheCenterHandler implements ClientEventLoopPacketHandler<ClientboundSetChunkCacheCenterPacket, ClientSession> {
    @Override
    public boolean applyAsync(final ClientboundSetChunkCacheCenterPacket packet, final ClientSession session) {
        CACHE.getChunkCache().setCenterX(packet.getChunkX());
        CACHE.getChunkCache().setCenterZ(packet.getChunkZ());
        return true;
    }
}
