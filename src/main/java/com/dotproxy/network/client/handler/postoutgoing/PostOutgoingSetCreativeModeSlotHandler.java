package com.dotproxy.network.client.handler.postoutgoing;

import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.ClientEventLoopPacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.inventory.ServerboundSetCreativeModeSlotPacket;

import static com.dotproxy.Globals.CACHE;
import static com.dotproxy.feature.spectator.SpectatorSync.syncPlayerEquipmentWithSpectatorsFromCache;

public class PostOutgoingSetCreativeModeSlotHandler implements ClientEventLoopPacketHandler<ServerboundSetCreativeModeSlotPacket, ClientSession> {
    @Override
    public boolean applyAsync(final ServerboundSetCreativeModeSlotPacket packet, final ClientSession session) {
        var playerInventory = CACHE.getPlayerCache().getInventoryCache().getPlayerInventory();
        playerInventory.setItemStack(packet.getSlot(), packet.getClickedItem());
        syncPlayerEquipmentWithSpectatorsFromCache();
        return true;
    }
}
