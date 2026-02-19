package com.swe.core.utils.database.tables;

import java.sql.Connection;

import com.swe.core.utils.database.DBConnect;

abstract public class Table  {
    public Connection conn;

    public Table() {
        this.conn = DBConnect.getInstance().conn;
    }

    // unimplemented class
}
