package com.dotproxy.network.client.handler.incoming.inventory;

import com.dotproxy.feature.spectator.SpectatorSync;
import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.ClientEventLoopPacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundContainerSetSlotPacket;
import org.jspecify.annotations.NonNull;

import static com.dotproxy.Globals.CACHE;
import static com.dotproxy.Globals.CLIENT_LOG;

public class ContainerSetSlotHandler implements ClientEventLoopPacketHandler<ClientboundContainerSetSlotPacket, ClientSession> {
    @Override
    public boolean applyAsync(@NonNull ClientboundContainerSetSlotPacket packet, @NonNull ClientSession session) {
        CACHE.getPlayerCache().setInventorySlot(packet.getContainerId(), packet.getItem(), packet.getSlot());
        if (packet.getContainerId() >= 0) { // negative numbers are special cases
            var container = CACHE.getPlayerCache().getInventoryCache().getContainers().get(packet.getContainerId());
            if (container != null) {
                container.setStateId(packet.getStateId());
            } else {
                CLIENT_LOG.warn("Received container set slot packet for unknown container id {}", packet.getContainerId());
            }
        }
        SpectatorSync.syncPlayerEquipmentWithSpectatorsFromCache();
        return true;
    }
}
