package com.dotproxy.command.api;

@FunctionalInterface
public interface CommandSuccessHandler {
    void handle(CommandContext context);
}
