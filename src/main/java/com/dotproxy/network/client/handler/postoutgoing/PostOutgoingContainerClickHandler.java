package com.dotproxy.network.client.handler.postoutgoing;

import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.ClientEventLoopPacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.inventory.ServerboundContainerClickPacket;

import static com.dotproxy.Globals.CACHE;
import static com.dotproxy.feature.spectator.SpectatorSync.syncPlayerEquipmentWithSpectatorsFromCache;

public class PostOutgoingContainerClickHandler implements ClientEventLoopPacketHandler<ServerboundContainerClickPacket, ClientSession> {
    @Override
    public boolean applyAsync(final ServerboundContainerClickPacket packet, final ClientSession session) {
        CACHE.getPlayerCache().getInventoryCache().handleContainerClick(packet);
        syncPlayerEquipmentWithSpectatorsFromCache();
        return true;
    }
}
