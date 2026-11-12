package com.dotproxy.network.client.handler.incoming.spawn;

import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.ClientEventLoopPacketHandler;
import com.dotproxy.util.math.MutableVec3i;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundSetDefaultSpawnPositionPacket;

import static com.dotproxy.Globals.CACHE;

public class SpawnPositionHandler implements ClientEventLoopPacketHandler<ClientboundSetDefaultSpawnPositionPacket, ClientSession> {
    @Override
    public boolean applyAsync(ClientboundSetDefaultSpawnPositionPacket packet, ClientSession session) {
        CACHE.getPlayerCache().setSpawnPosition(new MutableVec3i(packet.getX(), packet.getY(), packet.getZ()));
        return true;
    }
}
