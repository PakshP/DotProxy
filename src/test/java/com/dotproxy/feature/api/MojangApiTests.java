package com.dotproxy.feature.api;

import com.dotproxy.feature.api.mcprofile.MCProfileApi;
import com.dotproxy.feature.api.mojang.MojangApi;
import com.dotproxy.feature.api.sessionserver.SessionServerApi;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MojangApiTests {

    final MojangApi api = new MojangApi();
    final SessionServerApi sessionServerApi = new SessionServerApi();
    final MCProfileApi mcProfileApi = new MCProfileApi();

//    @Test
    public void getMojangProfileTest() {
        var response = api.getProfile("Notch");
        assertTrue(response.isPresent());
        assertTrue(response.get().name().equals("Notch"));
        var uuid = response.get().uuid();
    }

//    @Test
    public void getSessionServerProfileTest() {
        var response = sessionServerApi.getProfile(UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5"));
        assertTrue(response.isPresent());
        assertTrue(response.get().name().equals("Notch"));
        var uuid = response.get().uuid();
    }

//    @Test
    public void getMCProfileTest() {
        var response = mcProfileApi.getJavaProfile("Notch");
        assertTrue(response.isPresent());
        assertTrue(response.get().name().equals("Notch"));
        var uuid = response.get().uuid();
    }
}
