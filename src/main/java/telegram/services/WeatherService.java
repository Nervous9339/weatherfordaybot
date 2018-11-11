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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * @author Nikita Zinoviev
 * @version 1.0
 * @brief Weather service
 */
public class WeatherService {
    private static final String LOGTAG = "WEATHERSERVICE";

    public static final String METRICSYSTEM = "metric";
    public static final String IMPERIALSYSTEM = "imperial";

    private static final String BASEURL = "http://api.openweathermap.org/data/2.5/"; //<-- This is BASE url
    private static final String FORECASTPATH = "forecast/daily";
    private static final String CURRENTPATH = "weather";
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
    public String receiveWeatherAlert(int cityId, int userId){
        String cityFound;
        String responseToUser;
        try {
            String completeURL = BASEURL + FORECASTPATH + "?" + getCityQuery(cityId +"") + APIIDEND;
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

    /**
     * Convert a list of weather forcast to a list of strings to be send
     * @param jsonObject JSONObject containing the list
     * @return String to be sent to the user
     */
    private String convertListOfForecastToString(JSONObject jsonObject, boolean addDate){
        String responseToUser = "";
        for (int i = 0; i < jsonObject.getJSONArray("list").length(); i++){
            JSONObject internalJSON = jsonObject.getJSONArray("list").getJSONObject(i);
            responseToUser += convertListOfForecastToString(internalJSON, addDate);
        }
        return responseToUser;
    }

    private void saveRecentWeather(Integer userId, String cityName, int cityId) {
        //TODO Fill in when will the database
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
