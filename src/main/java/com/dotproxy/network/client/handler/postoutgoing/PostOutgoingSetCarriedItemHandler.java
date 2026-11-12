package com.dotproxy.network.client.handler.postoutgoing;

import com.dotproxy.feature.spectator.SpectatorSync;
import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.ClientEventLoopPacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundSetCarriedItemPacket;

import static com.dotproxy.Globals.*;

public class PostOutgoingSetCarriedItemHandler implements ClientEventLoopPacketHandler<ServerboundSetCarriedItemPacket, ClientSession> {
    @Override
    public boolean applyAsync(ServerboundSetCarriedItemPacket packet, ClientSession session) {
        if (packet.getSlot() < 0 || packet.getSlot() > 8) {
            CLIENT_LOG.debug("Passing through illegal SetCarriedItemPacket with slot: {}", packet.getSlot());
            // we're about to be kicked by the server, no need to write junk to cache
            // illegal disconnect modules will often send these packets
            return true;
        }
        try {
            CACHE.getPlayerCache().setHeldItemSlot(packet.getSlot());
            SpectatorSync.syncPlayerEquipmentWithSpectatorsFromCache();
        } catch (final Exception e) {
            DEFAULT_LOG.error("failed updating main hand slot", e);
        }
        return true;
    }
}
