package main;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

//@author Michael Haertling
public class ProbabilityCalculator {

    Combiner combiner;

    //int combosTriedInSession;
    int numDie;
    int rollPermutations;
    boolean d8;
    boolean didFinish;
    boolean shouldStop;

    HashMap<Integer, Integer>[] levels;
    Thread[] threads;
    int[] combosTriedInSession;

    public void setNumDie(int numDie, boolean d8) {
        didFinish = false;
        this.numDie = numDie;
        this.d8 = d8;
        this.rollPermutations = this.determineRollPermutations();
        combiner = new Combiner();
    }

    public void calculateProbabilities(final boolean outputToFile, boolean shouldthread) {
        shouldStop = false;
        final int maxRoll = this.getMaxRoll();

        if (shouldthread) {
            //Instantiate one thread for each leftmost value
            int numThreads = maxRoll;
            threads = new Thread[numThreads];
            combosTriedInSession = new int[numThreads];
            levels = new HashMap[numThreads];

            //Instantiate the level data structures
            for (int i = 0; i < levels.length; i++) {
                levels[i] = new HashMap<>();
            }

            for (int i = 0; i < threads.length; i++) {
                final int leftmost = i + 1;
                (threads[i] = new Thread() {
                    @Override
                    public void run() {
                        PrintWriter write = null;
                        if (outputToFile) {
                            String path = FileManager.getProbabilityPathIncomplete(numDie, d8);
                            path += "/@" + leftmost;
                            write = FileManager.openFileForWriting(path);
                        }
                        calculateProbabilities(leftmost - 1, write, leftmost, leftmost);
                    }
                }).start();
            }
        } else {
            int numThreads = 1;
            threads = new Thread[numThreads];
            combosTriedInSession = new int[numThreads];
            levels = new HashMap[numThreads];

            //Instantiate the level data structures
            for (int i = 0; i < levels.length; i++) {
                levels[i] = new HashMap<>();
            }

            (threads[0] = new Thread() {
                @Override
                public void run() {
                    PrintWriter write = null;
                    if (outputToFile) {
                        write = FileManager.openFileForWriting(FileManager.getProbabilityPathIncomplete(numDie, d8));
                    }
                    combosTriedInSession = new int[1];
                    calculateProbabilities(0, write, 1, maxRoll);
                }
            }).start();

        }
    }

    private void calculateProbabilities(int index, PrintWriter writer, int startLeft, int endLeft) {
        didFinish = true;
        combiner.setNumDie(numDie);
        combiner.calculateBetweenCombos();

        //Consider all possibilities for each number
        int[] nums = new int[combiner.getNumDie()];
        Arrays.fill(nums, 1);
        nums[0] = startLeft;

        int maxRoll = this.getMaxRoll();
        boolean done = false;
        while (!done && !shouldStop) {
            //Try combination
            CombinationData data = combiner.calculatePrimes(nums, this.d8);
            this.combosTriedInSession[index]++;

            if (writer != null) {
                writer.print(Arrays.toString(nums));
            }

            //Place results into primes
            HashMap<Integer, String> primes = data.getCurrentLevels();
            for (Integer level : primes.keySet()) {

                Integer numOfLevel = levels[index].get(level);
                if (numOfLevel == null) {
                    numOfLevel = 1;
                } else {
                    numOfLevel++;
                }
                levels[index].put(level, numOfLevel);

                if (writer != null) {
                    writer.print(" " + level);
                }
            }

            //Increment
            for (int i = nums.length - 1; i >= 0; i--) {
                if (nums[i] == maxRoll) {
                    //All the permutations have been tried
                    if (i == 0) {
                        done = true;
                        break;
                    }
                    nums[i] = 1;
                } else {
                    //End left has been reached
                    if (i == 0 && nums[0] == endLeft) {
                        done = true;
                        break;
                    }
                    nums[i]++;
                    break;
                }
            }

            if (writer != null && !done) {
                writer.print("\n");
            }
        }

        if (writer != null) {
            writer.close();
        }
    }

    public int getMaxRoll() {
        int maxRoll = 6;
        if (d8) {
            maxRoll = 8;
        }
        return maxRoll;
    }

    /**
     * Calculates the number of permutations possible with the current number of
     * die and whether or not d8s are being used.
     *
     * @return Total number of permutations of rolls.
     */
    public int determineRollPermutations() {
        int multiplier = 6;
        if (d8) {
            multiplier = 8;
        }
        return (int) Math.pow(multiplier, numDie);
    }

    public int getProgressTotal() {
        return this.determineRollPermutations();
    }

    public int[] getProgress() {
        return this.combosTriedInSession;
    }

    public void finalizeFile() {
        //Make sure the file has been finished
        if (!didFinish) {
            System.err.println("ProbabilityCalculator could not finalize file: unfinished");
            return;
        }

        didFinish = false;
        
        String path = FileManager.getProbabilityPathIncomplete(numDie, d8);
        String pathNew = FileManager.getProbabilityPath(numDie, d8);
        String[] files = FileManager.getFiles(path);
        //It was a silgle file
        if (files == null) {
            //Simply rename the file
            FileManager.renameFile(path, pathNew);
        } //It was multithreaded and has multiple files
        else {
            try (PrintWriter writer = FileManager.openFileForWriting(pathNew)) {
                boolean lineOne = true;
                //Combine all the files into one
                for (String file : files) {
                    try (Scanner scan = FileManager.openFileForReading(path+file)) {
                        while(scan.hasNextLine()){
                            if(lineOne){
                                lineOne = false;
                                writer.print(scan.nextLine());
                            }else{
                                writer.print("\n"+scan.nextLine());
                            }
                        }
                    }
                    FileManager.deleteFile(path+file);
                }
                FileManager.deleteFile(path);
            }
        }
    }

    public boolean isRunning() {
        if (threads == null) {
            return false;
        }
        for (Thread t : threads) {
            if (t.isAlive()) {
                return true;
            }
        }
        return false;
    }

    public HashMap<Integer, Integer>[] getPrimesByLevel() {
        return this.levels;
    }

    public void cancelCalculation() {
        shouldStop = true;
    }

    public boolean didFinish() {
        return didFinish && !isRunning();
    }

    public int getCumulativeProgress() {
        int total = 0;
        for (int i = 0; i < this.combosTriedInSession.length; i++) {
            total += this.combosTriedInSession[i];
        }
        return total;
    }
}
