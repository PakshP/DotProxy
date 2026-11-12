# Docker

You don't need a custom image to run DotProxy in Docker. The official `eclipse-temurin:21-jre` image works with a mounted folder that holds `DotProxy.jar`. Your `config.json`, login cache, logs, and plugins are saved in that folder, so nothing is lost when the container is recreated.

## Setup

1. Make a folder for the compose file (for example `~/dotproxy-docker`), with a `dotproxy` folder inside it for DotProxy's files, and download `DotProxy.jar` from the latest release into that:

    ```bash
    mkdir -p ~/dotproxy-docker/dotproxy && cd ~/dotproxy-docker
    wget -O dotproxy/DotProxy.jar https://github.com/PakshP/DotProxy/releases/latest/download/DotProxy.jar
    ```

2. Create a `compose.yaml` file in `~/dotproxy-docker` (next to the `dotproxy` folder):

    ```yaml title="compose.yaml"
    services:
      dotproxy:
        image: eclipse-temurin:21-jre
        container_name: dotproxy
        working_dir: /dotproxy
        command: ["java", "-Xmx300m", "-XX:+UseG1GC", "--enable-native-access=ALL-UNNAMED", "-jar", "DotProxy.jar"]
        volumes:
          - ./dotproxy:/dotproxy
        ports:
          - "25565:25565"
        stdin_open: true
        tty: true
        restart: unless-stopped
    ```

3. Start it:

    ```bash
    docker compose up -d
    ```

## Using the Terminal

DotProxy is configured with terminal commands. Attach to the container to type them:

```bash
docker attach dotproxy
```

Detach without stopping the container by pressing `Ctrl + P` and then `Ctrl + Q`.

Then follow the [Setup guide](Setup.md#first-time-configuration) to set your server, whitelist yourself, and `connect`.

The Microsoft login link and code also show up in the container logs:

```bash
docker logs -f dotproxy
```

## Updating

Download the new jar next to the old one, swap it in, and restart the container. From `~/dotproxy-docker`:

```bash
wget -O dotproxy/DotProxy.jar.new https://github.com/PakshP/DotProxy/releases/latest/download/DotProxy.jar && mv dotproxy/DotProxy.jar.new dotproxy/DotProxy.jar
docker compose restart
```

Instead of `docker compose restart`, you can also run the `restart` command in-game or on Discord. Docker starts DotProxy again with the new jar. Check the version with `status`.

To update Java as well, pull the latest image and recreate the container:

```bash
docker compose pull
docker compose up -d
```

For backups and other upkeep, see [Running & Maintaining](Maintenance.md). The files it mentions are in the `dotproxy` folder.

## Notes

* `restart: unless-stopped` lets Docker restart DotProxy when it exits, which does the same job as the start scripts. This applies to the `shutdown` command too, so to stop DotProxy for good, use `docker compose stop` instead.
* If you change the proxy port with `serverConnection port`, update the `ports` mapping to match.
* Once the Discord bot is set up, you can manage DotProxy from Discord instead of attaching to the container.

### Without Docker Compose

```bash
docker run -d -it --name dotproxy --restart unless-stopped \
  -v "$PWD/dotproxy:/dotproxy" -w /dotproxy -p 25565:25565 \
  eclipse-temurin:21-jre \
  java -Xmx300m -XX:+UseG1GC --enable-native-access=ALL-UNNAMED -jar DotProxy.jar
```
