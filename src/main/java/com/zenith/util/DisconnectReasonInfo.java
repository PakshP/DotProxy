package com.zenith.util;

import com.zenith.module.impl.ActiveHours;
import com.zenith.module.impl.AutoDisconnect;
import lombok.experimental.UtilityClass;

import static com.zenith.Globals.DEFAULT_LOG;
import static com.zenith.util.DisconnectMessages.*;
import static com.zenith.util.DisconnectReasonInfo.DisconnectCategory.*;

@UtilityClass
public class DisconnectReasonInfo {

    public enum DisconnectCategory {
        KICK,
        CONNECTION_ISSUE_PLAYER,
        CONNECTION_ISSUE,
        MANUAL,
        ZENITH_MODULE,
        SERVER_RESTART,
        SOCKS_PROXY,
        AUTHENTICATION_FAIL,
        AUTHENTICATION_RATE_LIMIT,
        ALREADY_CONNECTED,
        ILLEGAL_DISCONNECT
    }

    public DisconnectCategory getDisconnectCategory(final String reason) {
        if (reason.startsWith("You have lost connection to the server")) {
            return KICK;
        } else if (reason.startsWith("Read timed out.")) {
            return CONNECTION_ISSUE;
        } else if (reason.startsWith("An internal error occurred in your connection")) {
            return CONNECTION_ISSUE;
        } else if (reason.startsWith(LOGIN_FAILED)) {
            return AUTHENTICATION_FAIL;
        } else if (reason.equals(MANUAL_DISCONNECT)) {
            return MANUAL;
        } else if (AutoDisconnect.isAutoDisconnectReason(reason)) {
            return ZENITH_MODULE;
        } else if (reason.equals(SYSTEM_DISCONNECT)) {
            return ZENITH_MODULE;
        } else if (reason.equals(MAX_PT_DISCONNECT)) {
            return ZENITH_MODULE;
        } else if (ActiveHours.isActiveHoursDisconnect(reason)) {
            return ZENITH_MODULE;
        } else if (reason.equals(SERVER_RESTARTING)) {
            return SERVER_RESTART;
        } else if (reason.startsWith("io.netty.handler.proxy.ProxyConnectException")) {
            return SOCKS_PROXY;
        } else if (reason.startsWith("Authentication servers are down")) {
            return AUTHENTICATION_RATE_LIMIT;
        } else if (reason.startsWith("Connection closed")) {
            return CONNECTION_ISSUE;
        } else if (reason.startsWith("Server closed")) {
            return SERVER_RESTART;
        } else if (reason.startsWith("You are logging in too fast")) {
            return AUTHENTICATION_RATE_LIMIT;
        } else if (reason.startsWith("Connection timed out")) {
            return CONNECTION_ISSUE_PLAYER;
        } else if (reason.startsWith("Proxy shutting down")) {
            return SERVER_RESTART;
        } else if (reason.startsWith("You are already connected to this proxy")) {
            return ALREADY_CONNECTED;
        } else if (reason.startsWith("Illegal characters in chat")) {
            return ILLEGAL_DISCONNECT;
        }
        DEFAULT_LOG.debug("Unknown disconnect reason category for: {}", reason);
        return CONNECTION_ISSUE;
    }

}
