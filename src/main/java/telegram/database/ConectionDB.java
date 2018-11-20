package telegram.database;


import org.telegram.telegrambots.meta.logging.BotLogger;
import telegram.BuildVars;

import java.sql.*;

/**
 * Connector to database
 * @author Nikita Zinoviev
 * @version 1.0
 */
public class ConectionDB {
    private static final String LOGTAG = "CONNECTIONDB";
    private Connection currentConnection;
    private static ConectionDB instance;

    /*
    public static ConectionDB getInstance(){
        ConectionDB currentInstance;
        if (instance == null){
            synchronized (ConectionDB.class){
                if (instance == null){
                    instance = new ConectionDB();
                }
                currentInstance = instance;
            }
        }
        else {
            currentInstance = instance;
        }
        return currentInstance;
    }

    private ConectionDB(){
        try {
            this.currentConnection = DriverManager.getConnection(BuildVars.controllerDB);
        }
        catch (SQLException | IllegalAccessError ex){
            BotLogger.error(LOGTAG, ex);
        }
    } */



    public ConectionDB(){
        this.currentConnection = openConnection();
    }

    private Connection openConnection(){
        Connection connection = null;
        try {
            Class.forName(BuildVars.controllerDB).newInstance();
            //Class.forName(BuildVars.controllerDB).getDeclaredConstructors();
            connection = DriverManager.getConnection(BuildVars.linkDB);
        }
        catch (SQLException | ClassNotFoundException | IllegalAccessException | InstantiationException ex){
            BotLogger.error(LOGTAG, ex);
        }
        return connection;
    }

   public void closeConnection(){
       try {
           this.currentConnection.close();
       }
       catch (SQLException ex){
           BotLogger.error(LOGTAG, ex);
       }
   }

   public ResultSet runSqlQuery(String query) throws SQLException{
       final Statement statement;
       statement = this.currentConnection.createStatement();
       return statement.executeQuery(query);
   }

   public Boolean executeQuery(String query) throws SQLException{
       final Statement statement = this.currentConnection.createStatement();
       return statement.execute(query);
   }

   public PreparedStatement getPreparedStatement(String query) throws SQLException{
       return this.currentConnection.prepareStatement(query);
   }

   public PreparedStatement getPreparedStatement(String query, int flags) throws SQLException{
       return this.currentConnection.prepareStatement(query, flags);
   }

    /**
     * Initialize a transaction to a database
     * @throws SQLException if initialization fails
     */
   public void initTransaction() throws SQLException{
       this.currentConnection.setAutoCommit(false);
   }

    /**
     * Finish a transaction in database and commit changer
     * @throws SQLException if a rollback fails
     */
   public void commitTransaction() throws SQLException{
       try{
           this.currentConnection.commit();
       }
       catch (SQLException ex){
           if (this.currentConnection != null){
               this.currentConnection.rollback();
           }
       }
       finally {
           this.currentConnection.setAutoCommit(false);
       }
   }
}
