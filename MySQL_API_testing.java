/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


package mysql_api_testing;


import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import org.json.JSONObject;
import java.time.Instant;
import java.time.ZoneId;


/**
 *
 * @author ASUS
 */


public class MySQL_API_testing {

    
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
    
    public static final int TIME_OUT = 5;
    public static final int RECONNECTION_TIME_OUT = 2;
    
    public static JSONObject obj_main = new JSONObject();
    
//    https://ff855c5b-7d87-4035-a588-d444d913a96d.mock.pstmn.io/data
    public static String SCHEMA = "https";
//    public static String HOSTNAME = "ff855c5b-7d87-4035-a588-d444d913a96d.mock.pstmn.io";
    public static String HOSTNAME = "ff855c5b-7d87-4035-a588-d444d913a96dWRONG.mock.pstmn.io";
    public static String PATH = "data";
    
    public static String API_URL = SCHEMA + "://" + HOSTNAME + "/" + PATH;
            
    
    public static String getCurrentTime(){
        // 2020-11-05 15:37:00.884583
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
        LocalDateTime now = LocalDateTime.now();
        return(dtf.format(now));
    }
    
    
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
    
    
    public static String checkGeneralLog(Connection conn){
        String value = "OFF";
        try {
            String sql = "show variables like 'general_log';";
        
            Statement statement = conn.createStatement();
            ResultSet result = statement.executeQuery(sql);
            
            if(result.next()){
                String var_name = result.getString("Variable_name");
                value = result.getString("Value");

                String output = "%s : %s";
                System.out.println(String.format(output, var_name, value));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }
    
    
    public static String checkLogOutput(Connection conn) {
        String value = "FILE";
        try {
            String sql = "show variables like 'log_output';";
            
            Statement statement = conn.createStatement();
            ResultSet result = statement.executeQuery(sql);
            
            if (result.next()) {
                String var_name = result.getString("Variable_name");
                value = result.getString("Value");
                
                String output = "%s : %s";
                System.out.println(String.format(output, var_name, value));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }
    
    
    public static void onGeneralLog(Connection conn) {
        try {
            String sql = "SET global general_log = 1;";
            
            Statement statement = conn.createStatement();
            statement.execute(sql);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    public static void onLogOutput(Connection conn) {
        try {
            String sql = "SET global log_output = 'TABLE';";
            
            Statement statement = conn.createStatement();
            statement.execute(sql);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    public static boolean is_Valid_Double(String numberString) {
        try
        {
          Double.parseDouble(numberString);
          return true;
        }
        catch(NumberFormatException e)
        {
          return false;
        }
    }
    
    
    // cal functions
    public static String cal_Traffic_speed_Bytes_received(String Bytes_received, String Last_Bytes_received) {
        String value = "0";
        try {
            double Traffic_speed_Bytes_received = (Double.parseDouble(Bytes_received) - Double.parseDouble(Last_Bytes_received)) / TIME_OUT;
            Traffic_speed_Bytes_received = Math.floor(Traffic_speed_Bytes_received * 100) / 100;
            value = String.valueOf(Traffic_speed_Bytes_received);
            if((!is_Valid_Double(value)) || value.equals("NaN")){
                value = "0";
            }
        } catch (Exception e) {
        }
        return value;
    }

    
    public static String cal_Traffic_speed_Bytes_sent(String Bytes_sent, String Last_Bytes_sent) {
        String value = "0";
        try {
            double Traffic_speed_Bytes_sent = (Double.parseDouble(Bytes_sent) - Double.parseDouble(Last_Bytes_sent)) / TIME_OUT;
            Traffic_speed_Bytes_sent = Math.floor(Traffic_speed_Bytes_sent * 100) / 100;
            value = String.valueOf(Traffic_speed_Bytes_sent);
            if((!is_Valid_Double(value)) || value.equals("NaN")){
                value = "0";
            }
        } catch (Exception e) {
        }
        return value;
    }

    
    public static String cal_Innodb_data_reads_per_sec(String Innodb_data_read, String Last_Innodb_data_read) {
        String value = "0";
        try {
            double Innodb_data_reads_per_sec = (Double.parseDouble(Innodb_data_read) - Double.parseDouble(Last_Innodb_data_read)) / TIME_OUT;
            Innodb_data_reads_per_sec = Math.floor(Innodb_data_reads_per_sec * 100) / 100;
            value = String.valueOf(Innodb_data_reads_per_sec);
            if((!is_Valid_Double(value)) || value.equals("NaN")){
                value = "0";
            }
        } catch (Exception e) {
        }
        return value;
    }

    
    public static String cal_Innodb_data_writes_per_sec(String Innodb_data_written, String Last_Innodb_data_written) {
        String value = "0";
        try {
            double Innodb_data_writes_per_sec = (Double.parseDouble(Innodb_data_written) - Double.parseDouble(Last_Innodb_data_written)) / TIME_OUT;
            Innodb_data_writes_per_sec = Math.floor(Innodb_data_writes_per_sec * 100) / 100;
            value = String.valueOf(Innodb_data_writes_per_sec);
            if((!is_Valid_Double(value)) || value.equals("NaN")){
                value = "0";
            }
        } catch (Exception e) {
        }
        return value;
    }
    
    
    public static String cal_Key_read_efficiency(String Key_reads, String Key_read_requests) {
        String value = "0";
        try {
            double Key_read_efficiency = (1 - (Double.parseDouble(Key_reads)/Double.parseDouble(Key_read_requests)))*100;
            Key_read_efficiency = Math.floor(Key_read_efficiency * 100) / 100;
            value = String.valueOf(Key_read_efficiency);
            if((!is_Valid_Double(value)) || value.equals("NaN")){
                value = "0";
            }
        } catch (Exception e) {
        }
        return value;
    }
    
    
    public static String cal_Key_write_efficiency(String Key_writes, String Key_write_requests) {
        String value = "0";
        try {
            double Key_write_efficiency = (1 - (Double.parseDouble(Key_writes)/Double.parseDouble(Key_write_requests)))*100;
            Key_write_efficiency = Math.floor(Key_write_efficiency * 100) / 100;
            value = String.valueOf(Key_write_efficiency);
            if((!is_Valid_Double(value)) || value.equals("NaN")){
                value = "0";
            }
        } catch (Exception e) {
        }
        return value;
    }
    
    
    public static String monitorLogTable(Connection conn, String last_exec_time/*, int count[]*/) throws Exception {
        // LAST_EXEC_TIME temp var for retrieving most current exec time 
        String LAST_EXEC_TIME = new String();
        
            String sql = String.format("SELECT * FROM mysqL.general_log WHERE event_time > '%s' ", last_exec_time)
                    + "AND NOT argument LIKE 'SELECT * FROM mysqL.general_log WHERE event_time > %' "
                    + "AND NOT argument LIKE 'SHOW GLOBAL STATUS WHERE Variable_name REGEXP %'";
            
            Statement statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet result = statement.executeQuery(sql);
            
            if (result.last()) {
                LAST_EXEC_TIME = result.getString("event_time");
                result.beforeFirst();
            }
            
            // creating var
            List<JSONObject> obj_queriesArray = new ArrayList<JSONObject>();
            
            while (result.next()) {
                JSONObject obj_query = new JSONObject();
                
                String event_time = result.getString("event_time");
                String user_host = result.getString("user_host");
                String[] user_hostArray = user_host.split("@", 2); 
                String thread_id = result.getString("thread_id");
                String server_id = result.getString("server_id");
                String command_type = result.getString("command_type");
                String argument = result.getString("argument");
                
                // adding key-value pairs to JSON obj_query -> add obj to queries ArrayList for main obj                
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"); 
                LocalDateTime event_timeLocalDateTime = LocalDateTime.parse(event_time, dtf);
                
                Instant instant = event_timeLocalDateTime.atZone(ZoneId.systemDefault()).toInstant();    
                long timeInMillis = instant.toEpochMilli(); 
                
                obj_query.put("event_time", timeInMillis);
                obj_query.put("user", user_hostArray[0].trim());
                obj_query.put("host", user_hostArray[1].trim());
                obj_query.put("thread_id", Integer.parseInt(thread_id));
                obj_query.put("server_id", Integer.parseInt(server_id));
                obj_query.put("command_type", command_type);
                obj_query.put("argument", argument);
                obj_queriesArray.add(obj_query);
                
                String output = "| %s | %s | %s | %s | %s | %s |";
                
                if (!("".equals(event_time) || event_time.isEmpty())){
                    event_time = ANSI_RED + event_time + ANSI_RESET;
                } else {}
                if (!("".equals(user_host) || user_host.isEmpty())){
                    user_host = ANSI_GREEN + user_host + ANSI_RESET;
                } else {}
                if (!("".equals(thread_id) || thread_id.isEmpty())){
                    thread_id = ANSI_CYAN + thread_id + ANSI_RESET;
                } else {}
                if (!("".equals(server_id) || server_id.isEmpty())){
                    server_id = ANSI_YELLOW + server_id + ANSI_RESET;
                } else {}
                if (!("".equals(command_type) || server_id.isEmpty())){
                    command_type = ANSI_PURPLE + command_type + ANSI_RESET;
                } else {}
                if (!("".equals(argument) || server_id.isEmpty())){
                    argument = ANSI_BLUE + argument + ANSI_RESET;
                } else {}
                
                System.out.println(String.format(output, event_time, user_host, thread_id, server_id, command_type, argument));
//                count[0]++;
            }
            
            // putting queries obj to main obj
            obj_main.put("queries", obj_queriesArray);
        
        if (LAST_EXEC_TIME == null || LAST_EXEC_TIME.isEmpty()){
            LAST_EXEC_TIME = last_exec_time;
        }
        return LAST_EXEC_TIME;
    }
    
    
    public static HashMap<String, String> searchShowStatus(Connection conn, HashMap<String, String> NeededValues) throws Exception {
        HashMap<String, String> NEEDED_VALUES = new HashMap<String, String>();
        ArrayList<String> arr = new ArrayList<String>();
        // Uptime
        arr.add("Uptime");
        // Threads_connected
        arr.add("Threads_connected");
        // Traffic = total-last_total / TIME_OUT
        arr.add("Bytes_received");
        arr.add("Bytes_sent");
        // InnoDB Buffer Usage = (Innodb_buffer_pool_pages_data / Innodb_buffer_pool_pages_total)*100
        arr.add("Innodb_buffer_pool_pages_data");
        arr.add("Innodb_buffer_pool_pages_total");
        // InnoDB Data read write speed = total-last_total / TIME_OUT
        arr.add("Innodb_data_read");
        arr.add("Innodb_data_written");
        // Key Read Efficiency
        arr.add("Key_reads");
        arr.add("Key_read_requests");
        // Key Write Efficiency
        arr.add("Key_writes");
        arr.add("Key_write_requests");
        
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
            
            // initializing vars
            double Innodb_buffer_pool_pages_data = 0;
            double Innodb_buffer_pool_pages_total = 0;
            double Key_reads = 0;
            double Key_read_requests = 0;
            double Key_writes = 0;
            double Key_write_requests = 0;
            
            // creating var
            JSONObject obj_status = new JSONObject();
            
            while (result.next()) {
                boolean needed = false;
                
                String variable_name = result.getString("Variable_name");
                String value = result.getString("Value");
                
                if (variable_name.equals("Uptime")) {
                    needed = true;
                } 
                else if (variable_name.equals("Threads_connected")) {
                    variable_name = "Connections";
                    needed = true;
                }
                else if (variable_name.equals("Bytes_received")) {
                    variable_name = "Traffic in";
                    NEEDED_VALUES.put("Bytes_received", value);
                    value = cal_Traffic_speed_Bytes_received(value, NeededValues.get("Bytes_received"));
                    needed = true;
                }
                else if (variable_name.equals("Bytes_sent")) {
                    variable_name = "Traffic out";
                    NEEDED_VALUES.put("Bytes_sent", value);
                    value = cal_Traffic_speed_Bytes_received(value, NeededValues.get("Bytes_sent"));
                    needed = true;
                }
                
                else if (variable_name.equals("Innodb_buffer_pool_pages_data")) {
                    Innodb_buffer_pool_pages_data = Double.parseDouble(value);
                }
                else if (variable_name.equals("Innodb_buffer_pool_pages_total")) {
                    Innodb_buffer_pool_pages_total = Double.parseDouble(value);
                }
                
                else if (variable_name.equals("Innodb_data_read")) {
                    variable_name = "InnoDB Reads per Second";
                    NEEDED_VALUES.put("Innodb_data_read", value);
                    value = cal_Innodb_data_reads_per_sec(value, NeededValues.get("Innodb_data_read"));
                    needed = true;
                }
                else if (variable_name.equals("Innodb_data_written")) {
                    variable_name = "InnoDB Writes per Second";
                    NEEDED_VALUES.put("Innodb_data_written", value);
                    value = cal_Innodb_data_writes_per_sec(value, NeededValues.get("Innodb_data_written"));
                    needed = true;
                }
                
                else if (variable_name.equals("Key_reads")) {
                    Key_reads = Double.parseDouble(value);
                }
                else if (variable_name.equals("Key_read_requests")) {
                    Key_read_requests = Double.parseDouble(value);
                }
                else if (variable_name.equals("Key_writes")) {
                    Key_writes = Double.parseDouble(value);
                }
                else if (variable_name.equals("Key_write_requests")) {
                    Key_write_requests = Double.parseDouble(value);
                }
                
                if (needed == true) {
                    if (variable_name.equals("Connections")){
                        obj_status.put(variable_name, Integer.parseInt(value));
                    }
                    else {
                        obj_status.put(variable_name, Double.parseDouble(value));
                    }
                }
            }
            
            // cal Innodb_buffer_usage
            double Innodb_buffer_usage_value = (Innodb_buffer_pool_pages_data / Innodb_buffer_pool_pages_total) * 100;
            Innodb_buffer_usage_value = Math.floor(Innodb_buffer_usage_value * 100) / 100; 
            String Innodb_buffer_usage = String.valueOf(Innodb_buffer_usage_value);
            obj_status.put("InnoDB Buffer Usage", Double.parseDouble(Innodb_buffer_usage));
            // cal Key_write_efficiency
            String Key_write_efficiency = cal_Key_write_efficiency(String.valueOf(Key_writes), String.valueOf(Key_write_requests));
            obj_status.put("Key Write Efficiency", Double.parseDouble(Key_write_efficiency));
            // cal Key_read_efficiency
            String Key_read_efficiency = cal_Key_read_efficiency(String.valueOf(Key_reads), String.valueOf(Key_read_requests));
            obj_status.put("Key Read Efficiency", Double.parseDouble(Key_read_efficiency));
            
            //others
            obj_status.put("Server Status", "Running");
            
            // putting status vars obj to main obj
            obj_main.put("status", obj_status);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return NEEDED_VALUES;
    }
    
    
    public static void initMenu(String ip_address, String port_number, 
            String databaseName, String username, String password) {
        System.out.println("====================");
        System.out.println("ip_address = " + ip_address);
        System.out.println("port_number = " + port_number);
        System.out.println("databaseName = " + databaseName);
        System.out.println("username = " + username);
        System.out.println("password = " + password);
        System.out.println("====================");
    }
    
    
    public static void printChangeMenu() {
        System.out.println("\nWhich one do you want to change ?");
        System.out.println("1. ip_address");
        System.out.println("2. port_number");
        System.out.println("3. databaseName");
        System.out.println("4. username");
        System.out.println("5. password");
        System.out.print("Insert the number: ");
    }
    
    
    public static boolean checkFileExisted(String log_path, String file_name) {
        boolean already_existed = false;
        try {
            String file_path = log_path + file_name + ".txt";
            
            File f = new File(file_path);
            already_existed = f.exists();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return already_existed;
    }
    
    
    public static void main(String[] args) {
        // TODO code application logic here
        String ip_address = "localhost";
        String port_number = "3306";
        String databaseName = "sampledb";
        String username = "root";
        String password = "123456";
        
        try {
            Scanner sc = new Scanner(System.in);
            
            //Load properties file - Not yet
            
            initMenu(ip_address, port_number, databaseName, username, password);
            
            System.out.print("Do you want to make any change? (Y/N) : ");
            String key_inputs = sc.nextLine().toUpperCase().trim();
            
            while (key_inputs.startsWith("Y")) {
                printChangeMenu();
                key_inputs = sc.nextLine().trim();
                
                char key_input = key_inputs.charAt(0);
                
                switch (key_input) {
                    case '1':
                        //Enter ip_address
                        System.out.print("\nEnter IP Address/Domain Name (blank for localhost): ");
                        ip_address = new String(sc.nextLine());
                        if (ip_address.equals("")) {
                            ip_address = "localhost";
                        }
                        break;
                    case '2':
                        //Enter port_number
                        System.out.print("\nEnter Port Number (blank for \'3306\'): ");
                        port_number = new String(sc.nextLine());
                        if (port_number.equals("")) {
                            port_number = "3306";
                        }
                        break;
                    case '3':
                        //Enter databaseName
                        System.out.print("\nEnter Database Name (blank for \'sampledb\'): ");
                        databaseName = new String(sc.nextLine());
                        if (databaseName.equals("")) {
                            databaseName = "sampledb";
                        }
                        break;
                    case '4':
                        //Enter username
                        System.out.print("\nEnter Username (blank for \'root\'): ");
                        username = new String(sc.nextLine());
                        if (username.equals("")) {
                            username = "root";
                        }
                        break;
                    case '5':
                        //Enter password
                        System.out.print("\nEnter Password (blank for \'123456\'): ");
                        password = new String(sc.nextLine());
                        if (password.equals("")) {
                            password = "123456";
                        }
                        break;
                }
                
                System.out.println("Successfully updated the component!\n");
                initMenu(ip_address, port_number, databaseName, username, password);
                System.out.print("Do you still want to make changes ? (Y/N):");
                key_inputs = sc.nextLine().toUpperCase().trim();
            }
            
            // monitor
            
            //connect to the Database
            Connection conn = null;
            try {
                conn = getConnection(ip_address, port_number, databaseName, username, password);
            } catch (Exception e) {
                System.out.println("Could not connect to the database. Please re-check url");
                System.exit(0);
            }
            if (conn == null) {
                System.exit(0);
            }
            
            System.out.println("");
            
            //1. Check general_log status 1 -> if 0 : Turn it on
            String general_log_status = checkGeneralLog(conn);
            switch (general_log_status) {
                case "OFF":
                    System.out.print("MySQL general_log found not turn ON yet. Do you want to turn it on ? (Y/N):");
                    key_inputs = sc.nextLine().toUpperCase().trim();
                    if (key_inputs.startsWith("Y")) {
                        onGeneralLog(conn);
                        general_log_status = checkGeneralLog(conn);
                        switch (general_log_status) {
                            case "OFF":
                                System.out.println("general_log status is still OFF. Please re-check");
                                return;
                            case "ON":
                                System.out.println("general_log status is now ON and ready to be monitored");
                                break;
                            default:
                                System.out.println("general_log status is either ON or OFF. Please re-check");
                                return;
                        }
                    } else {
                        System.out.println("Turning off. Make sure general_log is ON to use the program");
                        return;
                    }   break;
                case "ON":
                    System.out.println("MySQL general_log found ON and ready to be monitored");
                    break;
                default:
                    System.out.println("MySQL general_log found neither ON or OFF. Please re-check");
                    return;
            }
            
            //2. Check log_output status 'table' -> if 'file' : Turn it on
            String log_output_status = checkLogOutput(conn);
            switch (log_output_status) {
                case "FILE":
                    System.out.print("MySQL log_output found not switched to TABLE yet. Do you want to switch it now ? (Y/N):");
                    key_inputs = sc.nextLine().toUpperCase().trim();
                    if (key_inputs.startsWith("Y")) {
                        onLogOutput(conn);
                        log_output_status = checkLogOutput(conn);
                        switch (log_output_status) {
                            case "FILE":
                                System.out.println("log_output value is still FILE. Please re-check");
                                return;
                            case "TABLE":
                                System.out.println("log_output value is now TABLE and ready to be monitored");
                                break;
                            default:
                                System.out.println("log_output value is either FILE or TABLE. Please re-check");
                                return;
                        }
                    } else {
                        System.out.println("Turning off. Make sure log_output is switched to TABLE to use the program");
                        return;
                    }   break;
                case "TABLE":
                    System.out.println("MySQL log_output is already TABLE and ready to be monitored");
                    break;
                default:
                    System.out.println("MySQL log_output found neither FILE or TABLE. Please re-check");
                    return;
            }
            
            System.out.println("\nBegin monitoring.\n-----------------------------------------------------\n");
            
            //3. Monitor log table - server status
//            int count[] = new int[]{0};
            String last_exec_time = getCurrentTime();
            StringBuilder response = new StringBuilder();
            while(true) {
                try {
                    response = new StringBuilder();
                    
                    //3.1. monitor Log Queries
                    last_exec_time = monitorLogTable(conn, last_exec_time/*, count*/);
//                    System.out.println("Executed");
    //                System.out.println(count[0]);
                    
                    //3.2. monitor Server Status
                    HashMap<String, String> Needed_Values = new HashMap<String, String>();
        
                    Needed_Values.put("Bytes_received", "0");
                    Needed_Values.put("Bytes_sent", "0");
                    Needed_Values.put("Innodb_data_read", "0");
                    Needed_Values.put("Innodb_data_written", "0");
                    Needed_Values.put("Key_reads", "0");
                    Needed_Values.put("Key_read_requests", "0");
                    Needed_Values.put("Key_writes", "0");
                    Needed_Values.put("Key_write_requests", "0");
                    
                    Needed_Values = searchShowStatus(conn, Needed_Values);
                    
                    //3.3 pretty print JSON main obj
                    System.out.println("");
                    System.out.println(ANSI_RED + getCurrentTime() + ANSI_RESET);
                    System.out.println(obj_main.toString(4));
                    System.out.println("");
                    
                    //3.4 send mock data
                    URL url = new URL (API_URL);
                    try {
                        
                    } catch (Exception e) {
                    }
                    HttpURLConnection con = (HttpURLConnection)url.openConnection();
                    con.setRequestMethod("POST");
                    con.setRequestProperty("Content-Type", "application/json; utf-8");
                    con.setRequestProperty("Accept", "application/json");
                    con.setDoOutput(true);
                    
                    String jsonInputString = obj_main.toString(4);
                    
                    // Create the Request Body
                    try(OutputStream os = con.getOutputStream()) {
                        byte[] input = jsonInputString.getBytes("utf-8");
                        os.write(input, 0, input.length);			
                    }
                    // Read the Response from Input Stream
                    try(BufferedReader br = new BufferedReader(
                        new InputStreamReader(con.getInputStream(), "utf-8"))) {
                            String responseLine = null;
                            while ((responseLine = br.readLine()) != null) {
                                response.append(responseLine.trim());
                            }
                        System.out.println(response.toString());
                    }
                    
                    TimeUnit.SECONDS.sleep(TIME_OUT);
                } catch (SQLException e) {
                    if (conn != null) {
                        conn.close();
                    }
                    System.out.println("Lost connection to DB. Trying to reconnect..");
                    try {
                        conn = getConnection(ip_address, port_number, databaseName, username, password);
                        onGeneralLog(conn);
                        onLogOutput(conn);
                    } catch (Exception ignore) {
//                        System.out.println(ignore);
                    }
                    TimeUnit.SECONDS.sleep(RECONNECTION_TIME_OUT);
                } catch (Exception e) {
                    if (response.toString().equals("")){
                        System.out.println("Lost connection to Mock Server. Saving queries as log files for later transfer..");
                    } else {
                        System.out.println(e);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
