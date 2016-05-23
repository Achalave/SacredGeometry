package main.probabilities;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;




//@author Michael Haertling

public class NetworkedComputationManagerClient extends ComputationManager{
    
    public static final int PORT = 2222;
    public static final int PROGRESS_UPDATE_DELAY = 1000;
    
    Connection connection;
    Semaphore connectionSem;
    Timer updateProgressTimer;
    
    
    public NetworkedComputationManagerClient(Socket s, int workSplit) throws IOException {
        super(workSplit);
        connection = new Connection(s);
        connectionSem = new Semaphore(1);
        //Create the action to be called by threads as they finish a computation
        action = new ComputationAction() {
            @Override
            public void computationCompleted(ComputationThread thread, WorkSection work) {
                finishedWorkSections.add(work);
                workAllocation.get(thread).remove(work);
                if (workSections.isEmpty()) {
                    thread.stopComputing();
                }
                if (didFinish()) {
                    stopComputation();
                }
            }

            @Override
            public void computationCanceled(ComputationThread thread, WorkSection work) {
                workAllocation.get(thread).remove(work);
                //Add that work back to the set
                work.reset();
                workSections.add(work);
            }
        };
        setupManager();
        
        //Create the progress timer
        updateProgressTimer = new Timer(PROGRESS_UPDATE_DELAY,new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                updateProgress();
            }
            
        });
        updateProgressTimer.start();
    }
    
    
    @Override
    public WorkSection getNextWorkSection(ComputationThread thread){
        try {
            connectionSem.acquire();
            connection.sendInt(NetworkComputationThread.GET_WORK);
            WorkSection ws = (WorkSection)connection.readObject();
            connectionSem.release();
            System.out.println("NCMC@ Work Section Aquired: "+ws+"\n++By Thread: "+thread);
            this.workAllocation.get(thread).add(ws);
            return(ws);
        } catch (InterruptedException ex) {
            Logger.getLogger(NetworkedComputationManagerClient.class.getName()).log(Level.SEVERE, null, ex);
        }//There is an error in the connection 
        catch (IOException ex) {
            connectionError();
        }
        return null;
    }
    
    
    
    private void setupManager(){
        try {
            connectionSem.acquire();
            connection.sendInt(NetworkComputationThread.GET_NUM_DIE);
            int numDie = connection.readInt();
            this.setNumDie(numDie);
            connectionSem.release();
        } catch (InterruptedException ex) {
            Logger.getLogger(NetworkedComputationManagerClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            connectionError();
        }
    }
    
    @Override
    public int getProgressTotal() {
        return 0;
    }
    
    private void connectionError(){
        this.stopComputation();
    }
    
    /**
     * Update the progress to the server.
     */
    private void updateProgress(){
        System.out.println("NCMC@ Updating Progress");
        try {
            connectionSem.acquire();
            
            for(ComputationThread thread:workAllocation.keySet()){
                connection.sendInt(NetworkComputationThread.PROGRESS);
                connection.sendString(thread.getNameOfCurrentWork());
                connection.sendString(thread.toString());
                System.out.println("--SENDING: "+thread.toString());
            }
            
            connectionSem.release();
        } catch (InterruptedException ex) {
            Logger.getLogger(NetworkedComputationManagerClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            connectionError();
        }
    }
    
    @Override
    public void stopComputation(){
        try {
            super.stopComputation();
            //Tell the server this client is quitting
            connectionSem.acquire();
            
            connection.sendInt(NetworkComputationThread.CLIENT_SHUTTING_DOWN);
            connection.close();
            
            connectionSem.release();
        } catch (InterruptedException ex) {
            Logger.getLogger(NetworkedComputationManagerClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            connectionError();
        }
    }
    
}
