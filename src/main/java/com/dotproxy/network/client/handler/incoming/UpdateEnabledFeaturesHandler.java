package com.dotproxy.network.client.handler.incoming;

import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.ClientEventLoopPacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.configuration.clientbound.ClientboundUpdateEnabledFeaturesPacket;

import static com.dotproxy.Globals.CACHE;

public class UpdateEnabledFeaturesHandler implements ClientEventLoopPacketHandler<ClientboundUpdateEnabledFeaturesPacket, ClientSession> {
    @Override
    public boolean applyAsync(final ClientboundUpdateEnabledFeaturesPacket packet, final ClientSession session) {
        CACHE.getConfigurationCache().setEnabledFeatures(packet.getFeatures());
        return true;
    }
}
