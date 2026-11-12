package com.dotproxy.network.client.handler.incoming.inventory;

import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.ClientEventLoopPacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundRecipeBookAddPacket;

import static com.dotproxy.Globals.CACHE;

public class RecipeBookAddHandler implements ClientEventLoopPacketHandler<ClientboundRecipeBookAddPacket, ClientSession> {
    @Override
    public boolean applyAsync(final ClientboundRecipeBookAddPacket packet, final ClientSession session) {
        CACHE.getRecipeCache().addRecipeBookEntries(packet);
        return true;
    }
}
