package com.dotproxy.network.client.handler.incoming;

import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.PacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.common.clientbound.ClientboundUpdateTagsPacket;

import static com.dotproxy.Globals.CACHE;

public class UpdateTagsHandler implements PacketHandler<ClientboundUpdateTagsPacket, ClientSession> {
    public static final UpdateTagsHandler INSTANCE = new UpdateTagsHandler();
    @Override
    public ClientboundUpdateTagsPacket apply(final ClientboundUpdateTagsPacket packet, final ClientSession session) {
        CACHE.getConfigurationCache().setTags(packet.getTags());
        return packet;
    }
}
