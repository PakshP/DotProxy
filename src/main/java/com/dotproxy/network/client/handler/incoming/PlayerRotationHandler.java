package com.dotproxy.network.client.handler.incoming;

import com.dotproxy.Proxy;
import com.dotproxy.feature.spectator.SpectatorSync;
import com.dotproxy.module.impl.AntiAFK;
import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.ClientEventLoopPacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerRotationPacket;

import static com.dotproxy.Globals.*;

public class PlayerRotationHandler implements ClientEventLoopPacketHandler<ClientboundPlayerRotationPacket, ClientSession> {
    @Override
    public boolean applyAsync(final ClientboundPlayerRotationPacket packet, final ClientSession session) {
        CACHE.getPlayerCache().setYaw(packet.getYaw());
        CACHE.getPlayerCache().setPitch(packet.getPitch());
        if (!Proxy.getInstance().hasActivePlayer()) {
            BOT.handlePlayerRotate();
        }
        SpectatorSync.syncPlayerPositionWithSpectators();
        MODULE.get(AntiAFK.class).handlePlayerPosRotate();
        return true;
    }
}
