package com.swe.core.utils.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.swe.core.utils.env.EnVars;
import com.swe.core.utils.env.MyEnv;

public class DBConnect {
    private static DBConnect instance;
    public Connection conn;

    @SuppressWarnings("UseSpecificCatch")
    private DBConnect() throws Exception {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver")
                .getDeclaredConstructor()
                .newInstance();
        } catch (Exception ex) {
            throw ex;
        }

        try {
            this.conn = DriverManager.getConnection(
                String.format(
                    "jdbc:mysql://localhost:%s/%s",
                    MyEnv.getVariable(EnVars.DB_PORT),
                    MyEnv.getVariable(EnVars.DB_NAME)    
                ),
                MyEnv.getVariable(EnVars.DB_USERNAME),
                MyEnv.getVariable(EnVars.DB_PASSWORD)
            );
        } catch (SQLException ex) {
            System.out.println(String.format(
                "SQLException: %s\nSQLState: %s\nVendorError: %d",
                ex.getMessage(),
                ex.getSQLState(),
                ex.getErrorCode()
            ));
            throw ex;
        }
    }

    public static DBConnect getInstance() {
        if (DBConnect.instance == null)
            try {
                DBConnect.instance = new DBConnect();
            } catch (Exception ex) {
                System.out.println("[Error connecting to DB]\n" + ex);
            }

        return DBConnect.instance;
    }

}