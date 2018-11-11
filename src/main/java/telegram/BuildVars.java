package telegram;

/**
 * @author Nikita Zinoviev
 * @version 1.0
 * @brief Custom build vars
 */
public class BuildVars {
    public static final Boolean debug = true;

    //TODO fill this field
    public static final String OPENWEATHERAPIKEY = "4d1c143edf9fef6363b1a3cb8142417d";

    public static final String pathToLogs = "./";

    //TODO fill fields about DataBaseg
    public static final String linkDB = "jdbc:sqlite:$PROJECT_DIR$/src/main/java/telegram/database\\userdatabase";
    public static final String controllerDB = "com.mysql.cj.jdbc.Driver";
}
