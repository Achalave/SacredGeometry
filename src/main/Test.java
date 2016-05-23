package main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import main.probabilities.ComputationManager;
import main.probabilities.IncrementOutOfBoundsException;
import main.probabilities.NetworkedComputationManagerClient;
import main.probabilities.WorkSection;


//@author Michael Haertling
public class Test {

    public static void main(String[] args) throws IncrementOutOfBoundsException, IOException {
//        int[] start = {1, 1, 1};
////        for(int i=1; i<=12;i++){
////        WorkSection work = new WorkSection(start,i,6);
////        System.out.println(Arrays.toString(work.getEnd())+"++++");
////        }
//        WorkSection work = new WorkSection(start, 216, 6);
//        System.out.println(Arrays.toString(work.split(4)));
//        while(!work.isCompleted()){
//            work.incrementCurrent();
//            System.out.println(Arrays.toString(work.getCurrent()));
//        }
        
        run1();
        run2();

    }

    public static void run1(){
        new Thread(){
            @Override
            public void run(){
                try {
                    System.out.println("OPENING SERVER");
                    ServerSocket ss = new ServerSocket(2222);
                    Socket socket = ss.accept();
                    System.out.println("OPENED SERVER");
                    
                    
                    int[] start = {1, 1, 1};
                    WorkSection work = new WorkSection(start, 216, 6);
                    
                    ComputationManager cm = new ComputationManager(work,5);
                    cm.createNewNetworkComputationThread(socket);
                    cm.startThreads();
                } catch (IOException | IncrementOutOfBoundsException ex) {
                    Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.out.println("CLOSED SERVER");
            }
        }.start();
    }
    
    public static void run2(){
        new Thread(){
            @Override
            public void run(){
                try {
                    System.out.println("OPENING CLIENT");
                    Socket socket = new Socket("localhost",2222);
                    System.out.println("OPENED CLIENT");
                    
                    NetworkedComputationManagerClient compManager = new NetworkedComputationManagerClient(socket,5);
                    compManager.createNewComputationThread();
                    compManager.startThreads();
                } catch (IOException ex) {
                    Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.out.println("CLOSED CLIENT");
            }
        }.start();
    }
    
}
