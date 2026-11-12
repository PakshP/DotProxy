# Running & Maintaining

This page covers keeping DotProxy running after the first setup: checking on it, restarting, updating, backups, moving to another machine, and fixing common problems.

If you haven't set DotProxy up yet, start with [Setup](Setup.md). For a full Linux VPS walkthrough, see the [Linux Guide](Linux-Guide.md).

## Files in the DotProxy Folder

| File | What it is | Secret? |
|------|------------|---------|
| `DotProxy.jar` | The proxy itself | |
| `start.sh` / `start.bat` | The start script, which restarts DotProxy when it asks to | |
| `config.json` | All settings | **Yes.** Contains your Discord bot token if you set one (and your account email and password if you used that login type) |
| `mc_auth_cache.json` | Your Minecraft account's login tokens | **Yes. Never share it.** Anyone with this file can log in as your account |
| `log/` | `latest.log` and `debug.log`. Older logs are rotated daily into `log/proxy-YYYY-MM-DD.N.log.zip` files, capped at about 250MB | |
| `plugins/` | Plugin jars, with their settings in `plugins/config` | |
| `viaversion.yml`, `viabackwards.yml`, `viarewind.yml` | ViaVersion settings | |
| `server-icon.png` | Optional. A custom 64x64 icon shown in the server list | |
| `.lock` | Stops two DotProxy instances from running in the same folder | |

!!! warning

    Never post `config.json` or `mc_auth_cache.json` anywhere, including when asking for help. If you think either one leaked, reset your Discord bot token and run `auth clear`, then `connect` to log in again.

## Checking It's Running

* Run [`status`](Commands.md#status) in-game, in the terminal, or on Discord. It shows the DotProxy version, whether the account is connected, and how long it has been online.
* If you run DotProxy in tmux, reattach to see the terminal: `tmux attach -t dotproxy` (detach again with `Ctrl + B`, then `D`).
* If you run it as a systemd service:

    ```bash
    systemctl status dotproxy
    journalctl -u dotproxy -f
    ```

* Follow the log file from any terminal:

    ```bash
    tail -f log/latest.log
    ```

## Starting, Stopping, and Restarting

| To... | Do this |
|-------|---------|
| Start | `./start.sh` (Linux/macOS), `start.bat` (Windows), or `sudo systemctl start dotproxy` if you use [systemd](Linux-Guide.md#6-optional-start-on-boot-with-systemd) |
| Restart | Run the [`restart`](Commands.md#restart) command. DotProxy exits, and the start script or systemd starts it again a few seconds later |
| Stop and keep it stopped | Run [`shutdown`](Commands.md#shutdown) (alias `exit`), or press `Ctrl + C` in the DotProxy terminal |

`restart` and `shutdown` can only be run by the account owner. They work in the terminal, in-game, and on Discord.

`restart` relies on a start script or systemd to bring DotProxy back. If you started it with plain `java -jar DotProxy.jar`, `restart` just stops it.

!!! info "`disconnect` is not `shutdown`"

    [`disconnect`](Commands.md#disconnect) and [`connect`](Commands.md#connect) only drop or rejoin the Minecraft server. DotProxy itself keeps running, so you can still run commands and join the proxy.

??? tip "Stopping a systemd service"

    `shutdown` keeps a systemd service stopped until you run `sudo systemctl start dotproxy`, or until the machine reboots (if the service is enabled). You can also use `sudo systemctl stop dotproxy` and `sudo systemctl restart dotproxy`.

## Updating

New versions are published on the [releases page](https://github.com/PakshP/DotProxy/releases). To get notified, open the [GitHub repository](https://github.com/PakshP/DotProxy) and click **Watch → Custom → Releases**.

Your settings, login, and plugins are kept when you update. Only `DotProxy.jar` is replaced.

### Linux

You can replace the jar while DotProxy is running. In the DotProxy folder:

```bash
wget -O DotProxy.jar.new https://github.com/PakshP/DotProxy/releases/latest/download/DotProxy.jar && mv DotProxy.jar.new DotProxy.jar
```

Then run `restart` (in the terminal, in-game, or on Discord). When it comes back, check the version with `status`.

Downloading to `DotProxy.jar.new` first means a failed download never leaves you with a broken jar.

### Windows

Windows locks `DotProxy.jar` while it's running, so stop it first:

1. Run `shutdown`.
2. Download the new `DotProxy.jar` from the [releases page](https://github.com/PakshP/DotProxy/releases/latest/download/DotProxy.jar) and replace the old one in the DotProxy folder.
3. Run `start.bat` again.

### Docker

See [Docker](Docker.md#updating).

??? tip "Updating the start script"

    The start scripts rarely change. If a release notes a change, download it the same way: [`start.sh`](https://github.com/PakshP/DotProxy/releases/latest/download/start.sh) or [`start.bat`](https://github.com/PakshP/DotProxy/releases/latest/download/start.bat). On Linux, run `chmod +x start.sh` again after downloading.

## Backups

Everything you'd need to restore DotProxy is in three places: `config.json`, `mc_auth_cache.json`, and `plugins/`. Everything else can be downloaded again.

On Linux, from the DotProxy folder:

```bash
tar czf dotproxy-backup-$(date +%F).tar.gz config.json mc_auth_cache.json plugins/
```

On Windows, copy those files and the `plugins` folder somewhere safe.

Backups contain your login tokens, so keep them as private as the originals.

??? tip "Automatic daily backups with cron"

    Run `crontab -e` and add this line to back up every day at 04:00 into `~/backups` (create that folder first with `mkdir ~/backups`). Change `~/dotproxy` if your DotProxy folder is somewhere else:

    ```
    0 4 * * * cd ~/dotproxy && tar czf ~/backups/dotproxy-backup-$(date +\%F).tar.gz config.json mc_auth_cache.json plugins/
    ```

    The `%` has to be written as `\%` in a crontab.

    To delete backups older than 30 days, add:

    ```
    30 4 * * * find ~/backups -name 'dotproxy-backup-*.tar.gz' -mtime +30 -delete
    ```

## Moving to a New Server

1. Install Java 21+ on the new machine. See the [Linux Guide](Linux-Guide.md#2-install-java-and-tmux) or [Setup](Setup.md#system-requirements).
2. Run `shutdown` on the old machine. Don't run two copies on the same account at once: they will keep kicking each other off the server.
3. Copy the whole DotProxy folder to the new machine (the `log/` folder is optional). For example, from the old machine:

    ```bash
    rsync -a ~/dotproxy/ <user>@<new-server-ip>:~/dotproxy/
    ```

    `scp -r ~/dotproxy <user>@<new-server-ip>:~/` works too.

4. Open the proxy's port on the new machine. See [the firewall step](Linux-Guide.md#4-open-the-firewall).
5. Start it with the start script (or set up systemd again).
6. If you set an address with `serverConnection proxyIP`, or use a domain name, point it to the new server's IP.

## Changing Memory or the Java Version

By default the start scripts limit DotProxy to 300MB of heap memory (`-Xmx300m`). That's enough for most setups. You may want more if you run many plugins.

The start scripts read two environment variables:

* `JVM_ARGS`: JVM arguments. Setting this **replaces** all the defaults, so include `-Xmx` yourself.
* `JAVA`: the path to the `java` executable to use (default: `java` from your `PATH`).

=== "Linux/macOS"

    ```bash
    JVM_ARGS="-Xmx500m -XX:+UseG1GC" ./start.sh
    JAVA=/usr/lib/jvm/java-21-openjdk-amd64/bin/java ./start.sh
    ```

=== "Windows"

    In a Command Prompt in the DotProxy folder:

    ```bat
    set JVM_ARGS=-Xmx500m -XX:+UseG1GC
    start.bat
    ```

=== "systemd"

    Edit the `ExecStart` line in `/etc/systemd/system/dotproxy.service`, for example change `-Xmx300m` to `-Xmx500m`, or `/usr/bin/java` to another Java path. Then run:

    ```bash
    sudo systemctl daemon-reload
    sudo systemctl restart dotproxy
    ```

**Updating Java:** Java installed from your package manager updates with the rest of the system:

* Ubuntu/Debian: `sudo apt update && sudo apt upgrade`
* Fedora/Rocky: `sudo dnf upgrade`
* Windows (winget): `winget upgrade EclipseAdoptium.Temurin.21.JRE`

DotProxy keeps using the old Java until it restarts, so run `restart` afterwards.

## Security Checklist

* **Keep the whitelist on.** Only whitelist people you trust: anyone on the [`whitelist`](Commands.md#whitelist) can take control of your account.
* For people who should only watch, use the [spectator whitelist](Commands.md#spectator) (`spectator whitelist add <username>`) instead.
* **Open only the proxy's port** (default `25565`) in your firewall. DotProxy doesn't need any other incoming ports.
* **Keep `mc_auth_cache.json` and your Discord bot token private.** Don't post `config.json` either.
* **Keep the OS and Java updated.** See [Updating Java](#changing-memory-or-the-java-version) above.
* To make the proxy harder to find, see [the FAQ](FAQ.md#who-are-these-unknown-people-pinging-or-trying-to-connect-to-my-proxy).

## Troubleshooting

??? question "I can't connect to the proxy"

    1. Check DotProxy is running (`status`) and note the port it listens on (`serverConnection`).
    2. Run [`connectionTest`](Commands.md#connectiontest).
    3. Make sure the port is open in **both** the OS firewall and your cloud provider's firewall. See [the Linux Guide](Linux-Guide.md#4-open-the-firewall) and [the FAQ](FAQ.md#i-cant-connect-to-my-proxy).
    4. Make sure your username is whitelisted: `whitelist add <username>`.

??? question "The account keeps disconnecting"

    See [Disconnects](Disconnects.md) for what each message means. Turn on [`autoReconnect`](Commands.md#autoreconnect) so DotProxy reconnects on its own.

??? question "Login fails"

    Clear the cached login and log in again:

    ```
    auth clear
    connect
    ```

    `connect` starts a new Microsoft device code login. If it still fails, see the tips under [Log in and connect](Setup.md#3-log-in-and-connect) and the [`auth`](Commands.md#auth) command.

??? question "DotProxy runs out of memory or gets killed"

    * If the log shows `java.lang.OutOfMemoryError`, give Java more memory by raising `-Xmx`. See [Changing Memory](#changing-memory-or-the-java-version).
    * If DotProxy disappears without an error in its log, the OS probably killed it because the machine ran out of RAM. Check with `sudo dmesg | grep -i "killed process"`. Add swap ([guide](https://linuxize.com/post/create-a-linux-swap-file/)), stop other programs, or use a machine with more RAM.

??? question "It says another DotProxy instance is open"

    Only one DotProxy can run in a folder at a time (that's what the `.lock` file is for). You probably have another copy running, often in a forgotten tmux session (`tmux ls`) or as a systemd service (`systemctl status dotproxy`). Stop the other one first.

    To run more than one account, give each its own folder. See [Running Multiple Instances](Setup.md#running-multiple-instances).

??? question "Asking for help"

    Include:

    * What you were doing and what happened
    * The output of `status`
    * `log/latest.log` (and `log/debug.log` if asked)

    **Don't** include `config.json` or `mc_auth_cache.json`.

## Uninstalling

1. Stop DotProxy with `shutdown`.
2. If you set up systemd, remove the service:

    ```bash
    sudo systemctl disable --now dotproxy
    sudo rm /etc/systemd/system/dotproxy.service
    sudo systemctl daemon-reload
    ```

3. Delete the DotProxy folder. This also deletes your saved login.
4. Optionally, close the port you opened (for example `sudo ufw delete allow 25565/tcp`) and delete the Discord bot in the [Discord Developer Portal](https://discord.com/developers/applications).
