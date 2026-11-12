package com.dotproxy.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.dotproxy.command.api.*;
import com.dotproxy.command.brigadier.CaseInsensitiveLiteralCommandNode;
import com.dotproxy.command.brigadier.McplBrigadierConverter;
import com.dotproxy.command.impl.*;
import com.dotproxy.network.server.ServerSession;
import lombok.Getter;
import lombok.Locked;
import org.geysermc.mcprotocollib.protocol.data.game.command.CommandNode;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.dotproxy.Globals.*;
import static java.util.Arrays.asList;

@Getter
public class CommandManager {
    private final List<Command> commandsList = Lists.newArrayList(
        new ActionLimiterCommand(),
        new ActiveHoursCommand(),
        new AntiAFKCommand(),
        new AntiKickCommand(),
        new AuthCommand(),
        new AutoDisconnectCommand(),
        new AutoReconnectCommand(),
        new AutoRespawnCommand(),
        new ChatHistoryCommand(),
        new ChatRelayCommand(),
        new ChatSchemaCommand(),
        new ClientConnectionCommand(),
        new CommandConfigCommand(),
        new ConnectCommand(),
        new ConnectionTestCommand(),
        new DebugCommand(),
        new DisconnectCommand(),
        new DiscordManageCommand(),
        new DiscordNotificationsCommand(),
        new DisplayCoordsCommand(),
        new ExtraChatCommand(),
        new FriendCommand(),
        new HelpCommand(),
        new IgnoreCommand(),
        new InventoryCommand(),
        new KickCommand(),
        new LicenseCommand(),
        new MultiCommand(),
        new PacketLogCommand(),
        new PluginsCommand(),
        new RateLimiterCommand(),
        new ReconnectCommand(),
        new RespawnCommand(),
        new RotateCommand(),
        new SendMessageCommand(),
        new ServerCommand(),
        new ServerConnectionCommand(),
        new ServerSwitcherCommand(),
        new ShutdownCommand(),
        new SkinCommand(),
        new SpectatorCommand(),
        new SpectatorEntityCommand(),
        new SpectatorEntityToggleCommand(),
        new SpectatorPlayerCamCommand(),
        new SpectatorSwapCommand(),
        new StatusCommand(),
        new TablistCommand(),
        new TerminalCommand(),
        new ThemeCommand(),
        new TickRateCommand(),
        new TransferCommand(),
        new UnsupportedCommand(),
        new ViaVersionCommand(),
        new VisualRangeCommand(),
        new WhitelistCommand()
    );
    private final CommandDispatcher<CommandContext> dispatcher;
    private @NonNull CommandNode[] mcplCommandNodes = new CommandNode[0];
    private AtomicBoolean mcplCommandNodesStale = new AtomicBoolean(true);

    public CommandManager() {
        this.dispatcher = new CommandDispatcher<>();
        registerCommands();
        mcplCommandNodesStale.set(true);
    }

    public void registerCommands() {
       commandsList.forEach(this::registerCommand);
    }

    public void registerPluginCommand(Command command) {
        if (commandsList.contains(command)) {
            DEFAULT_LOG.warn("Duplicate plugin command being registered: {}", command.commandUsage().getName(), new RuntimeException());
            return;
        }
        registerCommand(command);
        commandsList.add(command);
        mcplCommandNodesStale.set(true);
    }

    public List<Command> getCommands() {
        return commandsList;
    }

    public List<Command> getCommands(final CommandCategory category) {
        return commandsList.stream()
            .filter(command -> category == CommandCategory.ALL || command.commandUsage().getCategory() == category)
            .toList();
    }

    void registerCommand(final Command command) {
        LiteralArgumentBuilder<CommandContext> cmdBuilder = command.register();
        if (dispatcher.getRoot().getChild(cmdBuilder.getLiteral()) != null) {
            DEFAULT_LOG.warn("Duplicate command being registered: {}", cmdBuilder.getLiteral(), new RuntimeException());
        }
        final LiteralCommandNode<CommandContext> node = dispatcher.register(cmdBuilder);
        command.commandUsage().getAliases().forEach(alias -> dispatcher.register(command.redirect(alias, node)));
    }

    @Locked
    public CommandNode[] getMcplCommandNodes() {
        if (mcplCommandNodesStale.compareAndSet(true, false)) {
            syncCommandNodes();
        }
        return mcplCommandNodes;
    }

    void syncCommandNodes() {
        this.mcplCommandNodes = McplBrigadierConverter.toMcpl(this.dispatcher);
    }

    public void execute(final CommandContext context, final ParseResults<CommandContext> parseResults) {
        try {
            execute0(context, parseResults);
        } catch (final CommandSyntaxException e) {
            // fall through
            // errors handled by delegate
            // and if this not a matching root command we want to fallback to original commands
        }
        saveConfigAsync();
    }

    public void execute(final CommandContext context) {
        final ParseResults<CommandContext> parse = parse(context);
        execute(context, parse);
    }

    public ParseResults<CommandContext> parse(final CommandContext context) {
        return this.dispatcher.parse(downcaseFirstWord(context.getInput()), context);
    }

    public boolean hasCommandNode(final ParseResults<CommandContext> parse) {
        return parse.getContext().getNodes().stream().anyMatch(node -> node.getNode() instanceof CaseInsensitiveLiteralCommandNode);
    }

    private String downcaseFirstWord(final String sentence) {
        List<String> words = asList(sentence.split(" "));
        if (words.size() > 1) {
            return words.getFirst().toLowerCase() + sentence.substring(words.getFirst().length());
        } else {
            return sentence.toLowerCase();
        }
    }

    private void execute0(final CommandContext context, final ParseResults<CommandContext> parse) throws CommandSyntaxException {
        if (CONFIG.plugins.enabled && CONFIG.plugins.blockCommandsUntilLoaded && !PLUGIN_MANAGER.isInitialized()) {
            DEFAULT_LOG.warn("Blocked command execution until plugins are loaded: `{}`", context.getInput());
            return;
        }
        var commandNodeOptional = parse.getContext()
            .getNodes()
            .stream()
            .findFirst()
            .map(ParsedCommandNode::getNode)
            .filter(node -> node instanceof CaseInsensitiveLiteralCommandNode)
            .map(node -> ((CaseInsensitiveLiteralCommandNode<CommandContext>) node));
        if (commandNodeOptional.isEmpty()) return;
        var commandNode = commandNodeOptional.get();
        var errorHandler = commandNode.getErrorHandler();
        var successHandler = commandNode.getSuccessHandler();
        var executionErrorHandler = commandNode.getExecutionErrorHandler();
        var executionExceptionHandler = commandNode.getExecutionExceptionHandler();

        if (!parse.getExceptions().isEmpty() || parse.getReader().canRead()) {
            errorHandler.handle(parse.getExceptions(), context);
            return;
        }
        dispatcher.setConsumer((commandContext, success, result) -> {
            if (success) {
                if (result == Command.OK)
                    successHandler.handle(context);
                else
                    executionErrorHandler.handle(context);
            }
            else errorHandler.handle(parse.getExceptions(), context);
        });
        try {
            dispatcher.execute(parse);
        } catch (Exception e) {
            executionExceptionHandler.handle(context, e);
        }
    }

    public CompletableFuture<Suggestions> suggestions(final String input, CommandSource commandSource) {
        var stringReader = new StringReader(downcaseFirstWord(input));
        if (stringReader.canRead() && stringReader.peek() == '/') {
            stringReader.skip();
        }
        final ParseResults<CommandContext> parse = this.dispatcher.parse(stringReader, CommandContext.create(input, commandSource));
        return this.dispatcher.getCompletionSuggestions(parse);
    }

    public CompletableFuture<Suggestions> suggestions(final String input, PlayerCommandSource commandSource, ServerSession session) {
        var stringReader = new StringReader(downcaseFirstWord(input));
        if (stringReader.canRead() && stringReader.peek() == '/') {
            stringReader.skip();
        }
        var ctx = CommandContext.create(input, commandSource);
        ctx.setInGamePlayerInfo(new CommandContext.InGamePlayerInfo(session));
        final ParseResults<CommandContext> parse = this.dispatcher.parse(stringReader, ctx);
        return this.dispatcher.getCompletionSuggestions(parse);
    }
}
