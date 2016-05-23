package main.probabilities;

//@author Michael Haertling
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import main.CombinationData;
import main.Combiner;
import main.FileManager;

public class ComputationThread extends Thread {

    //public final int COMPUTATION_COMPLETE = 1;
    //public final int COMPUTATION_CANCELED = 0;
    protected ComputationAction act;
    protected ProbabilityData data;
    protected boolean run = true;
    protected Combiner combiner;
    protected WorkDispenser dispenser;
    protected int combosCompleted = 0;
    protected int totalCombos = 0;
    protected WorkSection finishedWork;

    public ComputationThread(ComputationAction a, WorkDispenser wd, Combiner c) {
        act = a;
        combiner = c;
        dispenser = wd;
    }

    @Override
    public void run() {
        while (run) {
            //Run the computation
            initiateComputation();
            //Get new data and notify 
            act.computationCompleted(this, finishedWork);
        }
    }

    public void initiateComputation() {
        //Create a new data object
        data = new ProbabilityData();
        WorkSection work = (dispenser.getNextWorkSection(this));

        //Make sure some work is gotten
        if (work == null) {
            return;
        }

        finishedWork = work;

        //Get the writer to enable writing to the file
        PrintWriter write = FileManager.openFileForWriting(FileManager.getProbabilityPathIncomplete(work));

        //Begin the computation loop
        boolean done = false;
        while (!work.isCompleted()) {
            //Get the rolls
            int[] rolls = work.getCurrent();
            
            //Analyze the roll set
            analyzedRollSet(rolls, combiner, work.usesD8(), write, data);
            
            //Increment the roll to be tested next
            work.incrementCurrent();

            //Check if the work has been completed
            if (work.isCompleted()) {
                done = true;
            }

            //If this computation is being saved to a file and it isnt the last,
            //then print a newline to separate the next line from this one
            if (write != null && !done) {
                write.print("\n");
            }
        }

        //Close the writer before finishing this computation
        if (write != null) {
            write.close();
        }

    }

    public void stopComputing() {
        run = false;
    }

    /**
     * Finds the possible primes for this roll set and prints the data to the file
     * and records the total results to the data object.
     * @param rolls The set of rolls to be analyzed.
     * @param combiner The Combiner object to use to do the calculations.
     * @param d8 An indication as to whether d8s are being used.
     * @param write The PrintWriter object to write the line for this roll set to.
     * @param data The data collection object storing the statistics for this computation.
     */
    protected void analyzedRollSet(int[] rolls, Combiner combiner, boolean d8, PrintWriter write, ProbabilityData data) {
        //Find the primes
        CombinationData comboData = combiner.calculatePrimes(rolls,d8);

        //Print the rolls to the file
        if (write != null) {
            write.print(Arrays.toString(rolls));
        }

        //Iterate through the result data
        HashMap<Integer, String> levels = comboData.getCurrentLevels();
        for (Integer level : levels.keySet()) {
            data.incrementLevel(level);
            if (write != null) {
                write.print(" " + level);
            }
        }

    }
    
    public String getNameOfCurrentWork(){
        if(finishedWork==null){
            return "No Current Work";
        }
        return finishedWork.toString();
    }
    
    @Override
    public String toString() {
        if(finishedWork==null){
            return this.getClass().getSimpleName() + ":\nIdle";
        }
        return this.getClass().getSimpleName() + ":\nWorking On " + finishedWork + " " + (int) finishedWork.getPercentageComplete() + "%";
    }

}
