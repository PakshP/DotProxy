# Disconnect Reasons

### What is this?

This page explains what DotProxy's disconnect messages mean and what you can do about them.

### How to Read This Page

Each heading is a category of disconnect.

Under each heading is an example disconnect reason, shown like this:

> Disconnected

followed by an explanation.

??? tip "Reconnect automatically"

    Most disconnects are temporary. Turn on [`autoReconnect`](Commands.md#autoreconnect) so DotProxy reconnects on its own.

## Kicked

> You have lost connection to the server

The destination server kicked the account. The reason depends on the server, but common ones are:

* Flagging the server's anti-cheat
* Being kicked for being AFK. Try the [`antiAFK`](Commands.md#antiafk) module.
* A staff member or plugin kicking the player
* Server-side session or playtime limits

## Manual Disconnect

> Manual Disconnect

Sent by DotProxy when someone runs the `disconnect` command.

Modules that disconnect on purpose can also send it, such as `autoDisconnect`.

## DotProxy Modules

> System disconnect

> AutoDisconnect

General disconnect reason sent by DotProxy modules such as [ActiveHours](Commands.md#activehours) and [AutoDisconnect](Commands.md#autodisconnect).

## Connection Issue

> Read timed out.

DotProxy disconnects if it hasn't received any packets from the destination server for a set time (60 seconds by default). This happens when either your internet connection or the server's has problems.

Use the [`clientConnection`](Commands.md#clientconnection) command to change this behavior (see `help clientConnection`).

> Connection closed

> java.net.SocketException: Connection Reset

> io.netty.channel.unix.Errors$NativeIoException: Broken Pipe

Your operating system closed the connection to the server. Either your connection or the server's had a problem.

## Connection Issue (Server)

> An internal error occurred in your connection

The server closed its connection to you. This can happen for many reasons, but the problem is on the server's side.

## Connection Issue (You)

> Connection timed out

DotProxy couldn't reach the server. Check that:

* the computer running DotProxy has a working internet connection
* the server address and port are correct (`server <address> <port>`)
* the server is online

## Server Restart

> Server restarting

> Server closed

> Proxy shutting down

The destination server is restarting or shutting down. Servers sometimes warn before restarting, but they can also crash without warning.

## SOCKS5 Proxy

> io.netty.handler.proxy.ProxyConnectException

This only affects you if you've set up a SOCKS5 or HTTP proxy with the [`clientConnection`](Commands.md#clientconnection) command.

It can happen if the proxy's credentials are wrong, or simply because proxy connections can be unreliable.

## Authentication Failure

> Login Failed

DotProxy couldn't log in to your Minecraft account. Common causes are wrong credentials, expired credentials, or Microsoft needing you to take some action to unlock your account.

Logging in with the official Minecraft launcher and joining a server often fixes login problems. See also the [`auth`](Commands.md#auth) command.

## Authentication Rate Limiting

> Authentication servers are down

> You are logging in too fast

Microsoft or the server has rate-limited the login, so DotProxy can't log in to your account or join the server right now. Wait a few minutes before trying again.

## Already Connected

> You are already connected to this proxy

> You logged in from another location

The account is already logged into the server somewhere else, for example from your own Minecraft client connecting to the server directly.

## Not Whitelisted

> Not whitelisted

This is shown to a **player** trying to join the proxy, not to the proxy itself. Add the player with `whitelist add <username>`, or `spectator whitelist add <username>` to let them watch only.
