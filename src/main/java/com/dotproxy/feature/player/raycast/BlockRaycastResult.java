package com.dotproxy.feature.player.raycast;

import com.dotproxy.mc.block.Block;
import com.dotproxy.mc.block.BlockRegistry;
import com.dotproxy.mc.block.Direction;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public record BlockRaycastResult(boolean hit, int x, int y, int z, @Nullable RayIntersection intersection, Block block) {
    public static BlockRaycastResult miss() {
        return new BlockRaycastResult(false, 0, 0, 0, null, BlockRegistry.AIR);
    }

    public @NonNull Direction direction() {
        return intersection == null ? Direction.UP : intersection.intersectingFace();
    }
}
