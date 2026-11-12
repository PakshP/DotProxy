package com.dotproxy.network.client.handler.incoming;

import com.dotproxy.cache.data.chunk.WorldBorderData;
import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.ClientEventLoopPacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.border.ClientboundInitializeBorderPacket;

import static com.dotproxy.Globals.CACHE;

public class WorldBorderInitializeHandler implements ClientEventLoopPacketHandler<ClientboundInitializeBorderPacket, ClientSession> {
    @Override
    public boolean applyAsync(final ClientboundInitializeBorderPacket packet, final ClientSession session) {
        CACHE.getChunkCache().setWorldBorderData(new WorldBorderData(
                packet.getNewCenterX(),
                packet.getNewCenterZ(),
                packet.getNewSize(),
                packet.getNewAbsoluteMaxSize(),
                packet.getWarningBlocks(),
                packet.getWarningTime()
        ));
        return true;
    }
}
