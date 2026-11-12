# Linux Guide

## Linux Distributions

Ubuntu is a good choice, but most distributions work just as well, such as Debian, Fedora, or Rocky Linux.

The main differences are the package manager and which applications come pre-installed.

For example, Ubuntu and Debian use the `apt` package manager.

## Installing Java

DotProxy needs **Java 21 or newer**.

On Ubuntu/Debian:

```bash
sudo apt update
sudo apt install openjdk-21-jre-headless
java -version
```

If your distribution doesn't package Java 21, use [SDKMAN!](https://sdkman.io/) or download a build from [Adoptium](https://adoptium.net/).

## Quick Setup

```bash
mkdir ~/dotproxy && cd ~/dotproxy
wget https://github.com/PakshP/DotProxy/releases/latest/download/DotProxy.jar
wget https://raw.githubusercontent.com/PakshP/DotProxy/main/scripts/start.sh
chmod +x start.sh
tmux new -s dotproxy
./start.sh
```

Detach from tmux with `Ctrl + B`, then `D`. Reattach later with `tmux attach -t dotproxy`.

Then continue with the [Setup guide](Setup.md#first-time-configuration).

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

## Running as a systemd Service (optional)

Instead of tmux, you can have systemd start DotProxy on boot and restart it automatically. Create `/etc/systemd/system/dotproxy.service` (replace `youruser` and the paths):

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

Then run:

```bash
sudo systemctl daemon-reload
sudo systemctl enable --now dotproxy
journalctl -u dotproxy -f
```

Without a terminal you can't type commands directly, so set up the [Discord bot](Discord-Bot-Guide.md) first, or do the initial setup in tmux before switching to systemd.
