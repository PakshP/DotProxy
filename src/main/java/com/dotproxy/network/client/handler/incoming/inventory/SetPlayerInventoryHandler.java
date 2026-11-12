package com.dotproxy.network.client.handler.incoming.inventory;

import com.dotproxy.mc.item.ContainerTypeInfoRegistry;
import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.ClientEventLoopPacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundSetPlayerInventoryPacket;

import static com.dotproxy.Globals.CACHE;
import static com.dotproxy.Globals.CACHE_LOG;
import static com.dotproxy.feature.spectator.SpectatorSync.syncPlayerEquipmentWithSpectatorsFromCache;

public class SetPlayerInventoryHandler implements ClientEventLoopPacketHandler<ClientboundSetPlayerInventoryPacket, ClientSession> {
    @Override
    public boolean applyAsync(final ClientboundSetPlayerInventoryPacket packet, final ClientSession session) {
        int index = packet.getSlot();
        if (index < 0) {
            CACHE_LOG.error("SetPlayerInventoryPacket slot {} out of bounds", index);
            return true;
        }
        final int playerInvTopSlots = 9;
        final int playerInvMainSlots = 27;
        final int hotbarSlots = 9;
        final int playerInvArmorSlotOffset = 5;
        if (index < 9) { // hotbar
            CACHE.getPlayerCache().getInventoryCache().getPlayerInventory().setItemStack(index + playerInvTopSlots + playerInvMainSlots, packet.getContents());
            if (CACHE.getPlayerCache().getInventoryCache().getOpenContainerId() > 0) {
                var container = CACHE.getPlayerCache().getInventoryCache().getOpenContainer();
                var containerTypeInfo = ContainerTypeInfoRegistry.REGISTRY.get(container.getType());
                container.setItemStack(index + containerTypeInfo.topSlots() + playerInvMainSlots, packet.getContents());
            }
        } else if (index < 36) { // main inv
            CACHE.getPlayerCache().getInventoryCache().getPlayerInventory().setItemStack(index + playerInvTopSlots - hotbarSlots, packet.getContents());
            if (CACHE.getPlayerCache().getInventoryCache().getOpenContainerId() > 0) {
                var container = CACHE.getPlayerCache().getInventoryCache().getOpenContainer();
                var containerTypeInfo = ContainerTypeInfoRegistry.REGISTRY.get(container.getType());
                container.setItemStack(index + containerTypeInfo.topSlots() - hotbarSlots, packet.getContents());
            }
        } else if (index < 40) { // armor
            // armor index in packet is in reverse order where feet has lowest slot id and counts up to head
            CACHE.getPlayerCache().getInventoryCache().getPlayerInventory().setItemStack(39 - index + playerInvArmorSlotOffset, packet.getContents());
        } else if (index == 40) { // offhand
            CACHE.getPlayerCache().getInventoryCache().getPlayerInventory().setItemStack(45, packet.getContents());
        } else {
            CACHE_LOG.error("SetPlayerInventoryPacket slot {} out of bounds", index);
        }
        syncPlayerEquipmentWithSpectatorsFromCache();
        return true;
    }
}
