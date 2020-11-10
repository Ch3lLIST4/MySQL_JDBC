/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testfunction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Random;

/**
 *
 * @author ASUS
 */
public class TestFunction {

    /**
     * @param args the command line arguments
     */
    public static final int UPPERBOUND = 1_000_000;
    public static final int NUMBER_OF_CONNECTIONS = 100;
    public static final int NUMBER_OF_THREADS = 100_000;
    
    public static Connection getConnection(String ip_address, String port_number, 
            String databaseName, String username, String password) {
        Connection conn = null;
        try {
            //1. Load Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            //2. Create String
            String url = String.format("jdbc:mysql://%s:%s/%s", ip_address, port_number, databaseName);
            //3. Connect Database
            conn = DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }
    
    public static void selectUserRandom(Connection conn[]){
        try {
            Random rand = new Random();
            int upperbound = UPPERBOUND;
            int int_random = rand.nextInt(upperbound);
            
            String sql = String.format("SELECT * FROM sampledb.Users WHERE id = %d;", int_random);
            
            int int_random_conn = rand.nextInt(NUMBER_OF_CONNECTIONS);
            Connection con = conn[int_random_conn];
            
//            if (con == null) {
//                System.out.println("Null On " + Integer.toString(int_random_conn));
//            }
            Statement statement = con.prepareStatement(sql);
            ResultSet result = statement.executeQuery(sql);
//            if (rowsDeleted > 0) {
            System.out.println("A user " + int_random + " was selected");
//            }  
        } catch (Exception e) {
//            e.printStackTrace();
//            System.out.println("ERROR ON " + String(int_random_conn));
        }
    }
    
    public static void insertUserRandom(Connection conn[]){
        try {
            Random rand = new Random();
            int upperbound = UPPERBOUND;
            int int_random = rand.nextInt(upperbound);
            
            String sql = String.format("INSERT INTO sampledb.Users (id) VALUES (%d);", int_random);
            
            int int_random_conn = rand.nextInt(NUMBER_OF_CONNECTIONS);
            Connection con = conn[int_random_conn];
            
            PreparedStatement statement = con.prepareStatement(sql);
            int rowsInserted = statement.executeUpdate();
//            if (rowsDeleted > 0) {
            System.out.println("A new user " + int_random + " was inserted");
//            }  
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void deleteUserRandom(Connection conn[]) {
        try {
            Random rand = new Random();
            int upperbound = UPPERBOUND;
            int int_random = rand.nextInt(upperbound);
            
            String sql = String.format("DELETE FROM sampledb.Users WHERE id = '%d';", int_random);
            
            int int_random_conn = rand.nextInt(NUMBER_OF_CONNECTIONS);
            Connection con = conn[int_random_conn];
            
            PreparedStatement statement = con.prepareStatement(sql);
            int rowsDeleted = statement.executeUpdate();
//            if (rowsDeleted > 0) {
                System.out.println("A user " + int_random + " was deleted");
//            }
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
        
        //1. Connect to Database
        final Connection conn[] = new Connection[NUMBER_OF_CONNECTIONS];
        for (int i = 0; i < NUMBER_OF_CONNECTIONS ; i++) {
            conn[i] = getConnection(ip_address, port_number, databaseName, username, password);
        }
        
        class RunnableSelect implements Runnable{
            public void run(){
                selectUserRandom(conn);
            }
        }
//        class RunnableInsert implements Runnable{
//            public void run(){
//                insertUserRandom(conn);
//            }
//        }
//        class RunnableDelete implements Runnable{
//            public void run(){
//                deleteUserRandom(conn);
//            }
//        }
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            RunnableSelect temp = new RunnableSelect();
            Thread t_temp = new Thread(temp);
            t_temp.start();
        }
//        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
//            RunnableInsert temp = new RunnableInsert();
//            Thread t_temp = new Thread(temp);
//            t_temp.start();
//        }
//        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
//            RunnableDelete temp = new RunnableDelete();
//            Thread t_temp = new Thread(temp);
//            t_temp.start();
//        }
        
    }
    
}
