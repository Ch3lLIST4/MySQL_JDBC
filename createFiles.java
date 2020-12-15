/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaapplication5;

import java.io.File;
import java.io.FileWriter;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author ASUS
 */
public class JavaApplication5 {
    
    /**
     * @param args the command line arguments
     */
    
    public static int MAX_QUERIES_IN_FILE = 50;
    
    public static void main(String[] args) {
        try {
            String folder_path = ".\\tmp\\";
            
            // create folder if not existed
            File dir = new File(folder_path);
            boolean checkDirCreated = dir.mkdir();
            if(checkDirCreated){
                System.out.println("Directory created successfully");
            }  
            
            // create file + write logs
            long file_index = 0;
            String file_name = new String();
            while(true) {
                // create log file index
                while(true) {
                    file_name = folder_path + "test_" + file_index + ".txt";

                    File fn = new File(file_name);

                    if (fn.createNewFile()) {
                        System.out.println("File created: " + file_name);
                        break;
                    } else {
                        System.out.println("File " + file_name + " already exists.");
                        file_index++;
                    }
                }

                int number_of_logs_each_retrieve = 5;
                // write log to file
                int count = 0;
                FileWriter writer = new FileWriter(file_name);
                while(true){
                    if (count >= MAX_QUERIES_IN_FILE) {
                        writer.close();
                        break;
                    } else {
                        writer.write(String.valueOf(count));
                        for (int i = 0; i<number_of_logs_each_retrieve; i++) {
                            writer.write("testing\n");
                        }
                        count = count + number_of_logs_each_retrieve;    
                    }
                }  
                TimeUnit.SECONDS.sleep(5);
            }
            
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
}
