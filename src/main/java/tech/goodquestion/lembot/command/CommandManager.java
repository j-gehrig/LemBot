package tech.goodquestion.lembot.command;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import tech.goodquestion.lembot.config.Config;
import tech.goodquestion.lembot.library.Helper;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class CommandManager extends ListenerAdapter {

    public static final Pattern COMMAND_PATTERN = Pattern.compile("[" + Config.getInstance().getBotConfig().getPrefix() + "]([^ ]+)(.*)");
    private static CommandManager INSTANCE = null;

    public static CommandManager getInstance() {
        return INSTANCE;
    }

    private final Map<String, IBotCommand> commands = new HashMap<>();
    private final JDA jda;
    private final Set<String> helpLists = new HashSet<>();

    public void registerCommand(final IBotCommand command) {
        commands.put(command.getName(), command);

        if (command.getHelpList() != null) {
            helpLists.add(command.getHelpList());
        }
    }

    public IBotCommand getCommand(final String name) {
        return commands.getOrDefault(name, null);
    }

    public CommandManager(final JDA jda) {
        this.jda = jda;
        INSTANCE = this;
        jda.addEventListener(this);
    }

    @Override
    public void onMessageReceived(@SuppressWarnings("null") @NotNull final MessageReceivedEvent event) {

        final Message message = event.getMessage();

        if (message.getAuthor().isBot()) {
            return;
        }

        Matcher commandMatcher = COMMAND_PATTERN.matcher(message.getContentRaw());

        if (!commandMatcher.matches()) return;

        String command = commandMatcher.group(1).toLowerCase();

        if (!commands.containsKey(command)) return;

        String[] args = commandMatcher.group(2).split(" ");

        args = Arrays.copyOfRange(args, 1, args.length);

        IBotCommand executor = commands.get(command);

        if (!executor.isPermitted(Objects.requireNonNull(message.getMember()))) {
            Helper.sendError(message, ":x: Permission Denied!");
            return;
        }

        try {
            executor.dispatch(message, (TextChannel) event.getChannel(), event.getMember(), args);
        } catch (IOException | SQLException ioException) {
            System.out.println(ioException.getMessage());
        }
    }

    public Map<String, IBotCommand> getCommands() {
        return commands;
    }

    public JDA getJDA() {
        return jda;
    }

    public Set<String> getHelpLists() {
        return helpLists;
    }
}