package com.dotproxy.feature.api.mccompanion.model;

import com.dotproxy.feature.api.ProfileData;

import java.util.UUID;

public record MCCompanionBedrockResponse (
    String gamertag,
    String xuid,
    UUID floodgateuid,
    String skinUrl
) implements ProfileData {

    @Override
    public String name() {
        return "." + gamertag;
    }

    @Override
    public UUID uuid() {
        return floodgateuid;
    }
}
