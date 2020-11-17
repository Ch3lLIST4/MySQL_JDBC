/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mysql_showstatus;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.json.JSONObject;

/**
 *
 * @author ASUS
 */
public class MySQL_ShowStatus {

    /**
     * @param args the command line arguments
     */
    
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001b[33m";
    public static final String ANSI_CYAN = "\u001b[36m";

    public static final String DATABASE_USER = "user";
    public static final String DATABASE_PASSWORD = "password";
    public static final String MYSQL_AUTO_RECONNECT = "autoReconnect";
    public static final String MYSQL_MAX_RECONNECTS = "maxReconnects";
    
    public static Connection getConnection(String ip_address, String port_number, 
            String databaseName, String username, String password) throws Exception {
        Connection conn = null;
        
        //1. Load Driver
//            Class.forName("com.mysql.jdbc.Driver");
        Class.forName("com.mysql.cj.jdbc.Driver");
        //2. Create String
        String url = String.format("jdbc:mysql://%s:%s/%s", ip_address, port_number, databaseName);
        //3. Create Properties
        java.util.Properties connProperties = new java.util.Properties();
        connProperties.put(DATABASE_USER, username);
        connProperties.put(DATABASE_PASSWORD, password);

        connProperties.put(MYSQL_AUTO_RECONNECT, "true");

        connProperties.put(MYSQL_MAX_RECONNECTS, "1");
        //4. Connect Database
        conn = DriverManager.getConnection(url, connProperties);
        
//        System.out.println(conn);
        return conn;
    }
    
    public static void searchShowStatus(Connection conn) {
        try {
            String sql = "SHOW STATUS";
            
            Statement statement = conn.createStatement();
            ResultSet result = statement.executeQuery(sql);
            
            JSONObject obj = new JSONObject();
            
            int count = 0;
            while (result.next()) {
                String variable_name = result.getString("Variable_name");
                String value = result.getString("Value");
                
                obj.put(variable_name, value);
                count++;
            }
            System.out.println(obj.toString(4));
            System.out.println(count);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        // TODO code application logic here
        String ip_address = "localhost";
        String port_number = "3306";
        String databaseName = "sampledb";
        String username = "root";
        String password = "123456";
        
        try {
            Connection conn = getConnection(ip_address, port_number, databaseName, username, password);
            searchShowStatus(conn);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
}
