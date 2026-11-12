package com.zenith.feature.api;

import com.zenith.feature.api.mcstatus.MCStatusApi;
import com.zenith.feature.api.mcstatus.model.MCStatusResponse;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MCStatusApiTest {

//    @Test
    public void test() {
        Optional<MCStatusResponse> responseOptional = MCStatusApi.INSTANCE.getMCServerStatus("mc.hypixel.net");
        assertTrue(responseOptional.isPresent());
        var response = responseOptional.get();
        assertTrue(response.online());
    }
}
