package telegram.database;

/**
 * Strings to create a database
 * @author Nikita Zinovev
 * @version 1.0
 */
public class CreationStrings {
    public static final String createRecentWeatherTable = "CREATE TABLE IF NOT EXISTS RecentWeather (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "userID INTEGER NOT NULL, date TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP, cityID INTEGER NOT NULL, cityName VARCHAR(60) NOT NULL, " +
            "CONSTRAINT unique_cityuser UNIQUE (userID,cityID))";
    public static final String createLastUpdateDatabase = "CREATE TABLE IF NOT EXISTS LastUpdate (token VARCHAR(125) PRIMARY KEY, " +
            "updateID INTEGER NOT NULL DEFAULT -1";
    public static final String createWeatherStateTable = "CREATE TABLE IF NOT EXISTS WeatherState (userID INTEGER NOT NULL, " +
            "chatID BIGINT NOT NULL, state INTEGER NOT NULL DEFAULT 0, CONSTRAINT weatherPrimaryKey PRIMARY KEY(userID,chatID))";
    public static final String createWeatherAlertTable = "CREATE TABLE IF NOT EXISTS WeatherAlert (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "userID INTEGER NOT NULL, cityID INTEGER NOT NULL, cityName VARCHAR(60) NOT NULL, time INTEGER NOT NULL DEFAULT -1, " +
            "CONSTRAINT unique_cityNameAlert UNIQUE (userID, cityName), CONSTRAINT unique_cityIdAlert UNIQUE (userID, cityID))";
    public static final String createUserWeatherOptionDatabase = "CREATE TABLE IF NOT EXISTS UserWeatherOptions (userID INTEGER PRIMARY KEY, " +
            "units VARCHAR(10) NOT NULL DEFAULT 'metric')";
}
