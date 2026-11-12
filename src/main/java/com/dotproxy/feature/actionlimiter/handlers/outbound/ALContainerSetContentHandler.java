package com.dotproxy.feature.actionlimiter.handlers.outbound;

import com.dotproxy.mc.item.ItemRegistry;
import com.dotproxy.module.impl.ActionLimiter;
import com.dotproxy.network.codec.PacketHandler;
import com.dotproxy.network.server.ServerSession;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundContainerSetContentPacket;

import static com.dotproxy.Globals.CONFIG;
import static com.dotproxy.Globals.MODULE;

public class ALContainerSetContentHandler implements PacketHandler<ClientboundContainerSetContentPacket, ServerSession> {
    @Override
    public ClientboundContainerSetContentPacket apply(final ClientboundContainerSetContentPacket packet, final ServerSession session) {
        if (!CONFIG.client.extra.actionLimiter.itemsBlacklistEnabled) return packet;
        var al = MODULE.get(ActionLimiter.class);
        for (var item : packet.getItems()) {
            if (al.isBlacklistedItem(item)) {
                var itemName = ItemRegistry.REGISTRY.get(item.getId()).name();
                al.info("Blacklisted item detected in container set content: {}", itemName);
                al.disconnect(session, "Blacklisted item: " + itemName);
                return null;
            }
        }
        if (al.isBlacklistedItem(packet.getCarriedItem())) {
            var itemName = ItemRegistry.REGISTRY.get(packet.getCarriedItem().getId()).name();
            al.info("Blacklisted item detected in container set content held item: {}", itemName);
            al.disconnect(session, "Blacklisted item: " + itemName);
            return null;
        }
        return packet;
    }
}
