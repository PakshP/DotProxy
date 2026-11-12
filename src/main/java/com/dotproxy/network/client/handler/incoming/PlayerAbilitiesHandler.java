package com.dotproxy.network.client.handler.incoming;

import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.ClientEventLoopPacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerAbilitiesPacket;

import static com.dotproxy.Globals.CACHE;

public class PlayerAbilitiesHandler implements ClientEventLoopPacketHandler<ClientboundPlayerAbilitiesPacket, ClientSession> {
    @Override
    public boolean applyAsync(final ClientboundPlayerAbilitiesPacket packet, final ClientSession session) {
        CACHE.getPlayerCache()
            .setInvincible(packet.isInvincible())
            .setCanFly(packet.isCanFly())
            .setFlying(packet.isFlying())
            .setCreative(packet.isCreative())
            .setFlySpeed(packet.getFlySpeed())
            .setWalkSpeed(packet.getWalkSpeed());
        return true;
    }
}
