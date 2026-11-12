package com.dotproxy.network.client.handler.outgoing;

import com.dotproxy.Proxy;
import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.PacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.inventory.ServerboundContainerClickPacket;

import static com.dotproxy.Globals.CACHE;

public class OutgoingContainerClickHandler implements PacketHandler<ServerboundContainerClickPacket, ClientSession> {
    @Override
    public ServerboundContainerClickPacket apply(final ServerboundContainerClickPacket packet, final ClientSession session) {
        if (Proxy.getInstance().hasActivePlayer()) {
            CACHE.getPlayerCache().getInventoryCache().getOpenContainer().setStateId(packet.getStateId());
        }
        return packet;
    }
}
