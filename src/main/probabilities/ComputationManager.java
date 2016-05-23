package main.probabilities;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import main.Combiner;

//@author Michael Haertling
public class ComputationManager implements WorkDispenser {

    protected WorkSection primaryWorkSection;
    protected List<WorkSection> workSections;
    protected List<WorkSection> finishedWorkSections;
    protected ComputationAction action;
    protected Map<ComputationThread, ArrayList<WorkSection>> workAllocation;
    protected Combiner combiner;
    protected int workSplit;
    protected boolean running = false;
    protected HashMap<WorkSection, Integer> computationsCompleted;
    protected ProbabilityData data;

    /**
     * Instantiates all the variable for this manager object.
     *
     * @param workSplit The number of sections to split the primary work section
     * into.
     */
    protected ComputationManager(int workSplit) {
        //Instantiate variables
        this.workSplit = workSplit;
        workSections = Collections.synchronizedList(new LinkedList<WorkSection>());
        finishedWorkSections = Collections.synchronizedList(new LinkedList<WorkSection>());
        workAllocation = Collections.synchronizedMap(new HashMap<ComputationThread, ArrayList<WorkSection>>());

        computationsCompleted = new HashMap<>();

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
    }

    /**
     * Instantiate this manager and split the work by the designated amount.
     *
     * @param w The work section to be computed.
     * @param workSplit The number of sections to split the primary work section
     * into.
     */
    public ComputationManager(WorkSection w, int workSplit) {
        this(workSplit);
        setPrimaryWork(w);
    }

    /**
     * Sets the number of die to be calculated and adjusts the combiner
     * accordingly.
     *
     * @param numDie The number of die to be calculated.
     */
    protected void setNumDie(int numDie) {
        if (combiner == null) {
            combiner = new Combiner();
        }
        combiner.setNumDie(numDie);
        combiner.calculateBetweenCombos();
    }

    protected final void setPrimaryWork(WorkSection w) {
        this.primaryWorkSection = w;
        setNumDie(primaryWorkSection.getNumDie());

        //Split the work sections and store them
        Collections.addAll(workSections, primaryWorkSection.split(workSplit));
    }

    @Override
    public WorkSection getNextWorkSection(ComputationThread thread) {
        if (workSections.isEmpty()) {
            return null;
        }
        WorkSection section = workSections.remove(0);
        this.workAllocation.get(thread).add(section);
        return section;
    }

    /**
     * Create a new computation thread.
     */
    public void createNewComputationThread() {
        //ComputationCompleteAction a, WorkSection work, Combiner c
        ComputationThread thread = new ComputationThread(action, this, combiner);
        workAllocation.put(thread, new ArrayList<WorkSection>());
    }

    /**
     * Create multiple computation threads.
     *
     * @param num The number of threads to create.
     */
    public void createNewComputationThreads(int num) {
        for (int i = 0; i < num; i++) {
            this.createNewComputationThread();
        }
    }

    /**
     * Creates a new networked computation thread.
     *
     * @param s The socket to create the networked thread over.
     * @throws java.io.IOException
     */
    public void createNewNetworkComputationThread(Socket s) throws IOException {
        NetworkComputationThread thread = new NetworkComputationThread(s, action, this, combiner);
        workAllocation.put(thread, new ArrayList<WorkSection>());
    }

    public void startThreads() {
        running = true;
        for (Thread t : workAllocation.keySet()) {
            t.start();
        }
    }

    /**
     * Creates a string representing the current state of the computation.
     *
     * @return The string representation.
     */
    public String getStatus() {
        String status = "";
        if (!didFinish()) {
            //Show the threads
            status += "Threads:\n";
            //Print the threads
            for (ComputationThread thread : workAllocation.keySet()) {
                status += thread + "\n";
            }
            //Print the complete works
            status += "\nComplete Sections:";
            boolean other = false;
            for (WorkSection section : this.finishedWorkSections) {
                if (!other) {
                    status += "\n";
                } else {
                    status += "\t";
                }
                other = !other;
                status += section;
            }
        } else {

            //Show the results
        }
        return status;
    }

    public boolean didFinish() {
        return finishedWorkSections.size() == workSplit;
    }

    public boolean isRunning() {
        return running;
    }

    public void setComputationsCompleted(WorkSection sec, int num) {
        this.computationsCompleted.put(sec, num);
    }

    public void incrementComputationsCompleted(WorkSection sec, int inc) {
        Integer temp = this.computationsCompleted.get(sec);
        if (temp != null) {
            inc += temp;
        }
        this.setComputationsCompleted(sec, inc);
    }

    public int getProgressTotal() {
        return this.primaryWorkSection.numCombosRemaining();
    }

    public void stopComputation() {
        running = false;
        for (ComputationThread t : workAllocation.keySet()) {
            t.stopComputing();
        }
    }

}
