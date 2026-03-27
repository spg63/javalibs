package javalibs;
/**
 * Copyright (javalibs.c) 2018 Sean Grimes. All rights reserved.
 * @author Sean Grimes, sean@seanpgrimes.com
 * License: MIT License
 */

import org.sqlite.SQLiteConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages database connections. Handles opening and closing connections to a SQLite
 * database. Prefer this over DBUtils.connect / DBUtils.disconnect for new code.
 */
public class DBConnection {
    private static volatile DBConnection _instance;
    private final TSL log = TSL.get();

    private DBConnection(){}

    public static DBConnection get(){
        if(_instance == null){
            synchronized(DBConnection.class){
                if(_instance == null){
                    _instance = new DBConnection();
                }
            }
        }
        return _instance;
    }

    /**
     * Open a connection to the database.
     * @param db Path to the DB file
     * @param dbURL The JDBC URL prefix (e.g. "jdbc:sqlite:")
     * @param dbDriverClassName The driver class name (e.g. "org.sqlite.JDBC")
     * @param enforceForeignKeys True to enforce foreign key constraints
     * @return An open Connection
     */
    public Connection connect(String db, String dbURL, String dbDriverClassName,
                              boolean enforceForeignKeys){
        try{
            Class.forName(dbDriverClassName);
            SQLiteConfig config = new SQLiteConfig();
            if(enforceForeignKeys)
                config.enforceForeignKeys(true);
            return DriverManager.getConnection(dbURL + db, config.toProperties());
        }
        catch(SQLException | ClassNotFoundException e){
            log.exception(e);
            throw new RuntimeException("DBConnection.connect failure");
        }
    }

    /**
     * Close a database connection.
     * @param conn The connection to close
     */
    public void disconnect(Connection conn){
        try{
            conn.close();
        }
        catch(SQLException e){
            log.exception(e);
            throw new RuntimeException("DBConnection.disconnect failure");
        }
    }
}
