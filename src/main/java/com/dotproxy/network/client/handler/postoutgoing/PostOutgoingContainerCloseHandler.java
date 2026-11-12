package com.dotproxy.network.client.handler.postoutgoing;

import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.ClientEventLoopPacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.inventory.ServerboundContainerClosePacket;

import static com.dotproxy.Globals.CACHE;
import static com.dotproxy.feature.spectator.SpectatorSync.syncPlayerEquipmentWithSpectatorsFromCache;

public class PostOutgoingContainerCloseHandler implements ClientEventLoopPacketHandler<ServerboundContainerClosePacket, ClientSession> {
    @Override
    public boolean applyAsync(final ServerboundContainerClosePacket packet, final ClientSession session) {
        CACHE.getPlayerCache().closeContainer(packet.getContainerId());
        syncPlayerEquipmentWithSpectatorsFromCache();
        return true;
    }
}
