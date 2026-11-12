package com.dotproxy.network.client.handler.incoming.inventory;

import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.ClientEventLoopPacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundRecipeBookSettingsPacket;

import static com.dotproxy.Globals.CACHE;

public class RecipeBookSettingsHandler implements ClientEventLoopPacketHandler<ClientboundRecipeBookSettingsPacket, ClientSession> {
    @Override
    public boolean applyAsync(final ClientboundRecipeBookSettingsPacket packet, final ClientSession session) {
        CACHE.getRecipeCache().setRecipeBookSettings(packet);
        return true;
    }
}
