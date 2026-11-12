package com.dotproxy.network.client.handler.incoming;

import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.PacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.configuration.clientbound.ClientboundRegistryDataPacket;

import static com.dotproxy.Globals.CACHE;

public class CRegistryDataHandler implements PacketHandler<ClientboundRegistryDataPacket, ClientSession> {
    @Override
    public ClientboundRegistryDataPacket apply(final ClientboundRegistryDataPacket packet, final ClientSession session) {
        CACHE.getRegistriesCache().initialize(packet.getRegistry(), packet.getEntries());
        return packet;
    }
}
