package com.dotproxy.network.client.handler.incoming.inventory;

import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.ClientEventLoopPacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundRecipeBookRemovePacket;

import static com.dotproxy.Globals.CACHE;

public class RecipeBookRemoveHandler implements ClientEventLoopPacketHandler<ClientboundRecipeBookRemovePacket, ClientSession> {
    @Override
    public boolean applyAsync(final ClientboundRecipeBookRemovePacket packet, final ClientSession session) {
        CACHE.getRecipeCache().removeRecipeBookEntries(packet);
        return true;
    }
}
