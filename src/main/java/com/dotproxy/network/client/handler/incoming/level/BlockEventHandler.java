package com.dotproxy.network.client.handler.incoming.level;

import com.dotproxy.feature.player.World;
import com.dotproxy.feature.player.tickable.block_entity.piston.PistonBaseBlock;
import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.ClientEventLoopPacketHandler;
import com.dotproxy.util.math.MathHelper;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.value.PistonValue;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.value.PistonValueType;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundBlockEventPacket;

import static com.dotproxy.Globals.CONFIG;

public class BlockEventHandler implements ClientEventLoopPacketHandler<ClientboundBlockEventPacket, ClientSession> {
    @Override
    public boolean applyAsync(final ClientboundBlockEventPacket packet, final ClientSession session) {
        if (PistonBaseBlock.isPiston(World.getBlock(packet.getX(), packet.getY(), packet.getZ()))
            && packet.getType() instanceof PistonValueType pistonValueType
            && packet.getValue() instanceof PistonValue pistonValue
        ) {
            if (CONFIG.debug.pistonPushSim
                && MathHelper.distance3d(
                    packet.getX(), packet.getY(), packet.getZ(),
                    World.getCurrentPlayerX(), World.getCurrentPlayerY(), World.getCurrentPlayerZ())
                <= CONFIG.debug.pistonPushSimMaxDistanceFromPlayer
            ) {
                PistonBaseBlock.triggerEvent(World.getBlockState(packet.getX(), packet.getY(), packet.getZ()), pistonValueType, pistonValue);
            } else {
                World.removeTickableBlockEntity(packet.getX(), packet.getY(), packet.getZ());
            }
        }
        return true;
    }
}
