package com.dotproxy.network.client.handler.incoming;

import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.ClientEventLoopPacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundMapItemDataPacket;

import static com.dotproxy.Globals.CACHE;

public class MapDataHandler implements ClientEventLoopPacketHandler<ClientboundMapItemDataPacket, ClientSession> {
    @Override
    public boolean applyAsync(ClientboundMapItemDataPacket packet, ClientSession session) {
        CACHE.getMapDataCache().upsert(packet);
        return true;
    }
}
