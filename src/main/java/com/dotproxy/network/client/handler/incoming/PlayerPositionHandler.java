package com.dotproxy.network.client.handler.incoming;

import com.dotproxy.Proxy;
import com.dotproxy.cache.data.PlayerCache;
import com.dotproxy.feature.spectator.SpectatorSync;
import com.dotproxy.module.impl.AntiAFK;
import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.ClientEventLoopPacketHandler;
import com.dotproxy.network.server.ServerSession;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.PositionElement;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerPositionPacket;
import org.jspecify.annotations.NonNull;

import static com.dotproxy.Globals.*;
import static java.util.Objects.isNull;

public class PlayerPositionHandler implements ClientEventLoopPacketHandler<ClientboundPlayerPositionPacket, ClientSession> {
    @Override
    public boolean applyAsync(@NonNull ClientboundPlayerPositionPacket packet, @NonNull ClientSession session) {
        PlayerCache cache = CACHE.getPlayerCache();
        var teleportQueue = cache.getTeleportQueue();
        cache.getTeleportQueue().add(packet);
        while (teleportQueue.size() > 100) {
            var tp = teleportQueue.poll();
            CLIENT_LOG.debug("Teleport queue larger than 100, dropping oldest entry. Dropped teleport: {} Last teleport: {}", tp.getId(), packet.getId());
        }
        cache
            .setRespawning(false)
            .setX((packet.getRelatives().contains(PositionElement.X) ? cache.getX() : 0.0d) + packet.getX())
            .setY((packet.getRelatives().contains(PositionElement.Y) ? cache.getY() : 0.0d) + packet.getY())
            .setZ((packet.getRelatives().contains(PositionElement.Z) ? cache.getZ() : 0.0d) + packet.getZ())
            .setVelX((packet.getRelatives().contains(PositionElement.DELTA_X) ? cache.getVelX() : 0.0d) + packet.getDeltaX())
            .setVelY((packet.getRelatives().contains(PositionElement.DELTA_Y) ? cache.getVelY() : 0.0d) + packet.getDeltaY())
            .setVelZ((packet.getRelatives().contains(PositionElement.DELTA_Z) ? cache.getVelZ() : 0.0d) + packet.getDeltaZ())
            .setYaw((packet.getRelatives().contains(PositionElement.Y_ROT) ? cache.getYaw() : 0.0f) + packet.getYaw())
            .setPitch((packet.getRelatives().contains(PositionElement.X_ROT) ? cache.getPitch() : 0.0f) + packet.getPitch());
        ServerSession currentPlayer = Proxy.getInstance().getCurrentPlayer().get();
        if (isNull(currentPlayer) || !currentPlayer.isLoggedIn()) {
            BOT.handlePlayerPosRotate(packet.getId());
        } else {
            CLIENT_LOG.debug("Passing teleport {} through to current player", packet.getId());
        }
        SpectatorSync.syncPlayerPositionWithSpectators();
        MODULE.get(AntiAFK.class).handlePlayerPosRotate();
        return true;
    }
}
