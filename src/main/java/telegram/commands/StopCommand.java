package telegram.commands;

import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

/**
 * @author Nikita Zinoviev
 * @version 1.0
 * @brief This command stops the conversation with the bot.
 *        Bot won't response to user until he sends a start command
 */
public class StopCommand extends BotCommand {

    private static final String LOGTAG = "STOPCOMMAND";

    public StopCommand(){
        super("stop", "With this command you can stop the bot");
    }

    //TODO finish this method after connecting the DB
    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {

    }
}
