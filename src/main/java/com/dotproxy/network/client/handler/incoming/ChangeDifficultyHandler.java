package com.dotproxy.network.client.handler.incoming;

import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.ClientEventLoopPacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundChangeDifficultyPacket;

import static com.dotproxy.Globals.CACHE;

public class ChangeDifficultyHandler implements ClientEventLoopPacketHandler<ClientboundChangeDifficultyPacket, ClientSession> {
    @Override
    public boolean applyAsync(final ClientboundChangeDifficultyPacket packet, final ClientSession session) {
        CACHE.getPlayerCache().setDifficulty(packet.getDifficulty());
        CACHE.getPlayerCache().setDifficultyLocked(packet.isDifficultyLocked());
        return true;
    }
}
