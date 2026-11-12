package com.dotproxy;

import com.google.gson.Gson;
import com.dotproxy.util.config.Config;
import lombok.SneakyThrows;

import java.nio.file.Files;

public class TestUtils {

    @SneakyThrows
    public static void setConfigFile(Config config) {
        var tempConfigFile = Files.createTempFile("dotproxy-test-config", ".json");
        tempConfigFile.toFile().deleteOnExit();
        try (var writer = Files.newBufferedWriter(tempConfigFile)) {
            new Gson().toJson(config, writer);
        }
        System.setProperty("dotproxy.config.file", tempConfigFile.toAbsolutePath().toString());
    }
}
