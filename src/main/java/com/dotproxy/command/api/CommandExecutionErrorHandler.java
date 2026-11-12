package com.dotproxy.command.api;

@FunctionalInterface
public interface CommandExecutionErrorHandler {
    void handle(CommandContext context);
}
