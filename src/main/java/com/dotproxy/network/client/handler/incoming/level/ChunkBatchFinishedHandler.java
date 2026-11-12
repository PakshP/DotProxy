package com.dotproxy.network.client.handler.incoming.level;

import com.dotproxy.Proxy;
import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.PacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundChunkBatchFinishedPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.level.ServerboundChunkBatchReceivedPacket;

public class ChunkBatchFinishedHandler implements PacketHandler<ClientboundChunkBatchFinishedPacket, ClientSession> {
    @Override
    public ClientboundChunkBatchFinishedPacket apply(final ClientboundChunkBatchFinishedPacket packet, final ClientSession session) {
        if (!Proxy.getInstance().hasActivePlayer()) {
            session.sendAsync(new ServerboundChunkBatchReceivedPacket(64)); // max allowed by server
        }
        return packet;
    }
}
