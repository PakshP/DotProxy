package com.dotproxy.feature.api;

import com.dotproxy.feature.api.crafthead.CraftheadApi;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CraftheadTest {

    public CraftheadApi api = new CraftheadApi();

//    @Test
    public void getProfileFromUsername() {
        var responseOptional = api.getProfile("Notch");
        assertTrue(responseOptional.isPresent());
        var response = responseOptional.get();
        assertEquals("Notch", response.name());
        assertEquals(UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5"), response.uuid());
    }

//    @Test
    public void getProfileFromUUID() {
        var responseOptional = api.getProfile(UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5"));
        assertTrue(responseOptional.isPresent());
        var response = responseOptional.get();
        assertEquals("Notch", response.name());
        assertEquals(UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5"), response.uuid());
    }

//    @Test
    public void getAvatarFromUsername() {
        var responseOptional = api.getAvatar("Notch");
        assertTrue(responseOptional.isPresent());
        var response = responseOptional.get();
        assertTrue(response.length > 0);
    }

//    @Test
    public void getAvatarFromUUID() {
        var responseOptional = api.getAvatar(UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5"));
        assertTrue(responseOptional.isPresent());
        var response = responseOptional.get();
        assertTrue(response.length > 0);
    }
}
