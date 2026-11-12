package com.zenith.feature.ping;

import com.zenith.feature.ping.data.MCResponse;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;

public class PingTest {
//    @Test
    public void test() throws IOException {
        final MCResponse pingWithDetails = MCPing.INSTANCE.ping("mc.hypixel.net", 25565, 3000, true);
        Assertions.assertNotNull(pingWithDetails.version());
        System.out.println("Online players: " + pingWithDetails.players().online());
    }

//    @Test
    public void protocolVersionDetectorTest() throws IOException {
        var version = MCPing.INSTANCE.getProtocolVersion("mc.hypixel.net", 25565, 3000, true);
        Assertions.assertTrue(version > 0);
    }
}
