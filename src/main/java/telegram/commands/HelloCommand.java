package telegram.commands;

import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

/**
 * @author Nikita Zinoviev
 * @version 1.0
 * @brief This command simply replies with a hello to the user command
 */
public class HelloCommand extends BotCommand {

    private static final String LOGTAG = "HELLOCOMMAND";

    public HelloCommand(){
        super("hello", "Say hello to this bot");
    }

    //TODO finish this method after connecting the DB
    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {

    }
}
