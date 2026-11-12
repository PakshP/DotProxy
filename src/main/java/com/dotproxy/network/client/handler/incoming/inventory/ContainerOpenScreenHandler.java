package com.dotproxy.network.client.handler.incoming.inventory;

import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.ClientEventLoopPacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundOpenScreenPacket;

import static com.dotproxy.Globals.CACHE;

public class ContainerOpenScreenHandler implements ClientEventLoopPacketHandler<ClientboundOpenScreenPacket, ClientSession> {
    @Override
    public boolean applyAsync(final ClientboundOpenScreenPacket packet, final ClientSession session) {
        CACHE.getPlayerCache().openContainer(packet.getContainerId(), packet.getType(), packet.getTitle());
        return true;
    }
}
