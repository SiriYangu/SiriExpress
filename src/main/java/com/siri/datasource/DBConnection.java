/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siri.datasource;

/**
 *
 * @author rkipkirui
 */
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rkipkirui
 */
public final class DBConnection {

    Statement stmt;
    ResultSet rs;

    Connection con = null;
    // private static Logging logger;

    public Connection getConnection() {
        try {
            con = HikariCPDataSource.getConnection();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("DB Exception :::" + ex.getCause().getLocalizedMessage());
        }
        return con;
    }

    public ResultSet query_all(final String query) {
        try {
            con = getConnection();
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
        } catch (SQLException ex) {
            System.out.println("" + ex.getCause().getLocalizedMessage());
        }
        return rs;
    }

    public int rowCount(final String query) {
        int count = 0;

        rs = query_all(query);
        try {
            while (rs.next()) {
                ++count;
            }
        } catch (SQLException ex) {
            System.out.println("" + ex.getCause().getLocalizedMessage());
        }

        return count;
    }

    public int update_db(final String query) {
        int i = 0;
        try {
            con = getConnection();
            stmt = con.createStatement();
            i = stmt.executeUpdate(query);
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("" + ex.getCause().getLocalizedMessage());
        } finally {
            try {
                stmt.close();
                con.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
                Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return i;
    }

    public void closeConn() {
        try {
            if (stmt != null) {
                stmt.close();
            }
            if (con != null) {
                con.close();
            }

        } catch (SQLException e) {
            System.out.println("" + e.getCause().getLocalizedMessage());
        }
    }
}
