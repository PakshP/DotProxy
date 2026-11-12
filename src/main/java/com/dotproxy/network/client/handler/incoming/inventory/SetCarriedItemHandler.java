package com.dotproxy.network.client.handler.incoming.inventory;

import com.dotproxy.Proxy;
import com.dotproxy.feature.spectator.SpectatorSync;
import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.ClientEventLoopPacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundSetHeldSlotPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundSetCarriedItemPacket;

import static com.dotproxy.Globals.CACHE;
import static com.dotproxy.Globals.DEFAULT_LOG;

public class SetCarriedItemHandler implements ClientEventLoopPacketHandler<ClientboundSetHeldSlotPacket, ClientSession> {
    @Override
    public boolean applyAsync(ClientboundSetHeldSlotPacket packet, ClientSession session) {
        if (!Proxy.getInstance().hasActivePlayer() && CACHE.getPlayerCache().getHeldItemSlot() != packet.getSlot()) {
            try {
                // the mc server does not know we are using this slot until we say so
                // so we make sure its synced correctly here
                // our outbound handler will update the slot in the cache
                session.sendAwait(new ServerboundSetCarriedItemPacket(packet.getSlot()));
                SpectatorSync.syncPlayerEquipmentWithSpectatorsFromCache();
            } catch (final Exception e) {
                DEFAULT_LOG.error("failed updating main hand slot", e);
            }
        }
        return true;
    }
}
