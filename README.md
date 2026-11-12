# DotProxy

DotProxy is a lightweight AFK proxy for Minecraft. It keeps one Minecraft account online on any server, around the clock.

* AFK farms, mob grinders, and chunk loading keep running while you're offline.
* Join the proxy from your normal Minecraft client to take over the account in-game. When you leave, it carries on AFKing.
* Share an account with friends without sharing the password.
* Control it from a terminal, in-game commands, or a Discord bot.

It works on (almost) any Minecraft server and with (almost) any client version.

<details>
<summary>What is a proxy?</summary>

There are four main components:

1. A player's Minecraft client ("Player Client")
2. DotProxy's Minecraft server ("Proxy Server")
3. DotProxy's Minecraft client ("Proxy Client")
4. The destination Minecraft server ("MC Server")

```
Player MC Client <-> Proxy Server <-> Proxy Client <-> MC Server
```

Players connect to the Proxy Server with a normal Minecraft client, the same way they join any server.
The Proxy Client stays connected to the destination MC server with your account.
The player's packets go to the Proxy Server, which passes them to the Proxy Client, which forwards them to the destination server.

When no player is connected, the Proxy Client keeps the account in the world on its own.

</details>

<details>
<summary>How does it work?</summary>

DotProxy does not use, depend on, or interact with Mojang's Minecraft client or server code.
It implements only the parts of the Minecraft network protocol and player logic it needs.
There is no rendering, lighting engine, or anything similar, which keeps resource usage low.

This also means existing Minecraft mods or server plugins cannot be used with DotProxy. Features have to be written for DotProxy itself, for example as a [DotProxy plugin](docs/wiki/Plugins.md).

DotProxy works mainly at the network packet layer. It can read, modify, cancel, or send packets in either direction at any time.

The client's session and world state is cached and sent to players when they connect, so joining the proxy feels like joining the server directly.
The cached state is also used for the built-in player simulation (physics and AntiAFK), Discord chat relay, alerts, and other features.

</details>

## Features

* Low resource usage: under 300MB of RAM per instance
* Integrated ViaVersion
    * Connect to (almost) any Minecraft server version, and let players join with (almost) any client version
* Microsoft account login using a device code. Your password never touches DotProxy.
* A whitelist, so you can share an account without sharing its password
* Spectator mode: extra whitelisted players can join and watch
* A Discord bot for remote control and notifications
    * Two-way chat relay between the server and a Discord channel
    * Configurable alerts and pings, for example when a player enters visual range or the proxy disconnects
* Commands from the terminal, in-game, or Discord
* Built-in modules:
    * **AntiAFK**: keeps the account from being kicked for idling
    * **AutoReconnect**: reconnects automatically after a disconnect
    * **AutoRespawn**: respawns automatically after death
    * **AutoDisconnect**: disconnects on low health, when a player enters visual range, and on other triggers
    * **ActiveHours**: connects at scheduled times
    * **AntiKick**: kicks controlling players who sit idle
    * **VisualRange**: alerts when players come into view
    * **ChatHistory**: replays recent chat to players when they join
    * **ExtraChat**: extra chat utilities
    * **ActionLimiter**: limits what controlling players are allowed to do
    * **TranslationProbeResponder**: answers sign and anvil translation-key probes like a vanilla client of the version the server sees, while nobody is controlling the account. When you are connected, your own client answers them
* A Java plugin system for adding your own modules and commands

## Quick Start

You need **Java 21 or newer**. On Windows, you can install it with `winget install EclipseAdoptium.Temurin.21.JRE`.

1. Make a new folder and download these files from the [latest release](https://github.com/PakshP/DotProxy/releases/latest) into it:
    * [`DotProxy.jar`](https://github.com/PakshP/DotProxy/releases/latest/download/DotProxy.jar)
    * [`start.sh`](https://github.com/PakshP/DotProxy/releases/latest/download/start.sh) (Linux/macOS) or [`start.bat`](https://github.com/PakshP/DotProxy/releases/latest/download/start.bat) (Windows)

    On Linux:

    ```bash
    mkdir ~/dotproxy && cd ~/dotproxy
    wget https://github.com/PakshP/DotProxy/releases/latest/download/DotProxy.jar
    wget https://github.com/PakshP/DotProxy/releases/latest/download/start.sh
    chmod +x start.sh
    ```

    You can also [build it yourself](#building-from-source).
2. Run the start script (`./start.sh`, or double-click `start.bat` on Windows). You can also run `java -jar DotProxy.jar` directly.
    * The start script brings DotProxy back up after a crash or the `restart` command, and stops when you run `shutdown`.
3. On first run, `config.json` is created in the folder.
4. In the DotProxy terminal:
    * `server <address> [port]` sets the server to connect to. The default is `localhost:25565`.
    * `whitelist add <your MC username>` lets you join the proxy.
    * `connect` logs in and connects. The first time, follow the Microsoft device code login link that's printed.
5. In Minecraft, add a server with the proxy's address (`localhost:25565` if it's on the same PC) and join.

The full guide is on the [Setup](docs/wiki/Setup.md) page.

### Hosting on a Server

To keep the account online 24/7, run DotProxy on a server or VPS:

* [Linux Guide](docs/wiki/Linux-Guide.md): a full walkthrough, from SSH and the firewall to starting on boot with systemd
* [Running & Maintaining](docs/wiki/Maintenance.md): checking it's running, updating, backups, and troubleshooting
* [Hosting Providers](docs/wiki/Hosting-Providers.md): where to rent a server

## Documentation

The documentation lives in [`docs/wiki`](docs/wiki/index.md):

* [Setup](docs/wiki/Setup.md)
* [Running & Maintaining](docs/wiki/Maintenance.md)
* [Linux Guide](docs/wiki/Linux-Guide.md)
* [Commands](docs/wiki/Commands.md)
* [Discord Bot Guide](docs/wiki/Discord-Bot-Guide.md)
* [Plugins](docs/wiki/Plugins.md)
* [Disconnects](docs/wiki/Disconnects.md)
* [FAQ](docs/wiki/FAQ.md)

## Building from Source

[IntelliJ IDEA](https://www.jetbrains.com/idea/) works well for building and running local development instances.

Gradle downloads the Java version needed for compiling automatically.

Useful Gradle tasks:

* `./gradlew build` builds an executable jar at `build/libs/DotProxy.jar`
* `./gradlew run` builds and runs a local development instance in `./run`

On Windows, use `gradlew.bat` instead of `./gradlew`.

## Thanks

DotProxy is built on these open-source projects:

* [MCProtocolLib](https://github.com/GeyserMC/MCProtocolLib)
* [Netty](https://github.com/netty/netty)
* [GraalVM](https://graalvm.org/)
* [ViaVersion](https://github.com/ViaVersion/ViaVersion)
* [RaphiMC's MinecraftAuth](https://github.com/RaphiMC/MinecraftAuth)
* [JDA](https://github.com/discord-jda/JDA)
* [JLine](https://github.com/jline/jline3)
* [Adventure](https://github.com/PaperMC/adventure)
* [Brigadier](https://github.com/Mojang/brigadier)
* And many other open-source libraries

See [licenses/NOTICE.md](licenses/NOTICE.md) for attributions.

## License

DotProxy is licensed under [AGPL-3.0](LICENSE). See [README-LICENSE.md](README-LICENSE.md) for more details.
