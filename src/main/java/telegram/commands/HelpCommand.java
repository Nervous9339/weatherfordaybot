package telegram.commands;

import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.ICommandRegistry;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;


/**
 * @author Nikita Zinoviev
 * @version 1.0
 * @brief This command starts the conversation with the bot
 */
public class HelpCommand extends BotCommand {

    private static final String LOGTAG = "HELPCOMMAND";

    private final ICommandRegistry commandRegistry;

    public HelpCommand(ICommandRegistry commandRegistry){
        super("help", "Get all commands this bot provides");
        this.commandRegistry = commandRegistry;
    }

    //TODO finish this method after connecting the DB
    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {

    }
}
