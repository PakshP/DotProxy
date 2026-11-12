# Linux Guide

This page walks through hosting DotProxy on a Linux server (for example a VPS) from start to finish. If you don't have a server yet, see [Hosting Providers](Hosting-Providers.md).

The commands below are for Ubuntu and Debian, with Fedora and Rocky Linux versions where they differ. Most other distributions work just as well. The main differences are the package manager and which applications come pre-installed.

## 1. Connect over SSH

From your own computer, connect to the server:

```bash
ssh <user>@<server-ip>
```

Your hosting provider tells you the username and IP address. On Windows, `ssh` works in PowerShell or Windows Terminal.

??? tip "Optional: run DotProxy as its own user"

    Running DotProxy under a separate user keeps it away from the rest of the system. Create the user (Ubuntu/Debian: `sudo adduser dotproxy`; Fedora/Rocky: `sudo useradd -m dotproxy`) and switch to it when you work in the DotProxy folder:

    ```bash
    sudo -iu dotproxy
    ```

    This user doesn't need `sudo`. Do the steps that need `sudo` (installing packages, the firewall, and systemd) from your normal account. Type `exit` to switch back.

## 2. Install Java and tmux

DotProxy needs **Java 21 or newer**. [tmux](#recommended-command-line-tools) keeps DotProxy running after you close SSH.

Ubuntu/Debian:

```bash
sudo apt update
sudo apt install openjdk-21-jre-headless tmux
java -version
```

Fedora/Rocky Linux:

```bash
sudo dnf install java-21-openjdk-headless tmux
java -version
```

Any Java version from 21 upwards works. If your distribution doesn't package Java 21, use [SDKMAN!](https://sdkman.io/) or download a build from [Adoptium](https://adoptium.net/).

## 3. Download DotProxy

Make a folder and download the jar and the start script from the latest release:

```bash
mkdir ~/dotproxy && cd ~/dotproxy
wget https://github.com/PakshP/DotProxy/releases/latest/download/DotProxy.jar
wget https://github.com/PakshP/DotProxy/releases/latest/download/start.sh
chmod +x start.sh
```

## 4. Open the Firewall

Players connect to the proxy on TCP port `25565` by default (change it with `serverConnection port <port>`). That port has to be open in up to two firewalls.

### Cloud provider firewall

Most cloud providers block incoming traffic in their own firewall, which you configure on their website. See [I can't connect to my proxy!](FAQ.md#i-cant-connect-to-my-proxy) in the FAQ, or the "Firewall Help" sections in [Hosting Providers](Hosting-Providers.md), for Oracle Cloud, AWS, and Google Cloud.

### OS firewall

Ubuntu uses `ufw`. Check whether it's on with `sudo ufw status`. If it says `active`, allow the port:

```bash
sudo ufw allow 25565/tcp
```

!!! warning

    If you turn `ufw` on yourself, run `sudo ufw allow OpenSSH` first, or you'll lock yourself out of SSH.

Fedora and Rocky Linux use `firewalld`:

```bash
sudo firewall-cmd --add-port=25565/tcp --permanent && sudo firewall-cmd --reload
```

If you use a different port, replace `25565` with it.

## 5. First Run in tmux

Start a tmux session and run DotProxy inside it:

```bash
tmux new -s dotproxy
./start.sh
```

Now do the [first-time configuration](Setup.md#first-time-configuration): set the server, whitelist yourself, and `connect`. Setting up the [Discord bot](Discord-Bot-Guide.md) now is a good idea too, so you can manage DotProxy without SSH.

When you're done, detach from tmux with `Ctrl + B`, then `D`. DotProxy keeps running after you close SSH. Reattach later with:

```bash
tmux attach -t dotproxy
```

If you're happy running DotProxy in tmux, you're done. Note that it won't start again by itself if the server reboots. For that, use systemd (below).

## 6. (Optional) Start on Boot with systemd

systemd can start DotProxy when the server boots and bring it back if it exits. Do the first run in tmux (above) first, then stop that instance with `shutdown`, since only one DotProxy can run in a folder at a time.

Create `/etc/systemd/system/dotproxy.service` (for example with `sudo nano /etc/systemd/system/dotproxy.service`) and replace `youruser` and the paths:

```ini
[Unit]
Description=DotProxy
After=network-online.target
Wants=network-online.target

[Service]
User=youruser
WorkingDirectory=/home/youruser/dotproxy
ExecStart=/usr/bin/java -Xmx300m -XX:+UseG1GC --enable-native-access=ALL-UNNAMED -jar DotProxy.jar
Restart=always
RestartPreventExitStatus=69

[Install]
WantedBy=multi-user.target
```

If `java` isn't at `/usr/bin/java`, find the right path with `which java`.

Then enable and start it, and follow its output:

```bash
sudo systemctl daemon-reload
sudo systemctl enable --now dotproxy
journalctl -u dotproxy -f
```

How it behaves:

* `restart` works: DotProxy exits and systemd starts it again.
* `shutdown` keeps it stopped (that's what `RestartPreventExitStatus=69` does) until you run `sudo systemctl start dotproxy` or the server reboots.
* A crash or any other exit is restarted automatically.

A systemd service has no terminal you can type into. Run commands through the [Discord bot](Discord-Bot-Guide.md) or in-game instead (for example `/status`).

Useful commands:

```bash
systemctl status dotproxy        # is it running?
sudo systemctl restart dotproxy  # restart
sudo systemctl stop dotproxy     # stop
journalctl -u dotproxy -f        # follow the output
```

## 7. Keeping It Running

See [Running & Maintaining](Maintenance.md) for updating, backups, logs, moving to another server, and troubleshooting.

## Recommended Command-line Tools

* tmux: a terminal multiplexer. You can open and detach terminal sessions, so programs keep running after you disconnect from SSH.
    * [tmux commands cheat sheet](https://tmuxcheatsheet.com/)
* nano or vim: terminal text editors
    * [vim commands cheat sheet](https://devhints.io/vim)
* sdkman: manages the Java versions installed on your system
* wget: downloads files from the internet

## Terminal Basics

* Install applications on Ubuntu: `sudo apt install tmux`
* `ssh <user>@<ip>` connects to your server remotely
* `cd <directory>` changes the current directory
* `ls` lists the current directory's contents
* `Ctrl + C` closes the current application
* `mv <file> <destination>` moves a file to a destination
* `cp <file> <destination>` copies a file to a destination
* `mkdir <dir name>` creates a directory
* `wget <url>` downloads a file from the internet
