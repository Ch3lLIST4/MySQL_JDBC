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
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import org.json.JSONObject;
import java.util.HashMap;

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
    
    public static final int TIME_OUT = 3;
    
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
    
    public static HashMap<String, String> searchShowStatus(Connection conn, HashMap<String, String> NeededValues) {
        HashMap<String, String> NEEDED_VALUES = new HashMap<String, String>();
        ArrayList<String> arr = new ArrayList<String>();
        // Uptime
        arr.add("Uptime");
        // Threads = Threads_connected - 1
        arr.add("Threads_connected");
        // Traffic = total / TIME_OUT
        arr.add("Bytes_received");
        arr.add("Bytes_sent");
        // InnoDB Buffer Usage = Innodb_buffer_pool_pages_data / Innodb_buffer_pool_pages_total
        arr.add("Innodb_buffer_pool_pages_data");
        arr.add("Innodb_buffer_pool_pages_total");
        //^%$
        //SHOW GLOBAL STATUS WHERE Variable_name REGEXP '^Threads_connected$|^Bytes_received$|^Bytes_sent$|^Created_tmp_disk_tables$|^Handler_read_first$|^Innodb_buffer_pool_wait_free$|^Key_reads$|^Max_used_connections$|^Open_tables$|^Select_full_join$|^Slow_queries$|^Uptime$';   
        // convert to needed String
        String listString = "";
        for (String s : arr)
        {
            listString += "^" + s + "$|";
        }
        if(listString.endsWith("|")) {
            listString = listString.substring(0, listString.length() - 1);
        }
        
        try {
            String sql = String.format("SHOW GLOBAL STATUS WHERE Variable_name REGEXP '%s';", listString);
            
            Statement statement = conn.createStatement();
            ResultSet result = statement.executeQuery(sql);
            
            JSONObject obj = new JSONObject();
            
            int count = 0;
            while (result.next()) {
                boolean needed = false;
                
                String variable_name = result.getString("Variable_name");
                String value = result.getString("Value");
                
                if (variable_name.equals("Uptime")) {
                    needed = true;
                } 
                else if (variable_name.equals("Threads_connected")) {
                    variable_name = "Connections";
                    value = String.valueOf(Integer.parseInt(value) - 1);
                    needed = true;
                }
                else if (variable_name.equals("Bytes_received")) {
                    variable_name = "Traffic in";
                    System.out.println("New Value : " + Double.parseDouble(value));
                    System.out.println("Last Value : " +  Double.parseDouble(NeededValues.get("Bytes_received")));
                    System.out.println("Traffic Speed : " + (Double.parseDouble(value) - Double.parseDouble(NeededValues.get("Bytes_received"))) / 3);
                    value = String.valueOf((Double.parseDouble(value) - Double.parseDouble(NeededValues.get("Bytes_received"))) / 3);
                    NEEDED_VALUES.put("Bytes_received", value);
                    needed = true;
                }
                else if (variable_name.equals("Bytes_sent")) {
                    variable_name = "Traffic out";
                    System.out.println("New Value : " + Double.parseDouble(value));
                    System.out.println("Last Value : " +  Double.parseDouble(NeededValues.get("Bytes_sent")));
                    System.out.println("Traffic Speed : " + (Double.parseDouble(value) - Double.parseDouble(NeededValues.get("Bytes_sent"))) / 3);
                    value = String.valueOf((Double.parseDouble(value) - Double.parseDouble(NeededValues.get("Bytes_sent")))/ 3);
                    NEEDED_VALUES.put("Bytes_sent", value);
                    needed = true;
                }
                
                if (needed == true) {
                    obj.put(variable_name, value);
                    count++;
                }
            }
            System.out.println(obj.toString(4));
            System.out.println(count);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return NEEDED_VALUES;
    }
    
    public static void main(String[] args) {
        // TODO code application logic here
        String ip_address = "localhost";
        String port_number = "3306";
        String databaseName = "sampledb";
        String username = "root";
        String password = "123456";
        
        HashMap<String, String> Needed_Values = new HashMap<String, String>();
        Needed_Values.put("Bytes_received", "0");
        Needed_Values.put("Bytes_sent", "0");
        try {
            Connection conn = getConnection(ip_address, port_number, databaseName, username, password);
            
            //Loop
            while (true) {
                Needed_Values = searchShowStatus(conn, Needed_Values);
//                System.out.println(Needed_Values);
                TimeUnit.SECONDS.sleep(TIME_OUT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
