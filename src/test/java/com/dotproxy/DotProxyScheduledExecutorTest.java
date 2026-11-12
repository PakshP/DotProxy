package com.dotproxy;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.dotproxy.util.Wait;
import com.dotproxy.util.DotProxyScheduledExecutor;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DotProxyScheduledExecutorTest {

    @Test
    public void testDefaultExceptionHandler() {
        AtomicBoolean defaultHandlerCalled = new AtomicBoolean(false);
        try (var executor = new DotProxyScheduledExecutor(4, new ThreadFactoryBuilder()
            .setNameFormat("DotProxy Scheduled Executor - #%d")
            .setUncaughtExceptionHandler((thread, e) -> {
                defaultHandlerCalled.set(true);
            })
            .build())) {
            executor.execute(() -> {
                throw new RuntimeException("asdf");
            });
            assertTrue(Wait.waitUntil(defaultHandlerCalled::get, 3));
        }
    }
}
