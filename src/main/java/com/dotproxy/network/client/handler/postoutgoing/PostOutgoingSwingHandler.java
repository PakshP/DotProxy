package com.dotproxy.network.client.handler.postoutgoing;

import com.dotproxy.event.module.ClientSwingEvent;
import com.dotproxy.feature.spectator.SpectatorSync;
import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.ClientEventLoopPacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundSwingPacket;

import static com.dotproxy.Globals.EVENT_BUS;

public class PostOutgoingSwingHandler implements ClientEventLoopPacketHandler<ServerboundSwingPacket, ClientSession> {
    @Override
    public boolean applyAsync(final ServerboundSwingPacket packet, final ClientSession session) {
        SpectatorSync.sendSwing();
        EVENT_BUS.postAsync(ClientSwingEvent.INSTANCE);
        return true;
    }
}
