# DotProxy Commands Documentation

## Command Prefixes

#### Discord

`.` (e.g. `.help`)

#### In-game

`/` OR `!` (e.g. `/help`)

#### Terminal

N/A (e.g. `help`)

## Core Commands

### connect

Connects DotProxy to the destination MC server

**Aliases:** `c`

**Usage**

  ```connect```


### disconnect

Disconnects DotProxy from the destination MC server

**Aliases:** `dc`

**Usage**

  ```disconnect```


### help

DotProxy command list

**Aliases:** `h`

**Usage**

  ```help```

  ```help <category>```

  ```help <command>```


### spectator

Configures the Spectator feature.



The spectator whitelist only allows players to join as spectators.

Players who are regular whitelisted (i.e. with the `whitelist` command) can always join as spectators regardless.



Spectator entities control what entity is used to represent spectators in-game.



Full commands allow spectators access to all standard DotProxy commands like `connect`, `disconnect`, etc.

If this is disabled, spectators only have access to a limited set of core commands.

**Usage**

  ```spectator on/off```

  ```spectator whitelist add/del <player>```

  ```spectator whitelist addAll <player 1>,<player 2>...```

  ```spectator whitelist list```

  ```spectator whitelist clear```

  ```spectator entity list```

  ```spectator entity <entity>```

  ```spectator publicChat on/off```

  ```spectator chatSendsPrivateMessage on/off```

  ```spectator playerCamOnJoin on/off```

  ```spectator fullCommands on/off```

  ```spectator fullCommands slashCommands on/off```

  ```spectator fullCommands requireRegularWhitelist on/off```

  ```spectator fullCommands serverCommands on/off```

  ```spectator fullCommands prioritizeServerCommands on/off```


### status

Prints the current status of DotProxy, the in-game player, and modules.

**Aliases:** `s`

**Usage**

  ```status```

  ```status modules```


### whitelist

Manages the list of players allowed to login.



Whitelisted players are allowed to both control the account in-game and spectate.



`autoAddDotProxyAccount` will add the MC account you have logged in DotProxy with to the whitelist.



Blacklist is only used and shown if the whitelist or spectator whitelist is disabled (see the `unsupported` command`)

**Aliases:** `wl`

**Usage**

  ```whitelist add/del <player>```

  ```whitelist addAll <player 1>,<player 2>...```

  ```whitelist list```

  ```whitelist clear```

  ```whitelist autoAddDotProxyAccount on/off```

  ```whitelist blacklist add/del <player>```

  ```whitelist blacklist clear```



## Manage Commands

### auth

Configures the proxy's authentication settings.



To switch accounts, use the `clear` command.



`attempts` configures the number of login attempts before wiping the cache.



`alwaysRefreshOnLogin` will always refresh the token on login instead of trusting the cache. This can cause

Microsoft to rate limit your account. Auth tokens will always refresh in the background even if this is off.



`deviceCode` is the default and recommended authentication type.

If authentication fails, try logging into the account on the vanilla MC launcher and joining a server. Then try again in DotProxy.

If this still fails, try one of the alternate auth types.

**Usage**

  ```auth clear```

  ```auth refreshNow```

  ```auth attempts <int>```

  ```auth alwaysRefreshOnLogin on/off```

  ```auth type <deviceCode/emailAndPassword/prism>```

  ```auth email <email>```

  ```auth password <password>```

  ```auth mention on/off```

  ```auth openBrowser on/off```

  ```auth maxRefreshIntervalMins <minutes>```

  ```auth useClientConnectionProxy on/off```

  ```auth chatSigning on/off```

  ```auth chatSigning force on/off```

  ```auth chatSigning commands on/off```


### chatRelay

Configures the Discord ChatRelay feature.



The ChatRelay is a live feed of chat messages and/or connection messages from the server to a Discord channel.



Mentions can be configured when a whisper is received or your name is seen in chat.



Messages typed in the ChatRelay discord channel will be sent as chat messages in-game

Discord message replies will be sent as whispers in-game.



Ignore regex will filter out messages, see here for help writing regex: https://regex101.com/ (Java flavor)

**Usage**

  ```chatRelay on/off```

  ```chatRelay channel <channelId>```

  ```chatRelay connectionMessages on/off```

  ```chatRelay whispers on/off```

  ```chatRelay publicChat on/off```

  ```chatRelay serverMessages on/off```

  ```chatRelay whisperMentions on/off```

  ```chatRelay nameMentions on/off```

  ```chatRelay mentionsWhileConnected on/off```

  ```chatRelay sendMessages on/off```

  ```chatRelay ignoreRegex add <regex>```

  ```chatRelay ignoreRegex del <index>```

  ```chatRelay ignoreRegex list```

  ```chatRelay ignoreRegex clear```


### chatSchema

Configure how DotProxy parses public chats and whispers.



Correct schemas are needed for chat relay and chat based features to work correctly.



Schemas have the following special tokens:

* $s -> Chat/whisper sender, player name

* $r -> Whisper receiver, player name

* $m -> Message, text content of the chat/whisper

* $w -> Wildcard, any varying text, e.g. a role prefix `[ADMIN] Steve: test message`



Example chat schema (the default):

* public chat: `<$s> $m`

* whisper inbound: `$s whispers: $m`

* whisper outbound: `to $r: $m`



You can configure different schemas for different servers based on the server address.



Server address is without port, e.g. `mc.example.com` or `192.168.0.5`

**Usage**

  ```chatSchema set <publicChat/whisperInbound/whisperOutbound> <serverAddress> <schema>```

  ```chatSchema preset <serverAddress> <essentials>```

  ```chatSchema del <serverAddress>```

  ```chatSchema list```


### clientConnection

Manages the connection configuration from DotProxy to the destination MC server.

**Usage**

  ```clientConnection autoConnect on/off```

  ```clientConnection proxy on/off```

  ```clientConnection proxy type <type>```

  ```clientConnection proxy host <host>```

  ```clientConnection proxy port <port>```

  ```clientConnection proxy user <user>```

  ```clientConnection proxy password <password>```

  ```clientConnection proxy auth clear```

  ```clientConnection bindAddress <address>```

  ```clientConnection timeout on/off```

  ```clientConnection timeout <seconds>```

  ```clientConnection ping packetInterval <seconds>```

  ```clientConnection keepAlive mode <passthrough/independent>```

  ```clientConnection keepAlive queueTimeout <ms>```


### commandConfig

Configures DotProxy command prefixes and settings.

**Usage**

  ```commandConfig discord prefix <string>```

  ```commandConfig ingame on/off```

  ```commandConfig ingame slashCommands on/off```

  ```commandConfig ingame slashCommands replaceServerCommands on/off```

  ```commandConfig ingame slashCommands prioritizeServerCommands on/off```

  ```commandConfig ingame slashCommands suggestions on/off```

  ```commandConfig ingame prefix <string>```

  ```commandConfig ingame allowWhitelistedToUseAccountOwnerCommands on/off```


### debug

Debug settings for features in testing or for use in development.

**Usage**

  ```debug sync inventory```

  ```debug sync chunks```

  ```debug clearEffects```

  ```debug kickDisconnect on/off```

  ```debug dc```

  ```debug debugLogs on/off```

  ```debug chunkCacheFullbright on/off```

  ```debug defaultClientRenderDistance <int>```

  ```debug lockFile on/off```

  ```debug uploadLog```

  ```debug uploadDebugLog```

  ```debug passthroughResourcePacks on/off```


### discord

Manages the Discord bot's configuration.



The relay is configured using the `chatRelay` command

**Usage**

  ```discord on/off```

  ```discord channel <channel ID>```

  ```discord token <token>```

  ```discord role <role ID>```

  ```discord relayChannel <channelId>```

  ```discord manageProfileImage on/off```

  ```discord manageNickname on/off```

  ```discord manageDescription on/off```

  ```discord managePresence on/off```

  ```discord showNonWhitelistIP on/off```

  ```discord ignoreOtherBots on/off```

  ```discord ignoreWebhooks on/off```


### displayCoords

Configures whether the discord bot's messages should display coordinates. Only usable by account owner(s).

**Aliases:** `coords`

**Usage**

  ```displayCoords on/off```


### friend

Manage the friend list.

Friends change behavior for various modules like VisualRange and AutoDisconnect

**Aliases:** `f`

**Usage**

  ```friend add/del <player>```

  ```friend addAll <player 1>,<player 2>...```

  ```friend list```

  ```friend clear```


### kick

Kicks all players or a specific player. Only usable by account owners.

**Usage**

  ```kick```

  ```kick <player>```


### plugins

[BETA]



Configures the DotProxy plugin manager.



Plugins are user-created add-ons that add modules and commands.



Plugins are only supported on the `java` release channel.

**Aliases:** `plugin`

**Usage**

  ```plugins on/off```

  ```plugins list```

  ```plugins download <url>```

  ```plugins remove <pluginId>```


### reconnect

Disconnect and reconnects from the destination MC server.

**Usage**

  ```reconnect```


### restart

Restarts DotProxy.



When started with the start script (start.sh / start.bat) or as a systemd service, DotProxy comes

back up automatically and loads any new DotProxy.jar. Use this after replacing the jar to update.



Without a start script or service, this behaves like `shutdown`.

**Usage**

  ```restart```


### server

Change the MC server DotProxy connects to.

**Usage**

  ```server <IP>```

  ```server <IP> <port>```


### serverConnection

Configures the MC server hosted by DotProxy and players' connections to it



The `proxyIP` is the IP players should connect to. This is purely informational.



The `port` argument changes the port the DotProxy MC server listens on



`upnp` will try to open the port to the public internet, useful for self-hosting on a home network



The `ping` arguments configure the server list ping response DotProxy sends to players.

`onlinePlayers` = MC profiles of players

`onlinePlayerCount` = number of players connected

`maxPlayers` = number of players that can connect

`lanBroadcast` = LAN server broadcast

`log` = logs pings



The `timeout` arguments configures how long until players are kicked due no packets being received.

**Usage**

  ```serverConnection proxyIP <ip>```

  ```serverConnection port <port>```

  ```serverConnection upnp on/off```

  ```serverConnection ping on/off```

  ```serverConnection ping onlinePlayers on/off```

  ```serverConnection ping onlinePlayerCount on/off```

  ```serverConnection ping maxPlayers <int>```

  ```serverConnection ping lanBroadcast on/off```

  ```serverConnection ping log on/off```

  ```serverConnection enforceMatchingConnectingAddress on/off```

  ```serverConnection timeout on/off```

  ```serverConnection timeout <seconds>```

  ```serverConnection autoConnectOnLogin on/off```

  ```serverConnection updateServerIcon on/off```

  ```serverConnection chatSigning mode <disguised/passthrough/system>```

  ```serverConnection preferLoginAsController on/off```


### shutdown

Shuts down DotProxy, without letting the start script restart it.

**Aliases:** `exit`

**Usage**

  ```shutdown```


### spectatorEntity

Changes the current spectator entity. Only usable by spectators

**Aliases:** `e`

**Usage**

  ```spectatorEntity```

  ```spectatorEntity <entity>```


### entityToggle

Toggles the visibility of spectator entities. Only usable by spectators.

**Aliases:** `etoggle`

**Usage**

  ```entityToggle```


### playerCam

Toggles spectators between player and entity cameras. Only usable by spectators

**Usage**

  ```playerCam```


### terminal

Configures the DotProxy interactive terminal.



All subcommands only usable from the terminal.

**Usage**

  ```terminal autoCompletions on/off```

  ```terminal logToDiscord on/off```

  ```terminal logChatMessages on/off```


### theme

Changes the color theme of alerts and messages.



Use `theme list` to see available colors.



Where Colors Are Used:

  * Primary: Most embeds and command responses if not an error.

  * Success: General "this worked" responses, server join, and friends

  * Error: Error responses, server leave, and enemies

**Aliases:** `color`

**Usage**

  ```theme list```

  ```theme primary <color>```

  ```theme success <color>```

  ```theme error <color>```


### transfer

Transfers connected players to a destination MC server.



If no player is specified, all currently connected players will be transferred.

If no port is specified, it will be looked up via DNS, or default to 25565.

**Usage**

  ```transfer <address>```

  ```transfer <address> <playerName>```

  ```transfer <address> <port>```

  ```transfer <address> <port> <playerName>```


### unsupported

Unsupported settings that cause critical security issues.



Do not use edit these unless you absolutely understand what you are doing.



No user support will be provided if you modify any of these settings.



All subcommands are only usable from the terminal.

**Usage**

  ```unsupported whitelist on/off```

  ```unsupported spectatorWhitelist on/off```

  ```unsupported allowOfflinePlayers on/off```

  ```unsupported auth type offline```

  ```unsupported auth offlineUsername <username>```

  ```unsupported auth offlineUUID <uuid>```

  ```unsupported auth offlineUUID clear```



## Info Commands

### connectionTest

Tests whether this proxy or another MC server is accessible from the public internet.



If the test succeeds, that means other people can connect.



If the test fails, either the `proxyIP` setting is not set to a public IP address or your instance is not

exposed on the public internet.



To configure the `proxyIP` use the `help serverConnection` command



On a VPS this is usually due to a firewall needing to be disabled.



On a home PC you would need both disable any firewall and configure port forwarding in your router.

**Usage**

  ```connectionTest```

  ```connectionTest <address>```

  ```connectionTest testOnStart on/off```


### discordNotifications

Configures various discord notifications regarding player and proxy connections, deaths, and more.

**Aliases:** `alerts` / `notifications`

**Usage**

  ```discordNotifications role set <roleId>```

  ```discordNotifications role reset```

  ```discordNotifications connect mention on/off```

  ```discordNotifications online mention on/off```

  ```discordNotifications disconnect mention on/off```

  ```discordNotifications death mention on/off```

  ```discordNotifications loginFailed mention on/off```

  ```discordNotifications clientConnect mention on/off```

  ```discordNotifications clientDisconnect mention on/off```

  ```discordNotifications spectatorConnect mention on/off```

  ```discordNotifications spectatorDisconnect mention on/off```

  ```discordNotifications nonWhitelistedConnect mention on/off```

  ```discordNotifications mcVersionMismatchWarning on/off```


### inventory

Show and interact with the player's inventory or containers.

**Aliases:** `inv`

**Usage**

  ```inventory```

  ```inventory show```

  ```inventory hold <slot>```

  ```inventory swap <from> <to>```

  ```inventory drop <slot>```

  ```inventory drop stack <slot>```

  ```inventory close```

  ```inventory withdraw```

  ```inventory deposit```

  ```inventory click <left/right> <slot>```

  ```inventory button <buttonId>```

  ```inventory actionDelayTicks <ticks>```

  ```inventory ncpStrict on/off```

  ```inventory autoCloseOpenContainers on/off```

  ```inventory autoCloseOpenContainers delaySeconds <seconds>```


### license

Displays the software license and information about your legal rights

**Usage**

  ```license```


### packetLog

Logs packets that are sent/received. Requires account owner permissions.



`client` -> packets between DotProxy and destination MC server

`server` -> packets between players and DotProxy



If a filter is set, only packets matching the filter will be logged.

Filters are always lowercase, but matched case-insensitive.



`logLevelDebug` -> toggles the logger level between INFO and DEBUG



To enable the debug log: `debug debugLogs on`

And to enable debug log in the terminal: `debug terminalDebugLogs on`

**Usage**

  ```packetLog on/off```

  ```packetLog client on/off```

  ```packetLog server on/off```

  ```packetLog filter add/del <string>```

  ```packetLog filter list```

  ```packetLog filter clear```

  ```packetLog logLevelDebug on/off```


### tablist

Prints the current MC server's player list

**Aliases:** `tab`

**Usage**

  ```tablist```

  ```tablist header```



## Module Commands

### actionLimiter

Limits player actions and movements.



Players who login with the same account as the one used by DotProxy will be immune to these restrictions.



If the movement limits are reached by a player, they will be disconnected while the proxy account will stay logged in.



Other limits do not disconnect players and instead cancel the actions.

**Aliases:** `al`

**Usage**

  ```actionLimiter on/off```

  ```actionLimiter allowMovement on/off```

  ```actionLimiter movementDistance <distance>```

  ```actionLimiter movementHome <x> <z>```

  ```actionLimiter movementMinY <y>```

  ```actionLimiter allowInventory on/off```

  ```actionLimiter allowBlockBreaking on/off```

  ```actionLimiter allowInteract on/off```

  ```actionLimiter allowEnderChest on/off```

  ```actionLimiter allowUseItem on/off```

  ```actionLimiter allowBookSigning on/off```

  ```actionLimiter allowChat on/off```

  ```actionLimiter allowServerCommands on/off```

  ```actionLimiter allowRespawn on/off```

  ```actionLimiter itemsBlacklist on/off```

  ```actionLimiter itemsBlacklist add/del <item>```

  ```actionLimiter itemsBlacklist addAll <item 1>,<item 2>...```

  ```actionLimiter itemsBlacklist clear```

  ```actionLimiter itemsBlacklist list```


### activeHours

Set times for DotProxy to automatically connect at.



Connects will occur at the set times.



 Time zone Ids ("TZ Identifier" column): https://w.wiki/A2fd

 Time format: hh:mm, Examples: 1:42, 14:42, 14:01

**Usage**

  ```activeHours on/off```

  ```activeHours timezone <timezone ID>```

  ```activeHours add/del <time>```

  ```activeHours status```

  ```activeHours whilePlayerConnected on/off```

  ```activeHours fullSessionUntilNextDisconnect on/off```


### antiAFK

Configures the AntiAFK module.



On most servers, swinging OR walking is enough to avoid being kicked for inactivity.



The walk action will move the player roughly in a square shape. To avoid falling down any ledges, enable safeWalk



For delay settings, 1 tick = 50ms

**Aliases:** `afk`

**Usage**

  ```antiAFK on/off```

  ```antiAFK rotate on/off```

  ```antiAFK rotate delay <ticks>```

  ```antiAFK swing on/off```

  ```antiAFK swing delay <ticks>```

  ```antiAFK walk on/off```

  ```antiAFK walk delay <ticks>```

  ```antiAFK safeWalk on/off```

  ```antiAFK walkDistance <ticks>```

  ```antiAFK jump on/off```

  ```antiAFK jump onlyInLiquid on/off```

  ```antiAFK jump delay <ticks>```

  ```antiAFK sneak on/off```

  ```antiAFK sneak delay <ticks>```


### antiKick

AntiKick kicks players controlling the proxy client if they are inactive for a set amount of time.



Inactivity is defined as not moving or swinging.

**Usage**

  ```antiKick on/off```

  ```antiKick playerInactivityKickMins <minutes>```

  ```antiKick minWalkDistance <blocks>```


### autoDisconnect

Configures the AutoDisconnect module.



Every mode and setting requires the module to be enabled to be active.



Modes:



  * Health: Disconnects when health is below a set threshold level

  * Thunder: Disconnects during thunderstorms (i.e. avoid lightning burning down bases)

  * Unknown Player: Disconnects when a player not on the friends list, whitelist, or spectator whitelist is in visual range

  * TotemPop: Disconnects when your totem is popped

Multiple modes can be enabled, they are non-exclusive



Settings non-exclusive to modes:

  * WhilePlayerConnected: If AutoDisconnect should disconnect while a player is controlling the proxy account

  * AutoClientDisconnect: Disconnects when the controlling player disconnects

  * CancelAutoReconnect: Cancels AutoReconnect when AutoDisconnect is triggered

**Aliases:** `autoLog`

**Usage**

  ```autoDisconnect on/off```

  ```autoDisconnect health on/off```

  ```autoDisconnect health <integer>```

  ```autoDisconnect thunder on/off```

  ```autoDisconnect unknownPlayer on/off```

  ```autoDisconnect totemPop on/off```

  ```autoDisconnect totemPop minTotemsRemaining <count>```

  ```autoDisconnect whilePlayerConnected on/off```

  ```autoDisconnect autoClientDisconnect on/off```

  ```autoDisconnect cancelAutoReconnect on/off```


### autoReconnect

Automatically reconnects the bot when it is disconnected.

**Usage**

  ```autoReconnect on/off```

  ```autoReconnect delay <seconds>```

  ```autoReconnect maxAttempts <number>```


### autoRespawn

Automatically respawns the player after dying.

**Usage**

  ```autoRespawn on/off```

  ```autoRespawn delay <milliseconds>```


### chatHistory

Caches and sends recent chat history to players and spectators who connect to the proxy.

Includes whispers, chat, and system messages.

**Usage**

  ```chatHistory on/off```

  ```chatHistory seconds <seconds>```

  ```chatHistory maxCount <maxCount>```

  ```chatHistory spectators on/off```


### extraChat

Extra chat features and modifications.

**Usage**

  ```extraChat on/off```

  ```extraChat hideChat on/off```

  ```extraChat hideWhispers on/off```

  ```extraChat showConnectionMessages on/off```

  ```extraChat insertClickableLinks on/off```

  ```extraChat whisperCommand <command>```

  ```extraChat prefix on/off```

  ```extraChat prefix set <prefix>```

  ```extraChat suffix on/off```

  ```extraChat suffix set <suffix>```

  ```extraChat suffix random on/off```


### ignore

Hides chat and death messages and notifications from a configured list of players.

**Usage**

  ```ignore add/del <player>```

  ```ignore addAll <player 1>,<player 2>...```

  ```ignore list```

  ```ignore clear```


### multi

Execute multiple commands one after another



Each command is separated by a double comma: `,,`



The contained commands do not have a prefix.



Example: `multi friend add Steve,,say hello,,whitelist add Alex`

**Aliases:** `x`

**Usage**

  ```multi <command1>,,<command2>...```


### rateLimiter

Limits how often players are allowed to attempt logins and send packets.



The rate limit is counted as the number of seconds between logins allowed.

The login rate limit is per IP address.



The packet rate limiter is counted per connection.

Packets received are counted in a configurable interval of seconds.

If more packets than the rateLimit are received in that interval, the player is disconnected.

**Usage**

  ```rateLimiter login on/off```

  ```rateLimiter login rateLimit <seconds>```

  ```rateLimiter packet on/off```

  ```rateLimiter packet interval <seconds>```

  ```rateLimiter packet rateLimit <int>```


### respawn

Performs a respawn

**Usage**

  ```respawn```


### rotate

Rotates the bot in-game.



Note that many other modules can change the player's rotation after this command is executed.

**Usage**

  ```rotate```

  ```rotate <yaw> <pitch>```

  ```rotate yaw <yaw>```

  ```rotate pitch <pitch>```


### sendMessage

Sends a message in-game.

**Aliases:** `say` / `m`

**Usage**

  ```sendMessage <message>```


### switch

Switch the connected player to an alternate MC server.



Can be used to switch between multiple DotProxy instances quickly.



Servers being switched to must have transfers enabled and be on an MC version >=1.20.6

**Usage**

  ```switch```

  ```switch register <name> <address> <port>```

  ```switch del <name>```

  ```switch clear```

  ```switch list```

  ```switch <name>```


### skin

Temporarily change your skin to another player's skin.



This is only client-side and only affects how you see yourself.

**Usage**

  ```skin <playerName>```


### swap

Swaps the current controlling player to spectator mode.

**Usage**

  ```swap```

  ```swap force```


### tickRate

Modifies the client tick rate, as a multiple of the default rate (20 ticks per second)



Example:



* 1.0 would be 20 tps

* 0.5 would slow down the tickrate to 10 tps

* 2.0 would speed up the tickrate to 40 tps

**Usage**

  ```tickRate <rate>```


### via

Configure the integrated ViaVersion module.



`proxyToServer` -> DotProxy connecting to the MC server

`playerToProxy` -> players connecting to DotProxy

**Usage**

  ```via proxyToServer on/off```

  ```via proxyToServer version auto```

  ```via proxyToServer version <MC version>```

  ```via playerToProxy on/off```


### visualRange

Configure the VisualRange notification feature.



Alerts are sent both in the terminal and in discord, with optional discord mentions.



To add players to the friends list see the `friends` command.

**Aliases:** `vr`

**Usage**

  ```visualRange on/off```

  ```visualRange list```

  ```visualRange enter on/off```

  ```visualRange enter mention on/off```

  ```visualRange enter whisper on/off```

  ```visualRange enter whisper message <message>```

  ```visualRange enter whisper cooldown <seconds>```

  ```visualRange enter whisper command <command>```

  ```visualRange enter whisper whilePlayerConnected on/off```

  ```visualRange leave on/off```

  ```visualRange logout on/off```

  ```visualRange ignoreFriends on/off```



