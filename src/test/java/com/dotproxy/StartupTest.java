package com.dotproxy;

import com.dotproxy.feature.ping.MCPing;
import com.dotproxy.util.Wait;
import com.dotproxy.util.config.Config;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(TestLogCaptureJunitExtension.class)
@DisabledInNativeImage
public class StartupTest {
    @Test
    public void launchDotProxyServer() {
        var launchThread = new AtomicReference<Thread>();
        try {
            var config = new Config();
            config.interactiveTerminal.enable = false;
            config.server.bind.port = 0;
            TestUtils.setConfigFile(config);

            launchThread.set(Thread.ofPlatform().daemon().start(Proxy::main));

            assertTrue(Wait.waitUntil(() ->
                        !launchThread.get().isAlive()
                            || (Proxy.getInstance().getServer() != null && Proxy.getInstance().getServer().isListening()),
                    10),
                "Failed to start DotProxy server"
            );

            try {
                var response = MCPing.INSTANCE.ping("localhost", Proxy.getInstance().getServer().getPort(), 5000, false);
                assertEquals("DotProxy", response.version().name());
            } catch (Exception e) {
                fail("Failed to ping local DotProxy mc server", e);
            }
        } finally {
            var t = launchThread.get();
            if (t != null) {
                t.interrupt();
            }
        }
    }
}
