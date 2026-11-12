# Setup

## System Requirements

* A Linux, Windows, or macOS computer that stays on while you want the account online
    * For 24/7 uptime, many people rent a small VPS. See [Hosting Providers](Hosting-Providers.md).
* **Java 21 or newer**
    * Check with `java -version`
    * Builds are available from [Adoptium (Eclipse Temurin)](https://adoptium.net/) and others. On Linux you can also use your package manager or [SDKMAN!](https://sdkman.io/).
* About 300MB of free RAM per instance

??? tip "Not enough RAM on your Linux VPS?"

    You can use part of your disk as (slow) extra memory: [https://linuxize.com/post/create-a-linux-swap-file/](https://linuxize.com/post/create-a-linux-swap-file/)

## Download

Make a new folder for DotProxy and put two files from the latest release in it:

* [`DotProxy.jar`](https://github.com/PakshP/DotProxy/releases/latest/download/DotProxy.jar)
* A start script:
    * [`start.sh`](https://github.com/PakshP/DotProxy/releases/latest/download/start.sh) for Linux/macOS
    * [`start.bat`](https://github.com/PakshP/DotProxy/releases/latest/download/start.bat) for Windows

Always take the jar and the start script from the same release. All versions are on the [releases page](https://github.com/PakshP/DotProxy/releases). You can also [build it from source](#building-from-source).

??? info "How do I download the files from a Linux terminal?"

    Use [wget](https://linuxize.com/post/wget-command-examples/#how-to-download-a-file-with-wget):

    ```bash
    mkdir ~/dotproxy && cd ~/dotproxy
    wget https://github.com/PakshP/DotProxy/releases/latest/download/DotProxy.jar
    wget https://github.com/PakshP/DotProxy/releases/latest/download/start.sh
    chmod +x start.sh
    ```

    On macOS, use `curl -LO <url>` instead of `wget <url>`.

??? tip "Windows quick start"

    1. Install Java 21. In PowerShell or Windows Terminal, run:

        ```
        winget install EclipseAdoptium.Temurin.21.JRE
        ```

        Or download the JRE 21 installer from [Adoptium](https://adoptium.net/) and run it.

    2. Make a new folder and download [`DotProxy.jar`](https://github.com/PakshP/DotProxy/releases/latest/download/DotProxy.jar) and [`start.bat`](https://github.com/PakshP/DotProxy/releases/latest/download/start.bat) into it.
    3. Double-click `start.bat`. If Windows shows a security warning for the downloaded file, choose to run it anyway.
    4. Continue with [First-Time Configuration](#first-time-configuration) in the window that opens.

## Running DotProxy

Open a terminal in the DotProxy folder and run the start script:

* Linux/macOS: `./start.sh` (run `chmod +x start.sh` first if needed)
* Windows: `start.bat`, or double-click it

The start script runs DotProxy in a loop, so DotProxy starts again automatically after a crash, or when you run the [`restart`](Commands.md#restart) command (for example after [updating](Maintenance.md#updating)). When DotProxy shuts down on purpose, for example with the [`shutdown`](Commands.md#shutdown) command, the script exits as well.

You can also run DotProxy directly, but then it won't restart itself:

```bash
java -jar DotProxy.jar
```

??? tip "Custom Java path or JVM arguments"

    Both start scripts read two environment variables:

    * `JAVA`: the path to the `java` executable to use (default: `java` from your `PATH`)
    * `JVM_ARGS`: JVM arguments. Setting this replaces the defaults, which include a 300MB heap limit (`-Xmx300m`).

??? tip "How to stop a running instance"

    Press `Ctrl + C` in the terminal, or run the `shutdown` command. To restart instead, run `restart`.

    If your terminal doesn't respond, close the window.

On first run, DotProxy creates `config.json` in the folder you run it from. All settings are saved there. You don't need to edit it by hand: every setting can be changed with commands.

## First-Time Configuration

Type these commands into the DotProxy terminal.

### 1. Set the server to connect to

By default DotProxy connects to `localhost:25565`. Point it at your server:

```
server <address>
server <address> <port>
```

Example: `server play.example.com`

### 2. Whitelist yourself

Only whitelisted players can join the proxy. Add your Minecraft username:

```
whitelist add <username>
```

Anyone you whitelist can take control of the account, so you can share it without sharing the password. To let someone **watch** without taking control, use the spectator whitelist instead:

```
spectator whitelist add <username>
```

### 3. Log in and connect

```
connect
```

The first time, DotProxy starts a Microsoft **device code** login. It prints a link and a code in the terminal (and in Discord, if you've set it up). Open the link in a browser, enter the code, and sign in to the Microsoft account that owns the Minecraft account.

The login is cached, so you won't need to sign in again on later connects. To switch accounts, see the [FAQ](FAQ.md#switch-accounts).

??? tip "Login problems"

    * If the Microsoft site signs you in to the wrong account automatically, open the link in a private/incognito window.
    * If login keeps failing, sign in with the official Minecraft launcher, join any server, then try again.
    * See the [`auth`](Commands.md#auth) command for other login options.

### 4. Join the proxy in-game

Run `status` and look for **Proxy IP**. That's the address to use in Minecraft.

In your Minecraft client, add a server with that address. If DotProxy runs on the same computer, use `localhost:25565`.

When you join, you take control of the account. When you leave, DotProxy keeps the account online and AFKing.

The proxy listens on port `25565` by default. To change it, or the address shown to players, use [`serverConnection`](Commands.md#serverconnection):

```
serverConnection port <port>
serverConnection proxyIP <address>
```

If you can't connect, run `connectionTest` and see [the FAQ](FAQ.md#i-cant-connect-to-my-proxy).

### 5. (Optional) Discord bot

You can control DotProxy from Discord, relay chat, and get alerts. See the [Discord Bot Guide](Discord-Bot-Guide.md).

### 6. (Optional) Modules

Turn on the modules you need. For example:

* [`autoReconnect`](Commands.md#autoreconnect): reconnect after disconnects
* [`autoRespawn`](Commands.md#autorespawn): respawn after death
* [`antiAFK`](Commands.md#antiafk): avoid AFK kicks
* [`autoDisconnect`](Commands.md#autodisconnect): disconnect on low health or when players come near
* [`activeHours`](Commands.md#activehours): connect at scheduled times
* [`visualRange`](Commands.md#visualrange): alerts when players come into view

Use `help <command>` to see each command's options.

## Using Commands

DotProxy commands work in three places:

| Where    | Prefix               | Example |
|----------|----------------------|---------|
| Terminal | none                 | `help`  |
| In-game  | `/` or `!`           | `/help` |
| Discord  | `.`                  | `.help` |

[Full Commands Documentation](Commands.md){ .md-button .md-button--primary }
[Frequently Asked Questions](FAQ.md){ .md-button .md-button--primary }

## Connecting to Other Minecraft Versions

DotProxy has ViaVersion built in. By default it can connect to (almost) any server version, and players can join with (almost) any client version.

You can change this with the [`via`](Commands.md#via) command.

## Keeping DotProxy Running

To check on DotProxy, update it, back it up, or fix problems later on, see [Running & Maintaining](Maintenance.md).

## Running on Linux Servers

See the [Linux Guide](Linux-Guide.md) for a full walkthrough, from SSH to starting on boot.

If you start DotProxy over SSH, use a terminal multiplexer. Otherwise **DotProxy stops when you close your SSH session.**

* (Recommended) [tmux](https://tmuxcheatsheet.com/how-to-install-tmux/)
* [screen](https://linuxize.com/post/how-to-use-linux-screen/)

You can also run DotProxy as a [systemd service](Linux-Guide.md#6-optional-start-on-boot-with-systemd), or with [Docker](Docker.md).

## Running Multiple Instances

Make a separate folder for each instance, each with its own copy of `DotProxy.jar` and the start script.

Each instance is run and configured on its own: separate terminal sessions, config files, proxy ports, Discord bots, and so on. Give each instance a different port with `serverConnection port <port>`.

## Using a Domain Name

To let players connect with a domain name instead of an IP address, add these DNS records with your DNS provider:

* An `A` record (for example `proxy.example.com`) pointing to your server's public IP address.
* Optional, only needed if you don't use the default port `25565`: an `SRV` record with:
    * Service: `_minecraft`
    * Protocol: `TCP`
    * Name: the name players will type (for example `mc`, which becomes `mc.example.com`)
    * Port: your DotProxy port
    * Target: the `A` record above (for example `proxy.example.com`)

Then set the address shown to players: `serverConnection proxyIP <domain>`

## Building from Source

```bash
git clone https://github.com/PakshP/DotProxy.git
cd DotProxy
./gradlew build
```

The jar is written to `build/libs/DotProxy.jar`. On Windows, use `gradlew.bat build`.

To run a development instance in `./run`, use `./gradlew run`.
