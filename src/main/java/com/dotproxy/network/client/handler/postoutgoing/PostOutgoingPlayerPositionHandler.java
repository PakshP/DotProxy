package com.dotproxy.network.client.handler.postoutgoing;

import com.dotproxy.feature.spectator.SpectatorSync;
import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.ClientEventLoopPacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerPosPacket;

import static com.dotproxy.Globals.CACHE;

public class PostOutgoingPlayerPositionHandler implements ClientEventLoopPacketHandler<ServerboundMovePlayerPosPacket, ClientSession> {

    @Override
    public boolean applyAsync(ServerboundMovePlayerPosPacket packet, ClientSession session) {
        CACHE.getPlayerCache()
                .setX(packet.getX())
                .setY(packet.getY())
                .setZ(packet.getZ());
        SpectatorSync.syncPlayerPositionWithSpectators();
        return true;
    }
}
