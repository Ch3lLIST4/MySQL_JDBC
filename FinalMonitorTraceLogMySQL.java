/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package finalmonitortracelogmysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author ASUS
 */
public class FinalMonitorTraceLogMySQL {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001b[33m";
    public static final String ANSI_CYAN = "\u001b[36m";
    
    public static String getCurrentTime(){
        // 2020-11-05 15:37:00.884583
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
        LocalDateTime now = LocalDateTime.now();
        return(dtf.format(now));
    }
    
    public static Connection getConnection(String ip_address, String port_number, 
            String databaseName, String username, String password) {
        Connection conn = null;
        try {
            //1. Load Driver
//            Class.forName("com.mysql.jdbc.Driver");
            Class.forName("com.mysql.cj.jdbc.Driver");
            //2. Create String
            String url = String.format("jdbc:mysql://%s:%s/%s", ip_address, port_number, databaseName);
            //3. Connect Database
            conn = DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            e.printStackTrace();
//            System.out.println("Something is wrong in your connection string!");
//            System.out.println("Please restart the program and re-enter the components!");
        }
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
    
    public static String monitorLogTable(Connection conn, String last_exec_time/*, int count[]*/) {
        // LAST_EXEC_TIME temp var for retrieving most current exec time 
        String LAST_EXEC_TIME = new String();
        
        try {
            String sql = String.format("SELECT * FROM mysqL.general_log WHERE event_time > '%s' ", last_exec_time)
                    + "AND NOT argument LIKE 'SELECT * FROM mysqL.general_log WHERE event_time >%';";
            
            Statement statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet result = statement.executeQuery(sql);
            
            if (result.last()) {
                LAST_EXEC_TIME = result.getString("event_time");
                result.beforeFirst();
            }
            while (result.next()) {
                String event_time = result.getString("event_time");
                String user_host = result.getString("user_host");
                String thread_id = result.getString("thread_id");
                String server_id = result.getString("server_id");
                String command_type = result.getString("command_type");
                String argument = result.getString("argument");
                
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        if (LAST_EXEC_TIME == null || LAST_EXEC_TIME.isEmpty()){
            LAST_EXEC_TIME = last_exec_time;
        }
        return LAST_EXEC_TIME;
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
            Connection conn = getConnection(ip_address, port_number, databaseName, username, password);
            
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
                    }   break;
                case "TABLE":
                    System.out.println("MySQL log_output is already TABLE and ready to be monitored");
                    break;
                default:
                    System.out.println("MySQL log_output found neither FILE or TABLE. Please re-check");
                    return;
            }
            
            System.out.println("\nBegin monitoring.\n-----------------------------------------------------\n");
            
            //3. Monitor log table
//            int count[] = new int[]{0};
            String last_exec_time = getCurrentTime();
            while(true) {
                last_exec_time = monitorLogTable(conn, last_exec_time/*, count*/);
//                System.out.println(count[0]);
                TimeUnit.SECONDS.sleep(5);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
