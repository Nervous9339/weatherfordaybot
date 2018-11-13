package telegram;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.LongPollingBot;
import org.telegram.telegrambots.meta.logging.BotLogger;
import org.telegram.telegrambots.meta.logging.BotsFileHandler;
import telegram.bot.WeatherForDayBot;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

/**
 * @author Nikita Zinoviev
 * @version 1.0
 * @brief Main class to create a bot
 */
public class Main {
    private static final String LOGTAG = "MAIN";

    public static void main(String[] args) {
        BotLogger.setLevel(Level.ALL);
        BotLogger.registerLogger(new ConsoleHandler());
        try {
            BotLogger.registerLogger(new BotsFileHandler());
        }
        catch (IOException ex){
            BotLogger.severe(LOGTAG, ex);
        }

        try {
            ApiContextInitializer.init();
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
            try {
                //register LongPolling bot.
                telegramBotsApi.registerBot(new WeatherForDayBot());
            }
            catch (TelegramApiException ex){
                BotLogger.error(LOGTAG, ex);
            }
        }
        catch (Exception ex){
            BotLogger.error(LOGTAG, ex);
        }
    }
}
