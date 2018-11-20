package telegram.services;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.logging.BotLogger;
import telegram.BuildVars;
import telegram.database.DatabaseManager;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * @author Nikita Zinoviev
 * @version 1.0
 * @brief Weather service
 */
public class WeatherService {
    private static final String weatherCurrent = "The weather for: %s is:\n\n%s";
    private static final String cityNotFound = "City not found, sorry";
    private static final String errorReceivingWeather = "Something went wrong, I couldn't get the weather for a given request";
    private static final String weatherForecast = "In the next three days in _%s_ will be:\n\n%s";
    private static final String forecastWeather = "\t- On *%s* \n\t- _Forecast:_ %s\n\t- _Max temperature:_ %s C\n\t- _Min temperature:_ %s C\n\n";
    private static final String subscribeWeather = "\t _Forecast:_ %s\n\t- _Max temperature:_ %s C\n\t- _Min temperature:_ %s C\n\n";

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy"); // <-- Date to text

    private static final String LOGTAG = "WEATHERSERVICE";

    public static final String METRICSYSTEM = "metric";

    private static final String BASEURL = "http://api.openweathermap.org/data/2.5/"; //<-- This is BASE url
    private static final String FORECASTPATH = "forecast/daily";
    private static final String CURRENTPATH = "weather";
    private static final String UNITSMETRIC = "&units=metric";
    private static final String APIIDEND = "&APPID=" + BuildVars.OPENWEATHERAPIKEY;
    private static volatile WeatherService instance; // <-- instance of this class

    private WeatherService(){
    }

    /**
     * Singleton
     *
     * @return Return the instance of the class
     */
    public static WeatherService getInstance(){
        WeatherService currentInstance;
        if (instance == null){
            synchronized (WeatherService.class){
                if (instance == null){
                    instance = new WeatherService();
                }
                currentInstance = instance;
            }
        }
        else {
            currentInstance = instance;
        }
        return currentInstance;
    }

    /**
     * Receive the weather of the city for one day
     *
     * @param cityId City to get the weather
     * @return userHash to be send to use
     *
     */
 /*   public String receiveWeatherAlert(int cityId, int userId){
        String cityFound;
        String responseToUser;
        try {
            String completeURL = BASEURL + FORECASTPATH + "?" + getCityQuery(cityId +"") + FORECASTPARAMS + APIIDEND;
            CloseableHttpClient client = HttpClientBuilder.create().setSSLHostnameVerifier(new NoopHostnameVerifier()).build();
            HttpGet request = new HttpGet(completeURL);

            CloseableHttpResponse response = client.execute(request);
            HttpEntity ht = response.getEntity();

            BufferedHttpEntity buf = new BufferedHttpEntity(ht);
            String responseString = EntityUtils.toString(buf, "UTF-8");

            JSONObject jsonObject = new JSONObject(responseString);
            BotLogger.info(LOGTAG, jsonObject.toString());
            if (jsonObject.getInt("cod") == 200) {
                cityFound = jsonObject.getJSONObject("city").getString("name") + " (" +
                        jsonObject.getJSONObject("city").getString("country") + ")";
                saveRecentWeather(userId, cityFound, jsonObject.getJSONObject("city").getInt("id"));
                responseToUser = String.format(cityFound, convertListOfForecastToString(jsonObject, false));
            }
            else {
                BotLogger.warn(LOGTAG, jsonObject.toString());
                responseToUser = "CityNotFound";
            }
        }
        catch (Exception ex){
            BotLogger.error(LOGTAG, ex);
            responseToUser = "errorReceivingWeather";
        }
        return responseToUser;
    }
    */

    /**
     * Receive the current weather by the city name
     * @param city City to get the weather
     * @return userHash to be send to use
     */
    public String receiveWeatherCurrent(String city, Integer userID){
        String cityFound;
        String responseToUser;
        try {
            String completeURL = BASEURL + CURRENTPATH + "?" + getCityQuery(city) + UNITSMETRIC + APIIDEND;
            CloseableHttpClient client = HttpClientBuilder.create().setSSLHostnameVerifier(new NoopHostnameVerifier()).build();
            HttpGet request = new HttpGet(completeURL);
            CloseableHttpResponse response = client.execute(request);
            HttpEntity ht = response.getEntity();

            BufferedHttpEntity buf = new BufferedHttpEntity(ht);
            String responseString = EntityUtils.toString(buf, "UTF-8");

            JSONObject jsonObject = new JSONObject(responseString);
            if (jsonObject.getInt("cod") == 200){
                cityFound = jsonObject.getString("name") + " (" + jsonObject.getJSONObject("sys").getString("country") + ")";
                saveRecentWeather(userID, cityFound, jsonObject.getInt("id"));
                responseToUser = String.format(weatherCurrent, cityFound, convertCurrentWeatherToString(jsonObject));
            }
            else {
                BotLogger.warn(LOGTAG, jsonObject.toString());
                responseToUser = cityNotFound;
            }
        }
        catch (Exception ex){
            BotLogger.error(LOGTAG, ex);
            responseToUser = errorReceivingWeather;
        }
        return responseToUser;
    }

    /**
     * Receive the current weather of the city by coordinates
     * @param longitude current longitude
     * @param latitude current latitude
     * @return userHash to be send to use
     */
    public String receiveWeatherCurrentByLocation(Float longitude, Float latitude, Integer userID){
        String cityFound;
        String responseToUser;
        try {
            String completeURL = BASEURL + CURRENTPATH + "?lat=" + URLEncoder.encode(latitude + "", "UTF-8") +
                    "&lon=" + URLEncoder.encode(longitude + "", "UTF-8") + UNITSMETRIC + APIIDEND;
            CloseableHttpClient client = HttpClientBuilder.create().setSSLHostnameVerifier(new NoopHostnameVerifier()).build();
            HttpGet request = new HttpGet(completeURL);
            CloseableHttpResponse response = client.execute(request);
            HttpEntity ht = response.getEntity();

            BufferedHttpEntity buf = new BufferedHttpEntity(ht);
            String responseString = EntityUtils.toString(buf, "UTF-8");

            JSONObject jsonObject = new JSONObject(responseString);
            if (jsonObject.getInt("cod") == 200){
                cityFound = jsonObject.getString("name") + " (" +
                        jsonObject.getJSONObject("sys").getString("country") + ")";
                saveRecentWeather(userID, cityFound, jsonObject.getInt("id"));
                responseToUser = String.format(weatherCurrent, cityFound, convertCurrentWeatherToString(jsonObject));
            }
            else {
                BotLogger.warn(LOGTAG, jsonObject.toString());
                responseToUser = cityNotFound;
            }
        }
        catch (Exception ex){
            BotLogger.error(LOGTAG, ex);
            responseToUser = errorReceivingWeather;
        }
        return responseToUser;
    }


    /**
     * Forecast for the following few days by name of the city
     * @param city transmitted city by user
     * @param userID
     * @return response to User
     */
    public String receiveWeatherForecast(String city, Integer userID){
        String cityFound;
        String responseToUser;
        try {
            String completeURL = BASEURL + FORECASTPATH + "?" + getCityQuery(city) + UNITSMETRIC + APIIDEND;
            CloseableHttpClient client = HttpClientBuilder.create().setSSLHostnameVerifier(new NoopHostnameVerifier()).build();
            HttpGet request = new HttpGet(completeURL);

            CloseableHttpResponse response = client.execute(request);
            HttpEntity ht = response.getEntity();

            BufferedHttpEntity buf = new BufferedHttpEntity(ht);
            String responseString = EntityUtils.toString(buf, "UTF-8");

            JSONObject jsonObject = new JSONObject(responseString);
            if (jsonObject.getInt("cod") == 200){
                cityFound = jsonObject.getJSONObject("city").getString("name") + " (" +
                        jsonObject.getJSONObject("city").getString("country") + ")";
                saveRecentWeather(userID, cityFound, jsonObject.getJSONObject("city").getInt("id"));
                responseToUser = String.format(weatherForecast, cityFound, convertListOfForecastToString(jsonObject, true));
            }
            else {
                BotLogger.warn(LOGTAG, jsonObject.toString());
                responseToUser = cityNotFound;
            }
        }
        catch (Exception ex){
            BotLogger.error(LOGTAG, ex);
            responseToUser = errorReceivingWeather;
        }
        return responseToUser;
    }

    /**
     * Forecast for the following few days by location
     * @param longitude transmitted by User
     * @param latitude transmitted by User
     * @param userID
     * @return response to User
     */
    public String receiveForecastWeatherByLocation(Float longitude, Float latitude, Integer userID){
        String cityFound;
        String responseToUser;
        try {
            String completeURL = BASEURL + FORECASTPATH + "?lat=" + URLEncoder.encode(latitude + "", "UTF-8") +
                    "&lon=" + URLEncoder.encode(longitude + "", "UTF-8") + METRICSYSTEM + APIIDEND;

            CloseableHttpClient client = HttpClientBuilder.create().setSSLHostnameVerifier(new NoopHostnameVerifier()).build();
            HttpGet request = new HttpGet(completeURL);

            CloseableHttpResponse response = client.execute(request);
            HttpEntity ht = response.getEntity();

            BufferedHttpEntity buf = new BufferedHttpEntity(ht);
            String responseString = EntityUtils.toString(buf, "UTF-8");

            JSONObject jsonObject = new JSONObject(responseString);
            if (jsonObject.getInt("cod") == 200){
                cityFound = jsonObject.getJSONObject("city").getString("name") +" (" +
                        jsonObject.getJSONObject("city").getString("country") + ")";
                saveRecentWeather(userID, cityFound, jsonObject.getJSONObject("city").getInt("id"));
                responseToUser = String.format(weatherForecast, cityFound, convertListOfForecastToString(jsonObject, true));
            }
            else {
                BotLogger.warn(LOGTAG, jsonObject.toString());
                responseToUser = cityNotFound;
            }
        }
        catch (Exception ex){
            BotLogger.error(LOGTAG, ex);
            responseToUser = errorReceivingWeather;
        }
        return responseToUser;
    }

    /**
     * Convert a current weather to a string
     * @param jsonObject JSONObject containing the information about current weather
     * @return String to user
     */
    private String convertCurrentWeatherToString(JSONObject jsonObject){
        String temp = ((int)jsonObject.getJSONObject("main").getDouble("temp")) +"";
        String cloudness = jsonObject.getJSONObject("clouds").getInt("all") + "%";
        String weatherDesc = jsonObject.getJSONArray("weather").getJSONObject(0).getString("description");
        return String.format("humidity: %s \n %s \n temperature: %s C", cloudness, weatherDesc, temp);
    }


    /**
     * Convert a list of weather forecast to a list of strings to be send
     * @param jsonObject JSONObject containing the list
     * @return String to be sent to the user
     */
    private String convertListOfForecastToString(JSONObject jsonObject, boolean addDate){
        String responseToUser = "";
        for (int i = 0; i < jsonObject.getJSONArray("list").length(); i++){
            JSONObject internalJSON = jsonObject.getJSONArray("list").getJSONObject(i);
            responseToUser += convertInternalInformationToString(internalJSON, addDate);
        }
        return responseToUser;
    }

    /**
     * Convert information from JSON to String
     * @param internalJSON JSONObject with forecast data to convert
     * @param addDate if true adds the date to the user response
     * @return String to the user
     */
    private String convertInternalInformationToString(JSONObject internalJSON, boolean addDate){
        LocalDate date;
        String tempMax;
        String tempMin;
        String weather;
        date = Instant.ofEpochSecond(internalJSON.getLong("dt")).atZone(ZoneId.systemDefault()).toLocalDate();
        tempMax = String.valueOf(internalJSON.getJSONObject("temp").getDouble("max"));
        tempMin = String.valueOf(internalJSON.getJSONObject("temp").getDouble("min"));
        JSONObject weatherObject = internalJSON.getJSONArray("weather").getJSONObject(0);
        weather = weatherObject.getString("description");

        if (addDate){
            return String.format(forecastWeather, dateFormatter.format(date), weather, tempMax, tempMin);
        }
        else {
            return String.format(subscribeWeather, weather, tempMax, tempMin);
        }
    }

    private void saveRecentWeather(Integer userId, String cityName, int cityId) {
        DatabaseManager.getInstance().addRecentWeather(userId, cityId, cityName);
    }

    private String getCityQuery(String city) throws UnsupportedEncodingException {
        String cityQuerry = "";
        try {
            cityQuerry += "id=" + URLEncoder.encode(Integer.parseInt(city)+"", "UTF-8");
        }
        catch (NumberFormatException | NullPointerException ex){
            cityQuerry += "q=" + URLEncoder.encode(city, "UTF-8");
        }
        return cityQuerry;
    }
}
