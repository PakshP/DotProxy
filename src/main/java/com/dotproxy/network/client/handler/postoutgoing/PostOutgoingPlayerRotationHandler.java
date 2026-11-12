package com.dotproxy.network.client.handler.postoutgoing;

import com.dotproxy.feature.spectator.SpectatorSync;
import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.ClientEventLoopPacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerRotPacket;

import static com.dotproxy.Globals.CACHE;

public class PostOutgoingPlayerRotationHandler implements ClientEventLoopPacketHandler<ServerboundMovePlayerRotPacket, ClientSession> {
    @Override
    public boolean applyAsync(ServerboundMovePlayerRotPacket packet, ClientSession session) {
        CACHE.getPlayerCache()
                .setYaw(packet.getYaw())
                .setPitch(packet.getPitch());
        SpectatorSync.syncPlayerPositionWithSpectators();
        return true;
    }
}
