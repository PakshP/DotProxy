package com.dotproxy.network.client.handler.incoming.inventory;

import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.ClientEventLoopPacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundUpdateRecipesPacket;
import org.jspecify.annotations.NonNull;

import static com.dotproxy.Globals.CACHE;

public class UpdateRecipesHandler implements ClientEventLoopPacketHandler<ClientboundUpdateRecipesPacket, ClientSession> {
    @Override
    public boolean applyAsync(@NonNull ClientboundUpdateRecipesPacket packet, @NonNull ClientSession session) {
        CACHE.getRecipeCache().setRecipeRegistry(packet);
        return true;
    }
}
