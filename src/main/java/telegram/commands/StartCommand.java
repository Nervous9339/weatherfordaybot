package telegram.commands;

import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

/**
 * @author Nikita Zinoviev
 * @version 1.0
 * @brief This command starts the conversation with the bot
 */
public class StartCommand extends BotCommand {
    private static final String LOGTAG = "STARTCOMMAND";

    public StartCommand(){
        super("start", "With this command you can start the Bot");
    }

    //TODO finish this method after connecting the DB
    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {

    }
}
