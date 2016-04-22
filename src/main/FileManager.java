package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

//@author Michael Haertling
public class FileManager {

    public static final String COMBINATION_PATH = "combinations/";

    public static PrintWriter openFileForWriting(String path) {

        try {
            File file = new File(path);
            
            //Check if the directories are made
            File dir = file.getParentFile();
            if(!dir.exists()){
                dir.mkdirs();
            }
            
            //Make sure the file is made
            if (!file.exists()) {
                file.createNewFile();
            }
            
            return new PrintWriter(file);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static Scanner openFileForReading(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }

        try {
            Scanner scan = new Scanner(file);
            return scan;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static String getProbabilityPath(int numDie, boolean d8) {
        String path = COMBINATION_PATH + "D";
        if (d8) {
            path += 8;
        } else {
            path += 6;
        }
        path += "-" + numDie;
        return path;
    }

}
