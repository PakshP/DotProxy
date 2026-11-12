package com.dotproxy.network.client.handler.incoming;

import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.ClientEventLoopPacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundSetExperiencePacket;

import static com.dotproxy.Globals.CACHE;

public class SetExperienceHandler implements ClientEventLoopPacketHandler<ClientboundSetExperiencePacket, ClientSession> {
    @Override
    public boolean applyAsync(ClientboundSetExperiencePacket packet, ClientSession session) {
        CACHE.getPlayerCache().getThePlayer()
                .setTotalExperience(packet.getTotalExperience())
                .setLevel(packet.getLevel())
                .setExperience(packet.getExperience());
        return true;
    }
}
