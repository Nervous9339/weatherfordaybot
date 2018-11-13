package telegram.bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.logging.BotLogger;
import telegram.BotConfig;
import telegram.services.WeatherService;

public class WeatherForDayBot extends TelegramLongPollingBot {
    private static final String LOGTAG = "WEATHERHANDLER";

    public WeatherForDayBot() {
        super();
    }

    @Override
    public String getBotToken() {
        return BotConfig.WEATHERBOT_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update){
        try {
            if (update.hasMessage()) {
                Message message = update.getMessage();
                if (message.hasText() || message.hasLocation()){
                    handleIncomingMessage(message);
                    //onCurrentWeatherCityRecieved(message.getChatId(), message);
                }
            }
        }
        catch (Exception ex){
            BotLogger.error(LOGTAG, ex);
        }
    }

    @Override
    public String getBotUsername() {
        return BotConfig.WEATHERBOT_NAME;
    }

    private void handleIncomingMessage(Message message) throws TelegramApiException{

    }

    /*private void handleIncomingMessage(Message message) throws TelegramApiException {
        SendMessage sendMessageRequest = messageOnCurrentWeather(message);
    }

    private static SendMessage messageOnCurrentWeather(Message message){
        SendMessage sendMessageRequest = onCurrentWeather(message);
        return sendMessageRequest;
    }

    private static SendMessage onCurrentWeather(Message message){
        SendMessage sendMessageRequest = onCurrentWeatherCityRecieved(message.getText());
        return sendMessageRequest;
    }*/

    /*private void onCurrentWeatherCityRecieved(Long chatId, Message message) throws TelegramApiException{
        String weather = WeatherService.getInstance().receiveWeatherCurrent(message.getText());
        SendMessage sendMessageRequest = new SendMessage();
        sendMessageRequest.setText(weather);
        sendMessageRequest.setChatId(chatId);
        execute(sendMessageRequest);
    }*/
}
