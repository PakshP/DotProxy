package com.dotproxy;

import com.dotproxy.via.ProtocolVersionDetector;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PingTest {

//    @Test
    public void constPing() {
        // testing ping srv dns resolver
        assertEquals(762, ProtocolVersionDetector.getProtocolVersion("constantiam.net", 25565));
    }

//    @Test
    public void nineb9tPing() {
        // testing ping srv dns resolver
        assertEquals(757, ProtocolVersionDetector.getProtocolVersion("9b9t.com", 25565));
    }
}
