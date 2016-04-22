package main;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

//@author Michael Haertling
public class ProbabilityCalculator {

    Combiner combiner;

    int combosTriedInSession;
    int numDie;
    int rollPermutations;
    boolean d8;
    boolean running;
    boolean didFinish;

    HashMap<Integer, Integer> levels;
    long timeToCalculate;
    int outerNumber;

    Thread thread;

    public void setNumDie(int numDie, boolean d8) {
        this.numDie = numDie;
        this.d8 = d8;
        this.rollPermutations = this.determineRollPermutations();
        combiner = new Combiner();
    }

    public void calculateProbabilities(final boolean outputToFile, boolean shouldthread) {
        if (shouldthread) {
            (thread = new Thread() {
                @Override
                public void run() {
                    calculateProbabilities(outputToFile);
                }
            }).start();
        } else {
            calculateProbabilities(outputToFile);
        }
    }

    public void calculateProbabilities(boolean outputToFile) {
        outerNumber = 0;
        didFinish = false;
        running = true;
        this.timeToCalculate = System.currentTimeMillis();
        this.combosTriedInSession = 0;
        combiner.setNumDie(numDie);
        combiner.calculateBetweenCombos();

        PrintWriter writer = null;
        if (outputToFile) {
            writer = FileManager.openFileForWriting(FileManager.getProbabilityPath(numDie, d8));
        }

        //Consider all possibilities for each number
        int[] nums = new int[combiner.getNumDie()];
        Arrays.fill(nums, 1);

        int maxRoll = 6;
        if (d8) {
            maxRoll = 8;
        }

        levels = new HashMap<>();

        boolean done = false;
        while (!done && running) {
            //Try combination
            combiner.calculatePrimes(nums);
            this.combosTriedInSession++;
            ArrayList<Integer> alreadyIncrementedPrimeLevels = new ArrayList<>();

            if (outputToFile) {
                writer.print(Arrays.toString(nums));
            }

            //Place results into primes
            HashMap<Integer, String> primes = combiner.getCurrentLevels();
            for (Integer level : primes.keySet()) {

                Integer numOfLevel = levels.get(level);
                if (numOfLevel == null) {
                    numOfLevel = 1;
                } else {
                    numOfLevel++;
                }
                levels.put(level, numOfLevel);

                if (outputToFile) {
                    writer.print(" " + level);
                }
            }

            //Increment
            for (int i = nums.length - 1; i >= 0; i--) {
                if (nums[i] == maxRoll) {
                    //All the permutations have been tried
                    if (i == 0) {
                        didFinish = true;
                        done = true;
                        break;
                    }
                    nums[i] = 1;
                } else {
                    if (i == 0) {
                        outerNumber = nums[0];
                    }
                    nums[i]++;
                    break;
                }
            }

            if (outputToFile && !done) {
                writer.print("\n");
            }
        }

        if (outputToFile) {
            writer.close();
        }

        this.timeToCalculate = System.currentTimeMillis() - timeToCalculate;
        running = false;
        thread = null;
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

    public double getCompletionPercentage() {
        return ((double) combosTriedInSession / this.rollPermutations) * 100;
    }

    public int getProgressTotal() {
        return this.determineRollPermutations();
    }

    public int getProgress() {
        return this.combosTriedInSession;
    }

    public long getElapsedTime() {
        if (running) {
            return System.currentTimeMillis() - this.timeToCalculate;
        } else {
            return this.timeToCalculate;
        }
    }

    public boolean isRunning() {
        return running;
    }

    public int getOuterNumber() {
        return outerNumber;
    }

    public HashMap<Integer, Integer> getPrimesByLevel() {
        return this.levels;
    }

    public void cancelCalculation() {
        running = false;
    }

    public boolean didFinish() {
        return didFinish;
    }
}
