package com.dotproxy.feature.actionlimiter.handlers.outbound;

import com.dotproxy.mc.item.ItemRegistry;
import com.dotproxy.module.impl.ActionLimiter;
import com.dotproxy.network.codec.PacketHandler;
import com.dotproxy.network.server.ServerSession;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundSetPlayerInventoryPacket;

import static com.dotproxy.Globals.CONFIG;
import static com.dotproxy.Globals.MODULE;

public class ALSetPlayerInventoryHandler implements PacketHandler<ClientboundSetPlayerInventoryPacket, ServerSession> {
    @Override
    public ClientboundSetPlayerInventoryPacket apply(final ClientboundSetPlayerInventoryPacket packet, final ServerSession session) {
        if (!CONFIG.client.extra.actionLimiter.itemsBlacklistEnabled) return packet;
        var al = MODULE.get(ActionLimiter.class);
        if (al.isBlacklistedItem(packet.getContents())) {
            var itemName = ItemRegistry.REGISTRY.get(packet.getContents().getId()).name();
            al.info("Blacklisted item detected in set player inventory: {}", itemName);
            al.disconnect(session, "Blacklisted item: " + itemName);
            return null;
        }
        return packet;
    }
}
