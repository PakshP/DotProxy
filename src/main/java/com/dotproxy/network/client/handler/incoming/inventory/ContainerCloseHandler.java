package com.dotproxy.network.client.handler.incoming.inventory;

import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.ClientEventLoopPacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundContainerClosePacket;

import static com.dotproxy.Globals.CACHE;
import static com.dotproxy.feature.spectator.SpectatorSync.syncPlayerEquipmentWithSpectatorsFromCache;

public class ContainerCloseHandler implements ClientEventLoopPacketHandler<ClientboundContainerClosePacket, ClientSession> {
    @Override
    public boolean applyAsync(final ClientboundContainerClosePacket packet, final ClientSession session) {
        CACHE.getPlayerCache().closeContainer(packet.getContainerId());
        syncPlayerEquipmentWithSpectatorsFromCache();
        return true;
    }
}
