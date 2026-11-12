package com.dotproxy.network.client.handler.incoming.inventory;

import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.ClientEventLoopPacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundContainerSetContentPacket;
import org.jspecify.annotations.NonNull;

import static com.dotproxy.Globals.CACHE;
import static com.dotproxy.Globals.CLIENT_LOG;
import static com.dotproxy.feature.spectator.SpectatorSync.syncPlayerEquipmentWithSpectatorsFromCache;


public class ContainerSetContentHandler implements ClientEventLoopPacketHandler<ClientboundContainerSetContentPacket, ClientSession> {
    @Override
    public boolean applyAsync(@NonNull ClientboundContainerSetContentPacket packet, @NonNull ClientSession session) {
        CACHE.getPlayerCache().setInventory(packet.getContainerId(), packet.getItems());
        CACHE.getPlayerCache().getInventoryCache().setMouseStack(packet.getCarriedItem());
        var container = CACHE.getPlayerCache().getInventoryCache().getContainers().get(packet.getContainerId());
        if (container != null) {
            container.setStateId(packet.getStateId());
        } else {
            CLIENT_LOG.warn("Received container set content packet for unknown container id {}", packet.getContainerId());
        }
        syncPlayerEquipmentWithSpectatorsFromCache();
        return true;
    }
}
