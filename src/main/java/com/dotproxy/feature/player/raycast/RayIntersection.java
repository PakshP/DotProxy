package com.dotproxy.feature.player.raycast;

import com.dotproxy.mc.block.Direction;

public record RayIntersection(double x, double y, double z, Direction intersectingFace) {
}
