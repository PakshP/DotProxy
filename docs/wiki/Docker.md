# Docker

You don't need a custom image to run DotProxy in Docker. The official `eclipse-temurin:21-jre` image works with a mounted folder that holds `DotProxy.jar`. Your `config.json`, login cache, logs, and plugins are saved in that folder, so nothing is lost when the container is recreated.

## Setup

1. Make a folder for DotProxy (for example `~/dotproxy`) and put `DotProxy.jar` in it.
2. Create a `compose.yaml` file next to it:

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

## Notes

* `restart: unless-stopped` lets Docker restart DotProxy when it exits, which does the same job as the start scripts. To stop DotProxy for good, use `docker compose stop` instead of the `shutdown` command.
* If you change the proxy port with `serverConnection port`, update the `ports` mapping to match.
* Once the Discord bot is set up, you can manage DotProxy from Discord instead of attaching to the container.

### Without Docker Compose

```bash
docker run -d -it --name dotproxy --restart unless-stopped \
  -v "$PWD/dotproxy:/dotproxy" -w /dotproxy -p 25565:25565 \
  eclipse-temurin:21-jre \
  java -Xmx300m -XX:+UseG1GC --enable-native-access=ALL-UNNAMED -jar DotProxy.jar
```
