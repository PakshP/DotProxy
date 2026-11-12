package com.dotproxy.feature.api.mcprofile.model;

import com.dotproxy.feature.api.ProfileData;

import java.util.UUID;

public record MCProfileJavaResponse(
    String username,
    UUID uuid,
    String skin,
    String cape,
    boolean linked
) implements ProfileData {
    @Override
    public String name() {
        return username();
    }
}
