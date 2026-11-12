package com.dotproxy.network.client.handler.incoming.level;

import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.ClientEventLoopPacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundSetSimulationDistancePacket;

import static com.dotproxy.Globals.CACHE;

public class SetSimulationDistanceHandler implements ClientEventLoopPacketHandler<ClientboundSetSimulationDistancePacket, ClientSession> {
    @Override
    public boolean applyAsync(final ClientboundSetSimulationDistancePacket packet, final ClientSession session) {
        CACHE.getChunkCache().setServerSimulationDistance(packet.getSimulationDistance());
        return true;
    }
}
