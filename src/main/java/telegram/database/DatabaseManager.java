package telegram.database;



import org.telegram.telegrambots.meta.logging.BotLogger;
import telegram.services.WeatherAlert;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Database manager to perform database operations
 * @author Nikita Zinoviev
 * @version 1.0
 */
public class DatabaseManager {
    private static final String LOGTAG = "DATABASEMANAGER";

    private static volatile DatabaseManager instance;
    private static volatile ConectionDB conection;

    // private constructor due to Singleton
    private DatabaseManager(){
        conection = new ConectionDB();
        createTable();
    }

    /**
     * Get Singleton instance
     * @return instance of the class
     */
    public static DatabaseManager getInstance(){
        final DatabaseManager currentInstance;
        if (instance == null){
            synchronized (DatabaseManager.class){
                if (instance == null){
                    instance = new DatabaseManager();
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
     * Create the DB
     */
    private void createTable(){
        try {
            conection.initTransaction();
            createNewTables();
            conection.commitTransaction();
        }
        catch (SQLException ex){
            BotLogger.error(LOGTAG, ex);
        }
    }

    private void createNewTables() throws SQLException{
        conection.executeQuery(CreationStrings.createRecentWeatherTable);
        conection.executeQuery(CreationStrings.createWeatherStateTable);
        conection.executeQuery(CreationStrings.createWeatherAlertTable);
    }

    public boolean addRecentWeather(Integer userID, Integer cityID, String cityName) {
        int updatedRows = 0;
        try {
            final PreparedStatement preparedStatement =
                    conection.getPreparedStatement("REPLACE INTO RecentWeather (userID, cityID, cityName) VALUES(?, ?, ?)");
            preparedStatement.setInt(1, userID);
            preparedStatement.setInt(2, cityID);
            preparedStatement.setString(3, cityName);
            updatedRows = preparedStatement.executeUpdate();
        }
        catch (SQLException ex){
            ex.printStackTrace();
        }
        //cleanUpRecent(userID);
        return updatedRows > 0;
    }

    public List<String> getRecentWeather(Integer userID){
        List<String> recentWeather = new ArrayList<>();
        try {
            final PreparedStatement preparedStatement =
                    conection.getPreparedStatement("SELECT * FROM RecentWeather WHERE userID=? ORDER by date DESC");
            preparedStatement.setInt(1, userID);
            final ResultSet result = preparedStatement.executeQuery();
            while (result.next()){
                recentWeather.add(result.getString("cityName"));
            }
        }
        catch (SQLException ex){
            ex.printStackTrace();
        }
        return recentWeather;
    }

    public int getWeatherState(Integer userID, Long chatID){
        int state = 0;
        try {
            final PreparedStatement preparedStatement =
                    conection.getPreparedStatement("SELECT state FROM WeatherState WHERE userID=? AND chatID=?");
            preparedStatement.setInt(1, userID);
            preparedStatement.setLong(2, chatID);
            final ResultSet result = preparedStatement.executeQuery();
            if (result.next()){
                state = result.getInt("state");
            }
        }
        catch (SQLException ex){
            ex.printStackTrace();
        }
        return state;
    }

    public boolean insertWeatherState(Integer userID, Long chatID, int states){
        int updatedRows = 0;
        try {
            final PreparedStatement preparedStatement =
                    conection.getPreparedStatement("REPLACE INTO WeatherState (userID, chatID, state) VALUES (?, ?, ?)");
            preparedStatement.setInt(1, userID);
            preparedStatement.setLong(2, chatID);
            preparedStatement.setInt(3, states);
            updatedRows = preparedStatement.executeUpdate();
        }
        catch (SQLException ex){
            ex.printStackTrace();
        }
        return updatedRows > 0;
    }

    public Integer getRecentWeatherIdByCity(Integer userID, String city){
        Integer cityID = null;
        try {
            final PreparedStatement preparedStatement =
                    conection.getPreparedStatement("SELECT cityID FROM RecentWeather WHERE userID=? AND cityName=?");
            preparedStatement.setInt(1, userID);
            preparedStatement.setString(2, city);
            final ResultSet result = preparedStatement.executeQuery();
            if (result.next()){
                cityID = result.getInt("cityID");
            }
        }
        catch (SQLException ex){
            ex.printStackTrace();
        }
        return cityID;
    }

    public boolean createNewWeatherAlert(Integer userID, Integer cityID, String cityName){
        int updatedRows = 0;
        try {
            final PreparedStatement preparedStatement =
                    conection.getPreparedStatement("INSERT INTO WeatherAlert (userID, cityID, cityName) VALUES (?, ?, ?)");
            preparedStatement.setInt(1, userID);
            preparedStatement.setInt(2, cityID);
            preparedStatement.setString(3, cityName);
            updatedRows = preparedStatement.executeUpdate();
        }
        catch (SQLException ex){
            ex.printStackTrace();
        }
        return updatedRows > 0;
    }

    public List<String> getAlertCitiesNameByUser(int userID){
        List<String> alertCityNames = new ArrayList<>();
        try {
            final PreparedStatement preparedStatement =
                    conection.getPreparedStatement("SELECT cityName FROM WeatherAlert WHERE userID=?");
            preparedStatement.setInt(1, userID);
            final ResultSet result = preparedStatement.executeQuery();
            while (result.next()){
                alertCityNames.add(result.getString("cityName"));
            }
        }
        catch (SQLException ex){
            ex.printStackTrace();
        }
        return alertCityNames;
    }

    public boolean deleteAlertsForUser(Integer userID) {
        int updatedRows = 0;
        try {
            final PreparedStatement preparedStatement =
                    conection.getPreparedStatement("DELETE FROM WeatherAlert WHERE userID=?");
            preparedStatement.setInt(1, userID);
            updatedRows = preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return updatedRows > 0;
    }

    public boolean deleteAlertCity(Integer userID, String cityName){
        int updatedRows = 0;
        try {
            final PreparedStatement preparedStatement =
                    conection.getPreparedStatement("DELETE FROM WeatherAlert WHERE userID=? AND cityName=?");
            preparedStatement.setInt(1, userID);
            preparedStatement.setString(2, cityName);
            updatedRows = preparedStatement.executeUpdate();
        }
        catch (SQLException ex){
            ex.printStackTrace();
        }
        return updatedRows > 0;
    }

    public List<WeatherAlert> getAllAlerts(){
        List<WeatherAlert> alertList = new ArrayList<>();

        try {
            final PreparedStatement preparedStatement = conection.getPreparedStatement("SELECT * FROM WeatherAlert");
            final ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                WeatherAlert weatherAlert = new WeatherAlert();
                weatherAlert.setId(resultSet.getInt("id"));
                weatherAlert.setUserID(resultSet.getInt("userID"));
                weatherAlert.setCityID(resultSet.getInt("cityID"));
                alertList.add(weatherAlert);
            }
        }
        catch (SQLException ex){
            ex.printStackTrace();
        }
        return alertList;
    }
}
