package com.dotproxy.command.api;

@FunctionalInterface
public interface CommandExecutionExceptionHandler {
    void handle(CommandContext context, Throwable exception);
}
