package com.dotproxy.network.client.handler.incoming.level;

import com.dotproxy.feature.spectator.SpectatorSync;
import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.ClientEventLoopPacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundForgetLevelChunkPacket;
import org.jspecify.annotations.NonNull;

import static com.dotproxy.Globals.CACHE;

public class ForgetLevelChunkHandler implements ClientEventLoopPacketHandler<ClientboundForgetLevelChunkPacket, ClientSession> {
    @Override
    public boolean applyAsync(@NonNull ClientboundForgetLevelChunkPacket packet, @NonNull ClientSession session) {
        CACHE.getChunkCache().remove(packet.getX(), packet.getZ());
        SpectatorSync.checkSpectatorPositionOutOfRender(packet.getX(), packet.getZ());
        return true;
    }
}
