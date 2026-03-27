package javalibs;
/**
 * Copyright (javalibs.c) 2018 Sean Grimes. All rights reserved.
 * @author Sean Grimes, sean@seanpgrimes.com
 * License: MIT License
 */

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles database query (SELECT) operations. Prefer this over DBUtils.select /
 * DBUtils.selectAll / DBUtils.closeResultSet for new code.
 *
 * NOTE: The connection must remain open while you require access to a ResultSet.
 * NOTE: All failures throw an unchecked RuntimeException.
 */
public class DBQuery {
    private static volatile DBQuery _instance;
    private final TSL log = TSL.get();

    private DBQuery(){}

    public static DBQuery get(){
        if(_instance == null){
            synchronized(DBQuery.class){
                if(_instance == null){
                    _instance = new DBQuery();
                }
            }
        }
        return _instance;
    }

    /**
     * Execute a SELECT statement and return the ResultSet.
     * NOTE: The underlying Statement closes automatically when the ResultSet is closed.
     * @param conn An open connection to the DB
     * @param SQLStatement The SELECT statement
     * @return The ResultSet
     */
    public ResultSet select(Connection conn, String SQLStatement){
        try{
            Statement stmt = conn.createStatement();
            stmt.setFetchSize(1000);
            // Automatically closes the Statement when its ResultSet is closed
            stmt.closeOnCompletion();
            return stmt.executeQuery(SQLStatement);
        }
        catch(SQLException e){
            log.exception(e);
            throw new RuntimeException("DBQuery.select failure");
        }
    }

    /**
     * Execute multiple SELECT statements and return all ResultSets.
     * NOTE: The connection must remain open while you require access to the ResultSets.
     * @param conn An open connection to the DB
     * @param SQLStatements The SELECT statements
     * @return A list of ResultSets
     */
    public List<ResultSet> selectAll(Connection conn, List<String> SQLStatements){
        log.warn("DBQuery.selectAll called; unoptimized.");
        List<ResultSet> results = new ArrayList<>();
        for(String sql : SQLStatements)
            results.add(select(conn, sql));
        return results;
    }

    /**
     * Close a ResultSet, handling the checked exception.
     * @param rs The ResultSet to close
     */
    public void closeResultSet(ResultSet rs){
        try{
            rs.close();
        }
        catch(SQLException e){
            log.exception(e);
            throw new RuntimeException("DBQuery.closeResultSet failure");
        }
    }
}
