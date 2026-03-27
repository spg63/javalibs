package javalibs;
/**
 * Copyright (javalibs.c) 2018 Sean Grimes. All rights reserved.
 * @author Sean Grimes, sean@seanpgrimes.com
 * License: MIT License
 */

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;

/**
 * Database utility functions.
 *
 * @deprecated This class is being phased out. Use the focused replacements instead:
 *   <ul>
 *     <li>{@link DBConnection} — connect / disconnect</li>
 *     <li>{@link DBQuery}      — select / selectAll / closeResultSet</li>
 *     <li>{@link DBUpdate}     — insert / delete / execute / insertAll / deleteAll</li>
 *   </ul>
 * All methods here delegate to those classes. Existing callers continue to work without
 * changes — migrate at your own pace.
 */
@Deprecated
@SuppressWarnings({"unused", "DeprecatedIsStillUsed"})
public class DBUtils {
    private static volatile DBUtils _instance;
    private final DBConnection dbc = DBConnection.get();
    private final DBQuery dbq = DBQuery.get();
    private final DBUpdate dbu = DBUpdate.get();

    private DBUtils(){}

    /** @deprecated Use {@link DBConnection}, {@link DBQuery}, or {@link DBUpdate} */
    @Deprecated
    public static DBUtils get(){
        if(_instance == null){
            synchronized(DBUtils.class){
                if(_instance == null){
                    _instance = new DBUtils();
                }
            }
        }
        return _instance;
    }

    /** @deprecated Use {@link DBConnection#connect} */
    @Deprecated
    public Connection connect(String db, String dbURL, String dbDriverClassName,
                              boolean enforceForeignKeys){
        return dbc.connect(db, dbURL, dbDriverClassName, enforceForeignKeys);
    }

    /** @deprecated Use {@link DBConnection#disconnect} */
    @Deprecated
    public void disconnect(Connection conn){
        dbc.disconnect(conn);
    }

    /** @deprecated Use {@link DBUpdate#insert(Connection, String)} */
    @Deprecated
    public void insert(Connection conn, String SQLStatement){
        dbu.insert(conn, SQLStatement);
    }

    /** @deprecated Use {@link DBUpdate#insert(String, String, String, String, boolean)} */
    @Deprecated
    public void insert(String db, String dbURL, String dbDriverClassName,
                       String SQLStatement, boolean enforceForeignKeys){
        dbu.insert(db, dbURL, dbDriverClassName, SQLStatement, enforceForeignKeys);
    }

    /** @deprecated Use {@link DBUpdate#execute} */
    @Deprecated
    public void execute(Connection conn, String SQLStatement){
        dbu.execute(conn, SQLStatement);
    }

    /** @deprecated Use {@link DBUpdate#delete(Connection, String)} */
    @Deprecated
    public void delete(Connection conn, String SQLStatement){
        dbu.delete(conn, SQLStatement);
    }

    /** @deprecated Use {@link DBUpdate#delete(String, String, String, String, boolean)} */
    @Deprecated
    public void delete(String db, String dbURL, String dbDriverClassName,
                       String SQLStatement, boolean enforceForeignKeys){
        dbu.delete(db, dbURL, dbDriverClassName, SQLStatement, enforceForeignKeys);
    }

    /** @deprecated Use {@link DBUpdate#insertAll(Connection, List)} */
    @Deprecated
    public void insertAll(Connection conn, List<String> SQLStatements){
        dbu.insertAll(conn, SQLStatements);
    }

    /** @deprecated Use {@link DBUpdate#insertAll(String, String, String, List, boolean)} */
    @Deprecated
    public void insertAll(String db, String dbURL, String dbDriverClassName,
                          List<String> SQLStatements, boolean enforceForeignKeys){
        dbu.insertAll(db, dbURL, dbDriverClassName, SQLStatements, enforceForeignKeys);
    }

    /** @deprecated Use {@link DBUpdate#deleteAll(Connection, List)} */
    @Deprecated
    public void deleteAll(Connection conn, List<String> SQLStatements){
        dbu.deleteAll(conn, SQLStatements);
    }

    /** @deprecated Use {@link DBUpdate#deleteAll(String, String, String, List, boolean)} */
    @Deprecated
    public void deleteAll(String db, String dbURL, String dbDriverClassName,
                          List<String> SQLStatements, boolean enforceForeignKeys){
        dbu.deleteAll(db, dbURL, dbDriverClassName, SQLStatements, enforceForeignKeys);
    }

    /** @deprecated Use {@link DBQuery#select} */
    @Deprecated
    public ResultSet select(Connection conn, String SQLStatement){
        return dbq.select(conn, SQLStatement);
    }

    /** @deprecated Use {@link DBQuery#closeResultSet} */
    @Deprecated
    public void closeResultSet(ResultSet rs){
        dbq.closeResultSet(rs);
    }

    /** @deprecated Use {@link DBQuery#selectAll} */
    @Deprecated
    public List<ResultSet> selectAll(Connection conn, List<String> SQLStatements){
        return dbq.selectAll(conn, SQLStatements);
    }
}
