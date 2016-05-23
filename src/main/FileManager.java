package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import main.probabilities.WorkSection;

//@author Michael Haertling
public class FileManager {

    public static final String COMBINATION_PATH = "data/combinations/";
    public static final String METAMAGIC_PATH = "data/metamagic/";
    public static final String SPELLS_PATH = "data/spells/";

    public static PrintWriter openFileForWriting(String path) {
        try {
            File file = new File(path);

            //Check if the directories are made
            File dir = file.getParentFile();
            if (!dir.exists()) {
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
        String path = COMBINATION_PATH + "@D";
        if (d8) {
            path += 8;
        } else {
            path += 6;
        }
        path += "%"+numDie+"%";
        return path;
    }

    public static String getProbabilityPathIncomplete(WorkSection work){
        return getProbabilityPath(work.getNumDie(),work.usesD8())+" "+work;
    }
    
    public static boolean makeDirectories(String path){
        File file = new File(path);
        return file.mkdirs();
    }
    
    public static boolean deleteFile(String path) {
        File file = new File(path);
        return file.delete();
    }
    
    public static String[] getFiles(String path){
        File file = new File(path);
        if(file.isFile() || !file.exists()){
            return null;
        }
        return file.list();
    }
    
    public static void renameFile(String path, String pathnew){
        File file = new File(path);
        file.renameTo(new File(pathnew));
    }
}
