package com.dotproxy.network.client.handler.postoutgoing;

import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.ClientEventLoopPacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundPlayerAbilitiesPacket;

import static com.dotproxy.Globals.CACHE;

public class PostOutgoingPlayerAbilitiesHandler implements ClientEventLoopPacketHandler<ServerboundPlayerAbilitiesPacket, ClientSession> {
    @Override
    public boolean applyAsync(final ServerboundPlayerAbilitiesPacket packet, final ClientSession session) {
        CACHE.getPlayerCache().setFlying(packet.isFlying());
        return true;
    }
}
