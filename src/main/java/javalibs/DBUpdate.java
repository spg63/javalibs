package javalibs;
/**
 * Copyright (javalibs.c) 2018 Sean Grimes. All rights reserved.
 * @author Sean Grimes, sean@seanpgrimes.com
 * License: MIT License
 */

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Handles database write operations: INSERT, DELETE, UPDATE, and batch variants.
 * Prefer this over DBUtils insert / delete / execute / insertAll / deleteAll for new code.
 *
 * NOTE: All failures throw an unchecked RuntimeException.
 */
@SuppressWarnings("ThrowFromFinallyBlock")
public class DBUpdate {
    private static volatile DBUpdate _instance;
    private static final int QUERY_TIMEOUT = 240;
    private final TSL log = TSL.get();
    private final DBConnection dbc = DBConnection.get();

    private DBUpdate(){}

    public static DBUpdate get(){
        if(_instance == null){
            synchronized(DBUpdate.class){
                if(_instance == null){
                    _instance = new DBUpdate();
                }
            }
        }
        return _instance;
    }

    /**
     * Execute a single INSERT, DELETE, or UPDATE statement.
     * NOTE: The connection will not be closed for you.
     * @param conn An open connection to the DB
     * @param SQLStatement The statement to execute
     */
    public void insert(Connection conn, String SQLStatement){
        executeGenericUpdate(conn, SQLStatement);
    }

    /**
     * Execute a single INSERT statement, managing the connection internally.
     * @param db Path to the DB
     * @param dbURL The JDBC URL prefix
     * @param dbDriverClassName The driver class name
     * @param SQLStatement The statement to execute
     * @param enforceForeignKeys True to enforce foreign key constraints
     */
    public void insert(String db, String dbURL, String dbDriverClassName,
                       String SQLStatement, boolean enforceForeignKeys){
        executeGenericUpdate(db, dbURL, dbDriverClassName, SQLStatement, enforceForeignKeys);
    }

    /**
     * Execute a single DELETE statement.
     * NOTE: The connection will not be closed for you.
     * @param conn An open connection to the DB
     * @param SQLStatement The statement to execute
     */
    public void delete(Connection conn, String SQLStatement){
        executeGenericUpdate(conn, SQLStatement);
    }

    /**
     * Execute a single DELETE statement, managing the connection internally.
     * @param db Path to the DB
     * @param dbURL The JDBC URL prefix
     * @param dbDriverClassName The driver class name
     * @param SQLStatement The statement to execute
     * @param enforceForeignKeys True to enforce foreign key constraints
     */
    public void delete(String db, String dbURL, String dbDriverClassName,
                       String SQLStatement, boolean enforceForeignKeys){
        executeGenericUpdate(db, dbURL, dbDriverClassName, SQLStatement, enforceForeignKeys);
    }

    /**
     * Execute a generic DB statement not related to insert, delete, or update.
     * NOTE: The connection will not be closed for you.
     * @param conn An open connection to the DB
     * @param SQLStatement The statement to execute
     */
    public void execute(Connection conn, String SQLStatement){
        executeGenericUpdate(conn, SQLStatement);
    }

    /**
     * Execute a batch INSERT. There is no batch size limit — caller is responsible
     * for splitting into manageable chunks.
     * NOTE: The connection will not be closed for you.
     * @param conn An open connection to the DB
     * @param SQLStatements The statements to execute
     */
    public void insertAll(Connection conn, List<String> SQLStatements){
        executeBatchUpdate(conn, SQLStatements);
    }

    /**
     * Execute a batch INSERT, managing the connection internally.
     * @param db Path to the DB
     * @param dbURL The JDBC URL prefix
     * @param dbDriverClassName The driver class name
     * @param SQLStatements The statements to execute
     * @param enforceForeignKeys True to enforce foreign key constraints
     */
    public void insertAll(String db, String dbURL, String dbDriverClassName,
                          List<String> SQLStatements, boolean enforceForeignKeys){
        executeBatchUpdate(db, dbURL, dbDriverClassName, SQLStatements, enforceForeignKeys);
    }

    /**
     * Execute a batch DELETE. There is no batch size limit — caller is responsible
     * for splitting into manageable chunks.
     * NOTE: The connection will not be closed for you.
     * @param conn An open connection to the DB
     * @param SQLStatements The statements to execute
     */
    public void deleteAll(Connection conn, List<String> SQLStatements){
        executeBatchUpdate(conn, SQLStatements);
    }

    /**
     * Execute a batch DELETE, managing the connection internally.
     * @param db Path to the DB
     * @param dbURL The JDBC URL prefix
     * @param dbDriverClassName The driver class name
     * @param SQLStatements The statements to execute
     * @param enforceForeignKeys True to enforce foreign key constraints
     */
    public void deleteAll(String db, String dbURL, String dbDriverClassName,
                          List<String> SQLStatements, boolean enforceForeignKeys){
        executeBatchUpdate(db, dbURL, dbDriverClassName, SQLStatements, enforceForeignKeys);
    }

    private void executeGenericUpdate(String db, String dbURL, String dbDriverClassName,
                                      String SQLStatement, boolean enforceForeignKeys){
        Connection conn = dbc.connect(db, dbURL, dbDriverClassName, enforceForeignKeys);
        try{
            executeGenericUpdate(conn, SQLStatement);
        }
        finally{
            try{
                conn.close();
            }
            catch(SQLException e){
                log.exception(e);
                throw new RuntimeException("DBUpdate.executeGenericUpdate failure");
            }
        }
    }

    private void executeGenericUpdate(Connection conn, String SQLStatement){
        try(Statement stmt = conn.createStatement()){
            stmt.setQueryTimeout(QUERY_TIMEOUT);
            stmt.executeUpdate(SQLStatement);
        }
        catch(SQLException e){
            log.exception(e);
            throw new RuntimeException(
                    "DBUpdate.executeGenericUpdate failure for: " + SQLStatement
            );
        }
    }

    private void executeBatchUpdate(String db, String dbURL, String dbDriverClassName,
                                    List<String> SQLStatements, boolean enforceForeignKeys){
        Connection conn = dbc.connect(db, dbURL, dbDriverClassName, enforceForeignKeys);
        try{
            executeBatchUpdate(conn, SQLStatements);
        }
        finally{
            try{
                conn.close();
            }
            catch(SQLException e){
                log.exception(e);
                throw new RuntimeException("DBUpdate.executeBatchUpdate failure");
            }
        }
    }

    private void executeBatchUpdate(Connection conn, List<String> SQLStatements){
        try{
            conn.setAutoCommit(false);
        }
        catch(SQLException e){
            log.exception(e);
            throw new RuntimeException("DBUpdate.executeBatchUpdate failure");
        }

        try(Statement stmt = conn.createStatement()){
            for(String SQLStatement : SQLStatements){
                stmt.addBatch(SQLStatement);
            }
            stmt.executeBatch();
            conn.commit();
        }
        catch(SQLException e){
            log.exception(e);
            try{
                conn.rollback();
            }
            catch(SQLException ex){
                log.exception(ex);
                throw new RuntimeException("DBUpdate.executeBatchUpdate failure");
            }
            throw new RuntimeException("DBUpdate.executeBatchUpdate failure");
        }
        finally{
            try{
                if(!conn.getAutoCommit())
                    conn.setAutoCommit(true);
            }
            catch(SQLException e){
                log.exception(e);
                throw new RuntimeException("DBUpdate.executeBatchUpdate failure");
            }
        }
    }
}
