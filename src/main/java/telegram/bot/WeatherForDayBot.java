package telegram.bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.logging.BotLogger;
import telegram.BotConfig;
import telegram.Commands;
import telegram.database.DatabaseManager;
import telegram.services.CustomTimerTask;
import telegram.services.TimerExecutor;
import telegram.services.WeatherAlert;
import telegram.services.WeatherService;

import java.util.ArrayList;
import java.util.List;

public class WeatherForDayBot extends TelegramLongPollingBot {
    // Buttons
    private static final String FORECAST = "Forecast";
    private static final String CURRENT = "Current";
    private static final String CANCEL = "Cancel";
    private static final String NEW = "New";
    private static final String BACK = "Back";
    private static final String DELETE = "Delete";
    private static final String SUBSCRIBES = "Subscribe";
    private static final String LISTOFSUBSCRIBES = "List of your subscribes";
    private static final String LOCATION = "Location";
    // End Buttons

    //Help Message
    private static final String HELP_WEATHER_MESSAGE =
            "Hey, want some weather?\n" +
                    "Just send me one of this commands:\n" +
                    "Click on _Current_ and get current weather\n" +
                    "Click on _Forecast_ and get 9-hours forecast\n" +
                    "_Subscribe_ to receive weather every day";
    //End Help Message

    //Subscribe Menu
    private static final String YOU_NEED_THIS_FIRST = "You don't have cities to get daily weather forecast. " +
                                    "Please, go back and search for the _Current_ weather and then come back!";
    private static final String CHOOSE_NEW_SUBSCRIBE_CUTY = "For which city do you want to receive the weather?";
    private static final String CHOOSE_DELETE_SUBSCRIBE_CITY = "For which city do you want to delete subscribe?";
    private static final String NO_SUBSCRIBE = "You don't have a subscription to receive a weather";
    private static final String SUBSCRIBE_DELETE = "The selected subscribe has been deleted";
    private static final String INITIAL_SUBSCRIBE_STRING = "You have _%d_ subscribes:\n\n%s";
    private static final String PARTIAL_SUBSCRIBE = "_%S_\n";
    private static final String NEW_SUBSCRIBE_SAVED = "Your subscribe for _%s_ has been created! " +
                                                        "You will receive the weather update twice a day";
    //End Subscribe Menu

    //The rest of the menu
    private static final String ONCANCEL_COMMAND = "Back to main menu";
    private static final String ONWEATHER_NEW_COMMAND = "Please, send me the name of the city";
    private static final String ONWEATHER_LOCATION_COMMAND = "Please, send me a location";
    private static final String ONCURRENT_COMMAND_FROM_HISTORY = "Select the city from your recent requests, " +
                                                    "_New_ to send a new city or _Location_ to send me a location";
    private static final String ONCURRENT_COMMAND_WITHOUT_HISTORY = "Select _New_ to send me the name of the city or _Location_ to send me a location";
    private static final String ONFORECAST_COMMAND_FROM_HISTORY = "Select the city from your recent requests, " +
                                                    "_New_ to send a new city or _Location_ to send me a location";
    private static final String ONFORECAST_COMMAND_WITHOUT_HISTORY = "Select _New_ to send me the name of the city of _Location_ to send me a location";
    private static final String CHOOSE_OPTION = "Please, select an option from the menu";
    //END rest of the menu

    private static final String LOGTAG = "WEATHERHANDLER";

    private static final int STARTSTATE = 0;
    private static final int MAINMENU = 1;
    private static final int CURRENTWEATHER = 2;
    private static final int CURRENTNEWWEATHER = 3;
    private static final int CURRENTLOCATIONWEATHER = 4;
    private static final int FORECASTWEATHER = 5;
    private static final int FORECASTNEWWEATHER = 6;
    private static final int FORECASTLOCATIONWEATHER = 7;
    private static final int SUBSCRIBE = 8;
    private static final int SUBSCRIBENEW = 9;
    private static final int SUBSCRIBEDELETE = 10;

    public WeatherForDayBot() {
        super();
        startAlertTimers();
    }

    @Override
    public String getBotToken() {
        return BotConfig.WEATHERBOT_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage()) {
                Message message = update.getMessage();
                if (message.hasText() || message.hasLocation()) {
                    handleIncomingMessage(message);
                }
            }
        } catch (Exception ex) {
            BotLogger.error(LOGTAG, ex);
        }
    }

    @Override
    public String getBotUsername() {
        return BotConfig.WEATHERBOT_NAME;
    }

    private static SendMessage messageOnMainMenu(Message message){
        SendMessage sendMessageRequest;
        if (message.hasText()){
            if (message.getText().equals(getCurrentCommand())){
                sendMessageRequest = onCurrentChosen(message);
            }
            else if (message.getText().equals(getForecastCommand())){
                sendMessageRequest = onForecastChosen(message);
            }
            else if (message.getText().equals(getSubscribesCommand())){
                sendMessageRequest = onSubscribeChosen(message);
            }
            else if (message.getText().equals(Commands.help) || message.getText().equals(Commands.start)){
                sendMessageRequest = sendHelpMessage(message.getChatId(), message.getMessageId(), getMainMenuKeyboard());
            }
            else {
                sendMessageRequest = sendChooseOptionMessage(message.getChatId(), message.getMessageId(),
                        getMainMenuKeyboard());
            }
        }
        else {
            sendMessageRequest = sendChooseOptionMessage(message.getChatId(), message.getMessageId(),
                    getMainMenuKeyboard());
        }
        return sendMessageRequest;
    }

    private static SendMessage onSubscribeChosen(Message message){
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);

        ReplyKeyboardMarkup replyKeyboardMarkup = getSubscribesKeyboard();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setChatId(message.getChatId());
        sendMessage.setText(CHOOSE_OPTION);

        DatabaseManager.getInstance().insertWeatherState(message.getFrom().getId(), message.getChatId(), SUBSCRIBE);
        return sendMessage;
    }

    private static SendMessage onCancelCommand(Long chatID, Integer userID, Integer messageID, ReplyKeyboard replyKeyboard){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatID.toString());
        sendMessage.enableMarkdown(true);
        sendMessage.setReplyToMessageId(messageID);
        sendMessage.setReplyMarkup(replyKeyboard);
        sendMessage.setText(ONCANCEL_COMMAND);

        DatabaseManager.getInstance().insertWeatherState(userID, chatID, MAINMENU);
        return sendMessage;
    }

    //Block incoming message handle

    private void handleIncomingMessage(Message message) throws TelegramApiException {
        final int state = DatabaseManager.getInstance().getWeatherState(message.getFrom().getId(), message.getChatId());
        SendMessage sendMessageRequest;
        switch (state) {
            case MAINMENU:
                sendMessageRequest = messageOnMainMenu(message);
                break;
            case CURRENTWEATHER:
            case CURRENTNEWWEATHER:
            case CURRENTLOCATIONWEATHER:
                sendMessageRequest = messageOnCurrentWeather(message, state);
                break;
            case FORECASTWEATHER:
            case FORECASTNEWWEATHER:
            case FORECASTLOCATIONWEATHER:
                sendMessageRequest = messageOnForecastWeather(message, state);
                break;
            case SUBSCRIBE:
            case SUBSCRIBENEW:
            case SUBSCRIBEDELETE:
                sendMessageRequest = messageOnSubscribe(message, state);
                break;
            default:
                sendMessageRequest = sendMessageDefault(message);
                break;
        }

        execute(sendMessageRequest);
    }

    //EndBlock incoming message handle

    //Block for Main menu

    private static SendMessage onForecastWeather(Message message){
        SendMessage sendMessageRequest = null;
        if (message.hasText()){
            if (message.getText().startsWith(getNewCommand())){
                sendMessageRequest = onNewForecastWeatherCommand(message.getChatId(), message.getFrom().getId(),
                        message.getMessageId());
            }
            else if (message.getText().startsWith(getLocationCommand())){
                sendMessageRequest = onLocationForecastWeatherCommand(message.getChatId(), message.getFrom().getId(),
                        message.getMessageId());
            }
            else if (message.getText().startsWith(getCancelCommand())){
                sendMessageRequest = onCancelCommand(message.getChatId(), message.getFrom().getId(),
                        message.getMessageId(), getMainMenuKeyboard());
            }
            else {
                sendMessageRequest = onForecastWeatherCityReceived(message.getChatId(), message.getFrom().getId(),
                        message.getMessageId(), message.getText());
            }
        }
        return sendMessageRequest;
    }

    private static SendMessage onCurrentChosen(Message message){
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);

        ReplyKeyboardMarkup replyKeyboardMarkup = getRecentsKeyboard(message.getFrom().getId());
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setChatId(message.getChatId());
        if (replyKeyboardMarkup.getKeyboard().size() > 3){
            sendMessage.setText(ONCURRENT_COMMAND_FROM_HISTORY);
        }
        else {
            sendMessage.setText(ONCURRENT_COMMAND_WITHOUT_HISTORY);
        }

        DatabaseManager.getInstance().insertWeatherState(message.getFrom().getId(), message.getChatId(), CURRENTWEATHER);
        return sendMessage;
    }

    private static SendMessage onForecastChosen(Message message){
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);

        ReplyKeyboardMarkup replyKeyboardMarkup = getRecentsKeyboard(message.getFrom().getId());
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setChatId(message.getChatId());
        if (replyKeyboardMarkup.getKeyboard().size() > 3){
            sendMessage.setText(ONFORECAST_COMMAND_FROM_HISTORY);
        }
        else {
            sendMessage.setText(ONFORECAST_COMMAND_WITHOUT_HISTORY);
        }

        DatabaseManager.getInstance().insertWeatherState(message.getFrom().getId(), message.getChatId(), FORECASTWEATHER);
        return sendMessage;
    }

    private static SendMessage onForecastWeatherCityReceived(Long chatID, Integer userID, Integer messageID, String cityName){
        Integer cityID = DatabaseManager.getInstance().getRecentWeatherIdByCity(userID, cityName);
        if (cityID != null){
            String weather = WeatherService.getInstance().receiveWeatherForecast(cityID.toString(), userID);
            SendMessage sendMessage = new SendMessage();
            sendMessage.enableMarkdown(true);
            sendMessage.setReplyToMessageId(messageID);
            sendMessage.setReplyMarkup(getMainMenuKeyboard());
            sendMessage.setChatId(chatID.toString());
            sendMessage.setText(weather);

            DatabaseManager.getInstance().insertWeatherState(userID, chatID, MAINMENU);
            return sendMessage;
        }
        else {
            return sendChooseOptionMessage(chatID, messageID, getRecentsKeyboard(userID));
        }
    }

    //EndBlock for Main menu

    //Bloc for Current Weather

    private static SendMessage messageOnCurrentWeather(Message message, int state){
        SendMessage sendMessageRequest = null;
        switch (state){
            case CURRENTWEATHER:
                sendMessageRequest = onCurrentWeather(message);
                break;
            case CURRENTNEWWEATHER:
                sendMessageRequest = onCurrentNewWeather(message);
                break;
            case CURRENTLOCATIONWEATHER:
                sendMessageRequest = onCurrentLocationWeather(message);
                break;
        }
        return sendMessageRequest;
    }

    private static SendMessage onCurrentWeather(Message message){
        SendMessage sendMessage = null;
        if (message.hasText()){
            if (message.getText().startsWith(getNewCommand())){
                sendMessage = onNewCurrentWeatherCommand(message.getChatId(), message.getFrom().getId(), message.getMessageId());
            }
            else if (message.getText().startsWith(getLocationCommand())){
                sendMessage = onLocationCurrentWeatherCommand(message.getChatId(), message.getFrom().getId(), message.getMessageId());
            }
            else if (message.getText().startsWith(getCancelCommand())){
                sendMessage = onCancelCommand(message.getChatId(), message.getFrom().getId(), message.getMessageId(),
                        getMainMenuKeyboard());
            }
            else {
                sendMessage = onCurrentWeatherCityReceived(message.getChatId(), message.getFrom().getId(), message.getMessageId(),
                        message.getText());
            }
        }
        return sendMessage;
    }

    private static SendMessage onCurrentNewWeather(Message message){
        if (message.isReply()){
            return onCurrentWeatherReceived(message.getChatId(), message.getFrom().getId(), message.getMessageId(),
                    message.getText());
        }
        else {
            return sendMessageDefault(message);
        }
    }

    private static SendMessage onCurrentLocationWeather(Message message){
        if (message.isReply() && message.hasLocation()){
            return onCurrentLocationWeatherReceived(message);
        }
        else {
            return sendMessageDefault(message);
        }
    }

    private static SendMessage onCurrentWeatherCityReceived(Long chatID, Integer userID, Integer messageID, String cityName){
        Integer cityID = DatabaseManager.getInstance().getRecentWeatherIdByCity(userID, cityName);
        if (cityID != null){
            String weather = WeatherService.getInstance().receiveWeatherCurrent(cityID.toString(), userID);
            SendMessage sendMessage = new SendMessage();
            sendMessage.enableMarkdown(true);
            sendMessage.setReplyMarkup(getMainMenuKeyboard());
            sendMessage.setReplyToMessageId(messageID);
            sendMessage.setChatId(chatID.toString());
            sendMessage.setText(weather);
            DatabaseManager.getInstance().insertWeatherState(userID, chatID, MAINMENU);
            return sendMessage;
        }
        else {
            return sendChooseOptionMessage(chatID, messageID, getRecentsKeyboard(userID));
        }
    }

    private static SendMessage onNewCurrentWeatherCommand(Long chatID, Integer userID, Integer messageID){
        ForceReplyKeyboard forceReplyKeyboard = getForceReply();

        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatID.toString());
        sendMessage.setReplyToMessageId(messageID);
        sendMessage.setText(ONWEATHER_NEW_COMMAND);
        sendMessage.setReplyMarkup(forceReplyKeyboard);

        DatabaseManager.getInstance().insertWeatherState(userID, chatID, CURRENTNEWWEATHER);
        return sendMessage;
    }

    private static SendMessage onLocationCurrentWeatherCommand(Long chatID, Integer userID, Integer messageID){
        ForceReplyKeyboard forceReplyKeyboard = getForceReply();

        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatID.toString());
        sendMessage.setReplyToMessageId(messageID);
        sendMessage.setReplyMarkup(forceReplyKeyboard);
        sendMessage.setText(ONWEATHER_LOCATION_COMMAND);

        DatabaseManager.getInstance().insertWeatherState(userID, chatID, CURRENTLOCATIONWEATHER);
        return sendMessage;
    }

    //EndBlock for Current Weather

    //Block for Forecast Weather

    private static SendMessage messageOnForecastWeather(Message message, int state){
        SendMessage sendMessageRequest = null;
        switch (state){
            case FORECASTWEATHER:
                sendMessageRequest = onForecastWeather(message);
                break;
            case FORECASTNEWWEATHER:
                sendMessageRequest = onForecastNewWeather(message);
                break;
            case FORECASTLOCATIONWEATHER:
                sendMessageRequest = onForecastLocationWeather(message);
                break;
        }
        return sendMessageRequest;
    }

    private static SendMessage onSubscribeOptionSelected(Message message){
        SendMessage sendMessageRequest = null;
        if (message.hasText()){
            if (message.getText().equals(getNewCommand())){
                sendMessageRequest = onNewSubscribeCommand(message);
            }
            else if (message.getText().equals(getDeleteCommand())){
                sendMessageRequest = onDeleteSubscribeCommand(message);
            }
            else if (message.getText().contains(getListCommand())){
                sendMessageRequest = onListSubscribeCommand(message);
            }
            else if (message.getText().equals(getBackCommand())){
                sendMessageRequest = onBackSubscribeCommand(message);
            }
            else {
                sendMessageRequest = sendChooseOptionMessage(message.getChatId(), message.getMessageId(),
                        getSubscribesKeyboard());
            }
        }
        return sendMessageRequest;
    }

    private static SendMessage onForecastNewWeather(Message message){
        if (message.isReply()){
            return onForecastWeatherReceived(message.getChatId(), message.getFrom().getId(),
                    message.getMessageId(), message.getText());
        }
        else {
            return sendMessageDefault(message);
        }
    }

    private static SendMessage onNewForecastWeatherCommand(Long chatID, Integer userID, Integer messageID){
        ForceReplyKeyboard forceReplyKeyboard = getForceReply();

        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatID.toString());
        sendMessage.setReplyToMessageId(messageID);
        sendMessage.setReplyMarkup(forceReplyKeyboard);
        sendMessage.setText(ONWEATHER_NEW_COMMAND);

        DatabaseManager.getInstance().insertWeatherState(userID, chatID, FORECASTNEWWEATHER);
        return sendMessage;
    }

    private static SendMessage onLocationForecastWeatherCommand(Long chatID, Integer userID, Integer messageID){
        ForceReplyKeyboard forceReplyKeyboard = getForceReply();

        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatID.toString());
        sendMessage.setReplyMarkup(forceReplyKeyboard);
        sendMessage.setReplyToMessageId(messageID);
        sendMessage.setText(ONWEATHER_LOCATION_COMMAND);

        DatabaseManager.getInstance().insertWeatherState(userID, chatID, FORECASTLOCATIONWEATHER);
        return sendMessage;
    }

    private static SendMessage onNewSubscribeCommand(Message message){
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);

        sendMessage.setChatId(message.getChatId());
        sendMessage.setReplyMarkup(getRecentsKeyboard(message.getFrom().getId(), false));
        if (getRecentsKeyboard(message.getFrom().getId(), false).getKeyboard().size() < 2){
            sendMessage.setText(YOU_NEED_THIS_FIRST);
        }
        else {
            sendMessage.setText(CHOOSE_NEW_SUBSCRIBE_CUTY);
        }
        sendMessage.setReplyToMessageId(message.getMessageId());

        DatabaseManager.getInstance().insertWeatherState(message.getFrom().getId(), message.getChatId(), SUBSCRIBENEW);
        return sendMessage;
    }

    private static SendMessage onForecastLocationWeather(Message message){
        if (message.isReply() && message.hasLocation()){
            return onForecastWeatherLocationReceived(message);
        }
        else {
            return sendMessageDefault(message);
        }
    }

    //EndBlock for Forecast Weather

    //This block for Subscribe menu option selected

    private static SendMessage messageOnSubscribe(Message message, int state){
        SendMessage sendMessageRequest = null;
        switch (state) {
            case SUBSCRIBE:
                sendMessageRequest = onSubscribeOptionSelected(message);
                break;
            case SUBSCRIBENEW:
                sendMessageRequest = onNewSubscribeOptionSelected(message);
                break;
            case SUBSCRIBEDELETE:
                sendMessageRequest = onDeleteSubscribeOptionSelected(message);
                break;
        }
        return sendMessageRequest;
    }

    private static SendMessage onBackSubscribeCommand(Message message){
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);

        ReplyKeyboardMarkup replyKeyboardMarkup = getMainMenuKeyboard();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setChatId(message.getChatId());
        sendMessage.setText(getHelpMessage());

        DatabaseManager.getInstance().insertWeatherState(message.getFrom().getId(), message.getChatId(), MAINMENU);
        return sendMessage;
    }

    private static SendMessage onNewSubscribeOptionSelected(Message message){
        SendMessage sendMessageRequest = null;
        if (message.hasText()){
            if (message.getText().equals(getCancelCommand())){
                SendMessage sendMessage = new SendMessage();
                sendMessage.enableMarkdown(true);
                sendMessage.setChatId(message.getChatId());
                sendMessage.setReplyToMessageId(message.getMessageId());
                sendMessage.setReplyMarkup(getSubscribesKeyboard());
                sendMessage.setText(CHOOSE_OPTION);
                DatabaseManager.getInstance().insertWeatherState(message.getFrom().getId(), message.getChatId(), SUBSCRIBE);
                sendMessageRequest = sendMessage;
            }
            else {
                sendMessageRequest = onNewSubscribeCityReceived(message);
            }
        }
        return sendMessageRequest;
    }

    private static SendMessage onDeleteSubscribeCommand(Message message){
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);

        sendMessage.setChatId(message.getChatId());

        ReplyKeyboardMarkup replyKeyboardMarkup = getSubscribesListKeyboad(message.getFrom().getId());
        if (replyKeyboardMarkup != null){
            sendMessage.setReplyMarkup(replyKeyboardMarkup);
            sendMessage.setText(CHOOSE_DELETE_SUBSCRIBE_CITY);
            DatabaseManager.getInstance().insertWeatherState(message.getFrom().getId(), message.getChatId(), SUBSCRIBEDELETE);
        }
        else {
            sendMessage.setReplyMarkup(getSubscribesKeyboard());
            sendMessage.setText(NO_SUBSCRIBE);
        }

        sendMessage.setReplyToMessageId(message.getMessageId());
        return sendMessage;
    }

    private static SendMessage onListSubscribeCommand(Message message){
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);

        sendMessage.setChatId(message.getChatId());
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setReplyMarkup(getSubscribesKeyboard());
        sendMessage.setText(getSubscribeListMessage(message.getFrom().getId()));
        sendMessage.setReplyToMessageId(message.getMessageId());

        return sendMessage;
    }

    private static ReplyKeyboardMarkup getSubscribesKeyboard(){
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow firstRow = new KeyboardRow();
        firstRow.add(getNewCommand());
        KeyboardRow secondRow = new KeyboardRow();
        secondRow.add(getDeleteCommand());
        KeyboardRow thirdRow = new KeyboardRow();
        thirdRow.add(getListCommand());
        KeyboardRow fourthRow = new KeyboardRow();
        fourthRow.add(getBackCommand());

        keyboard.add(firstRow);
        keyboard.add(secondRow);
        keyboard.add(thirdRow);
        keyboard.add(fourthRow);

        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }

    private static String getListCommand(){
        return LISTOFSUBSCRIBES;
    }

    private static SendMessage onNewSubscribeCityReceived(Message message){
        int userID = message.getFrom().getId();
        Integer cityID = DatabaseManager.getInstance().getRecentWeatherIdByCity(userID, message.getText());
        if (cityID != null){
            DatabaseManager.getInstance().createNewWeatherAlert(userID, cityID, message.getText());
            SendMessage sendMessageRequest = new SendMessage();
            sendMessageRequest.enableMarkdown(true);
            sendMessageRequest.setChatId(message.getChatId());
            sendMessageRequest.setReplyToMessageId(message.getMessageId());
            sendMessageRequest.setReplyMarkup(getSubscribesKeyboard());
            sendMessageRequest.setText(getChooseNewSubscribeSetMessage(message.getText()));

            DatabaseManager.getInstance().insertWeatherState(userID, message.getChatId(), SUBSCRIBE);
            return sendMessageRequest;
        }
        else {
            return sendChooseOptionMessage(message.getChatId(), message.getMessageId(),
                    getRecentsKeyboard(message.getFrom().getId(), false));
        }
    }

    private static SendMessage onDeleteSubscribeOptionSelected(Message message){
        SendMessage sendMessageRequest = null;
        if (message.hasText()){
            if (message.getText().equals(getCancelCommand())){
                sendMessageRequest = onSubscribeDeleteBackOptionSelected(message);
            }
            else if (DatabaseManager.getInstance()
                    .getAlertCitiesNameByUser(message.getFrom().getId()).contains(message.getText())){
                sendMessageRequest = onSubscribeCityDeleteOptionSelected(message);
            }
            else {
                sendMessageRequest = sendChooseOptionMessage(message.getChatId(), message.getMessageId(),
                        getSubscribesListKeyboad(message.getFrom().getId()));
            }
        }
        return sendMessageRequest;
    }

    private static SendMessage onSubscribeDeleteBackOptionSelected(Message message){
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(message.getChatId());
        sendMessage.setReplyMarkup(getSubscribesKeyboard());
        sendMessage.setReplyToMessageId(message.getMessageId());

        DatabaseManager.getInstance().insertWeatherState(message.getFrom().getId(), message.getChatId(), SUBSCRIBE);
        return sendMessage;
    }

    private static SendMessage onSubscribeCityDeleteOptionSelected(Message message){
        DatabaseManager.getInstance().deleteAlertCity(message.getFrom().getId(), message.getText());
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(message.getChatId());
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setReplyMarkup(getSubscribesKeyboard());
        sendMessage.setText(SUBSCRIBE_DELETE);

        DatabaseManager.getInstance().insertWeatherState(message.getFrom().getId(), message.getChatId(), SUBSCRIBE);
        return sendMessage;
    }

    /*
        *Endblock of Subscribe menu
     */

    /*
        *Block of ReplyKeyboard
     */

    private static ReplyKeyboardMarkup getMainMenuKeyboard(){
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstrow = new KeyboardRow();
        keyboardFirstrow.add(getCurrentCommand());
        KeyboardRow keyboardSecondrow = new KeyboardRow();
        keyboardSecondrow.add(getForecastCommand());
        KeyboardRow keyboardThirdrow = new KeyboardRow();
        keyboardThirdrow.add(getSubscribesCommand());
        keyboard.add(keyboardFirstrow);
        keyboard.add(keyboardSecondrow);
        keyboard.add(keyboardThirdrow);
        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }

    private static ReplyKeyboardMarkup getRecentsKeyboard(Integer userID){
        return getRecentsKeyboard(userID, true);
    }

    private static ReplyKeyboardMarkup getRecentsKeyboard(Integer userID, boolean allowNew){
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();
        for (String recentWeather : DatabaseManager.getInstance().getRecentWeather(userID)){
            KeyboardRow row = new KeyboardRow();
            row.add(recentWeather);
            keyboard.add(row);
        }

        KeyboardRow row = new KeyboardRow();
        if (allowNew) {
            row.add(getLocationCommand());
            keyboard.add(row);

            row = new KeyboardRow();
            row.add(getNewCommand());
            keyboard.add(row);

            row = new KeyboardRow();
        }
        row.add(getCancelCommand());
        keyboard.add(row);

        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }

    private static String getSubscribesCommand(){
        return SUBSCRIBES;
    }

    private static ReplyKeyboardMarkup getSubscribesListKeyboad(Integer userID){
        ReplyKeyboardMarkup replyKeyboardMarkup = null;

        List<String> subscribesCityNames = DatabaseManager.getInstance().getAlertCitiesNameByUser(userID);
        if (subscribesCityNames.size() > 0){
            replyKeyboardMarkup = new ReplyKeyboardMarkup();
            replyKeyboardMarkup.setSelective(true);
            replyKeyboardMarkup.setResizeKeyboard(true);
            replyKeyboardMarkup.setOneTimeKeyboard(true);

            List<KeyboardRow> keyboard = new ArrayList<>();
            for (String subscribedCityName : subscribesCityNames) {
                KeyboardRow row = new KeyboardRow();
                row.add(subscribedCityName);
                keyboard.add(row);
            }
            KeyboardRow row = new KeyboardRow();
            row.add(getCancelCommand());
            keyboard.add(row);

            replyKeyboardMarkup.setKeyboard(keyboard);
        }

        return replyKeyboardMarkup;
    }

    private static ForceReplyKeyboard getForceReply(){
        ForceReplyKeyboard forceReplyKeyboard = new ForceReplyKeyboard();
        forceReplyKeyboard.setSelective(true);
        return forceReplyKeyboard;
    }

    /*
        *Endblock of ReplyKeyboard
     */


    //Block of getCommands

    private static SendMessage sendHelpMessage(Long chatID, Integer messageID, ReplyKeyboardMarkup replyKeyboardMarkup){
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatID);
        sendMessage.setReplyToMessageId(messageID);
        if (replyKeyboardMarkup != null){
            sendMessage.setReplyMarkup(replyKeyboardMarkup);
        }
        sendMessage.setText(HELP_WEATHER_MESSAGE);
        return sendMessage;
    }

    private static String getDeleteCommand(){
        return DELETE;
    }

    private void startAlertTimers(){
        TimerExecutor.getInstance().startExecutionEveryDayAt(new CustomTimerTask("First day alert", -1) {
            @Override
            public void execute(){
                sendAlerts();
            }
        }, 0, 0, 0);

        TimerExecutor.getInstance().startExecutionEveryDayAt(new CustomTimerTask("Second day alert", -1){
            @Override
            public void execute(){
                sendAlerts();
            }
        }, 12, 0, 0);
    }

    private static String getBackCommand(){
        return BACK;
    }

    private static String getNewCommand(){
        return NEW;
    }

    private static String getLocationCommand(){
        return LOCATION;
    }

    private static String getCurrentCommand(){
        return CURRENT;
    }

    private static String getForecastCommand(){
        return FORECAST;
    }

    private static String getCancelCommand(){
        return CANCEL;
    }

    //EndBlock of getCommands


    //Block of getMessages

    private static String getHelpMessage(){
        return HELP_WEATHER_MESSAGE;
    }

    private static String getChooseNewSubscribeSetMessage(String city){
        return String.format(NEW_SUBSCRIBE_SAVED, city);
    }

    private static String getSubscribeListMessage(Integer userID){
        String subscribeListMessage;

        List<String> subcribeCities = DatabaseManager.getInstance().getAlertCitiesNameByUser(userID);
        if (subcribeCities.size() > 0){
            String fullListOfSubscribes = "";
            for (String subscribeCity : subcribeCities){
                fullListOfSubscribes += String.format(PARTIAL_SUBSCRIBE, subscribeCity);
            }
            subscribeListMessage = String.format(INITIAL_SUBSCRIBE_STRING, subcribeCities.size(), fullListOfSubscribes);
        }
        else {
            subscribeListMessage = NO_SUBSCRIBE;
        }
        return subscribeListMessage;
    }

    //EndBlock of getMessages


    //Block of Send common messages

    private void sendAlerts(){
        List<WeatherAlert> alertList = DatabaseManager.getInstance().getAllAlerts();
        for (WeatherAlert weatherAlert : alertList){
            synchronized (Thread.currentThread()){
                try {
                    Thread.currentThread().wait(35);
                }
                catch (InterruptedException ex){
                    BotLogger.severe(LOGTAG, ex);
                }
            }
            String weather = WeatherService.getInstance().receiveWeatherAlert(weatherAlert.getCityID(), weatherAlert.getUserID());
            SendMessage sendMessage = new SendMessage();
            sendMessage.enableMarkdown(true);
            sendMessage.setChatId(String.valueOf(weatherAlert.getUserID()));
            sendMessage.setText(weather);
            try {
                execute(sendMessage);
            }
            catch (TelegramApiRequestException ex){
                BotLogger.warn(LOGTAG, ex);
                if (ex.getApiResponse().contains("Can't access the chat") ||
                        ex.getApiResponse().contains("Bot was blocked by the user")){
                    DatabaseManager.getInstance().deleteAlertsForUser(weatherAlert.getUserID());
                }
            }
            catch (Exception ex){
                BotLogger.severe(LOGTAG, ex);
            }
        }
    }

    private static SendMessage sendMessageDefault(Message message){
        ReplyKeyboardMarkup replyKeyboardMarkup = getMainMenuKeyboard();
        DatabaseManager.getInstance().insertWeatherState(message.getFrom().getId(), message.getChatId(), MAINMENU);
        return sendHelpMessage(message.getChatId(), message.getMessageId(), replyKeyboardMarkup);
    }

    private static SendMessage sendChooseOptionMessage(Long chatID, Integer messageID, ReplyKeyboardMarkup replyKeyboardMarkup){
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatID.toString());
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        sendMessage.setReplyToMessageId(messageID);
        sendMessage.setText(CHOOSE_OPTION);

        return sendMessage;
    }

    //EndBlock of Send common messages


    //Block sending weather

    private static SendMessage onCurrentWeatherReceived(Long chatID, Integer userID, Integer messageID, String cityName){
        String weather = WeatherService.getInstance().receiveWeatherCurrent(cityName, userID);
        SendMessage sendMessageRequest = new SendMessage();
        sendMessageRequest.enableMarkdown(true);
        sendMessageRequest.setReplyMarkup(getMainMenuKeyboard());
        sendMessageRequest.setReplyToMessageId(messageID);
        sendMessageRequest.setChatId(chatID.toString());
        sendMessageRequest.setText(weather);

        DatabaseManager.getInstance().insertWeatherState(userID, chatID, MAINMENU);
        return sendMessageRequest;
    }

    private static SendMessage onCurrentLocationWeatherReceived(Message message){
        String weather = WeatherService.getInstance().receiveWeatherCurrentByLocation(message.getLocation().getLongitude(),
                message.getLocation().getLatitude(), message.getFrom().getId());
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setReplyMarkup(getMainMenuKeyboard());
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setChatId(message.getChatId());
        sendMessage.setText(weather);

        DatabaseManager.getInstance().insertWeatherState(message.getFrom().getId(), message.getChatId(), MAINMENU);
        return sendMessage;
    }

    private static SendMessage onForecastWeatherReceived(Long chatID, Integer userID, Integer messageID, String cityName){
        String weather = WeatherService.getInstance().receiveWeatherForecast(cityName, userID);
        SendMessage sendMessageRequest = new SendMessage();
        sendMessageRequest.enableMarkdown(true);
        sendMessageRequest.setReplyMarkup(getMainMenuKeyboard());
        sendMessageRequest.setReplyToMessageId(messageID);
        sendMessageRequest.setChatId(chatID.toString());
        sendMessageRequest.setText(weather);

        DatabaseManager.getInstance().insertWeatherState(userID, chatID, MAINMENU);
        return sendMessageRequest;
    }

    private static SendMessage onForecastWeatherLocationReceived(Message message){
        String weather = WeatherService.getInstance().receiveForecastWeatherByLocation(message.getLocation().getLongitude(),
                message.getLocation().getLatitude(), message.getFrom().getId());
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setReplyMarkup(getMainMenuKeyboard());
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setChatId(message.getChatId());
        sendMessage.setText(weather);

        DatabaseManager.getInstance().insertWeatherState(message.getFrom().getId(), message.getChatId(), MAINMENU);
        return sendMessage;
    }

    //EndBlock sending weather
}
