package com.dotproxy.network.server.handler.shared.postoutgoing;

import com.dotproxy.network.codec.PostOutgoingPacketHandler;
import com.dotproxy.network.server.ServerSession;
import net.kyori.adventure.text.Component;
import org.geysermc.mcprotocollib.protocol.packet.common.clientbound.ClientboundTransferPacket;

public class TransferPostOutgoingHandler implements PostOutgoingPacketHandler<ClientboundTransferPacket, ServerSession> {
    @Override
    public void accept(final ClientboundTransferPacket packet, final ServerSession session) {
        session.disconnect(Component.text("Transferring to " + packet.getHost() + ":" + packet.getPort()));
    }
}
