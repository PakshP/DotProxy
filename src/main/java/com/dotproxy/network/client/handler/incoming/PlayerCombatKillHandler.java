package com.dotproxy.network.client.handler.incoming;

import com.dotproxy.event.client.ClientDeathEvent;
import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.ClientEventLoopPacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerCombatKillPacket;

import static com.dotproxy.Globals.CACHE;
import static com.dotproxy.Globals.EVENT_BUS;

public class PlayerCombatKillHandler implements ClientEventLoopPacketHandler<ClientboundPlayerCombatKillPacket, ClientSession> {

    @Override
    public boolean applyAsync(ClientboundPlayerCombatKillPacket packet, ClientSession session) {
        if (packet.getPlayerId() == CACHE.getPlayerCache().getEntityId()) {
            EVENT_BUS.postAsync(new ClientDeathEvent());
        }
        return true;
    }
}
