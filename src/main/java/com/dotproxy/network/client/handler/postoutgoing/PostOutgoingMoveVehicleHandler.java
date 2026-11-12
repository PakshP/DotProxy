package com.dotproxy.network.client.handler.postoutgoing;

import com.dotproxy.feature.spectator.SpectatorSync;
import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.ClientEventLoopPacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.level.ServerboundMoveVehiclePacket;

import static com.dotproxy.Globals.CACHE;

public class PostOutgoingMoveVehicleHandler implements ClientEventLoopPacketHandler<ServerboundMoveVehiclePacket, ClientSession> {
    @Override
    public boolean applyAsync(final ServerboundMoveVehiclePacket packet, final ClientSession session) {
        CACHE.getPlayerCache().setX(packet.getX());
        CACHE.getPlayerCache().setY(packet.getY());
        CACHE.getPlayerCache().setZ(packet.getZ());
        SpectatorSync.syncPlayerPositionWithSpectators();
        return true;
    }
}
