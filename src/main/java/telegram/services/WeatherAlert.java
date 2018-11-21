package telegram.services;


/**
 * @author Nikita Zinoviev
 * @version 1.0
 */
public class WeatherAlert {
    private int id;
    private int userID;
    private int cityID;

    public WeatherAlert(){}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getCityID() {
        return cityID;
    }

    public void setCityID(int cityID) {
        this.cityID = cityID;
    }
}
