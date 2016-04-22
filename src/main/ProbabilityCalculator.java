package main;

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

    public void calculateProbabilities(boolean shouldthread){
        if(shouldthread){
            (thread = new Thread(){
                @Override
                public void run(){
                    calculateProbabilities();
                }
            }).start();
        }else{
            calculateProbabilities();
        }
    }
    
    public void calculateProbabilities() {
        outerNumber = 0;
        didFinish = false;
        running = true;
        this.timeToCalculate = System.currentTimeMillis();
        this.combosTriedInSession = 0;
        combiner.setNumDie(numDie);
        combiner.calculateBetweenCombos();

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
            //Place results into primes
            for (Integer prime : combiner.getCurrentPrimes()) {
                int level = combiner.getLevelForPrime(prime);
                //Only increment the prime level if it is not already added
                if (!alreadyIncrementedPrimeLevels.contains(level)) {
                    alreadyIncrementedPrimeLevels.add(level);
                    Integer numOfLevel = levels.get(level);
                    if (numOfLevel == null) {
                        numOfLevel = 1;
                    } else {
                        numOfLevel++;
                    }
                    levels.put(level, numOfLevel);
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
                    if(i==0){
                        outerNumber=nums[0];
                    }
                    nums[i]++;
                    break;
                }
            }
        }

        this.timeToCalculate = System.currentTimeMillis()-timeToCalculate;
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
    
    public int getProgressTotal(){
        return this.determineRollPermutations();
    }
    
    public int getProgress(){
        return this.combosTriedInSession;
    }
    
    public long getElapsedTime(){
        if(running){
            return System.currentTimeMillis()-this.timeToCalculate;
        }else{
            return this.timeToCalculate;
        }
    }
    
    public boolean isRunning(){
        return running;
    }
    
    public int getOuterNumber(){
        return outerNumber;
    }
    
    public HashMap<Integer,Integer> getPrimesByLevel(){
        return this.levels;
    }
    
    public void cancelCalculation(){
        running = false;
    }
    
    public boolean didFinish(){
        return didFinish;
    }
}
