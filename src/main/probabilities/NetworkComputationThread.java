package main.probabilities;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import main.Combiner;

//@author Michael Haertling
public class NetworkComputationThread extends ComputationThread {

    final static int PROGRESS = 0;
    final static int FINISHED = 1;
    final static int GET_WORK = 2;
    final static int GET_NUM_DIE = 3;
    final static int GET_RUN_STATE = 4;
    final static int CLIENT_SHUTTING_DOWN = 5;

    Connection connection;
    HashMap<String, String> workProgress;
    ArrayList<WorkSection> works;

    boolean clientCanceled = false;

    public NetworkComputationThread(Socket s, ComputationAction a, WorkDispenser wd, Combiner c) throws IOException {
        super(a, wd, c);
        connection = new Connection(s);
        workProgress = new HashMap<>();
        works = new ArrayList<>();
    }

    @Override
    public void initiateComputation() {
        //start the input loop
        boolean go = true;
        while (go) {
            try {
                switch (connection.readInt()) {
                    case PROGRESS:
                        String name = connection.readString();
                        String progress = connection.readString();
                        workProgress.put(name, progress);
                        System.out.println("NCT@ Progress Updated for " + name + " |to| " + progress);
                        break;
                    case FINISHED:
                        name = connection.readString();
                        System.out.println("NCT@ Work Completed: " + name);
                        break;
                    case GET_WORK:
                        WorkSection work = this.dispenser.getNextWorkSection(this);
                        workProgress.put(work.toString(), "Sent");
                        works.add(work);
                        System.out.println("NCT@ Sending Work to Client: " + work);
                        //totalNumCombos = work.getNumCombos();
                        connection.sendObject(work);
                        break;
                    case GET_NUM_DIE:
                        connection.sendInt(combiner.getNumDie());
                        break;
                    case CLIENT_SHUTTING_DOWN:
                        clientCanceled = true;
                        stopComputing();
                        break;
                }
            }//The connection was lost
            catch (IOException ex) {
                stopComputing();
            }
        }

    }

    @Override
    public void stopComputing() {
        super.stopComputing();
        //Tell the client to stop computing
        connection.close();
    }

    @Override
    public String toString() {
        if (!run) {
            if (clientCanceled) {
                return this.getClass().getSimpleName() + " Client Canceled Operation.";
            } else {
                return this.getClass().getSimpleName() + " Connection Closed.";
            }
        }

        String result = this.getClass().getSimpleName() + "Working On:";
        for (String name : workProgress.keySet()) {
            result += "\n@" + workProgress.get(name);
        }
        return result;
    }

}
