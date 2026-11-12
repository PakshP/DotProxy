package com.dotproxy.network.client.handler.incoming;

import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.PacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.common.clientbound.ClientboundResourcePackPopPacket;

import static com.dotproxy.Globals.CACHE;

public class ResourcePackPopHandler implements PacketHandler<ClientboundResourcePackPopPacket, ClientSession> {
    public static final ResourcePackPopHandler INSTANCE = new ResourcePackPopHandler();
    @Override
    public ClientboundResourcePackPopPacket apply(final ClientboundResourcePackPopPacket packet, final ClientSession session) {
        if (packet.getId() == null) {
            CACHE.getConfigurationCache().getResourcePacks().clear();
        } else {
            CACHE.getConfigurationCache().getResourcePacks().remove(packet.getId());
        }
        return packet;
    }
}
