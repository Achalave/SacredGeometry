package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;



//@author Michael Haertling

public class Combiner {
    
    final static int[] PRIMES = {3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97, 101, 103, 107};
    final static int NOT_ENOUGH_ROLLS_ERROR = 1;
    final static int NO_TEXT_ENTERED_ERROR = 2;
    final static int INVALID_TEXT_ERROR = 3;
    final static int DIE_VALUE_OUT_OF_BOUNDS = 4;
    
    
    private int error;
    
    private ArrayList<Integer> currentPrimes;
    private ArrayList<String> currentPrimeEq;
    
    private Set<String[]> betweenCombos;
    private int comboLength = 0;
    
    private int numDie = 2;
    
    public Combiner(){
        currentPrimes = new ArrayList<>();
        currentPrimeEq = new ArrayList<>();
    }
    
    public int getError(){
        return error;
    }
    
    public void setNumDie(int die){
        numDie = die;
    }

    public ArrayList<Integer> getCurrentPrimes() {
        return currentPrimes;
    }

    public ArrayList<String> getCurrentPrimeEq() {
        return currentPrimeEq;
    }
    
    /**
     * Use the already calculated betweenCombos with the given input to find all
     * possible primes that can be generated.
     * @param text The String list of primes.
     * @param allowD8 Whether or not d8s are allowed.
     * @return True if primes where successfully calculated.
     */
    public boolean calculatePrimes(String text, boolean allowD8) {
        //Clear the storage
        this.currentPrimeEq.clear();
        this.currentPrimes.clear();


        if (text.isEmpty()) {
            error = Combiner.NO_TEXT_ENTERED_ERROR;
            return false;
        }

        //Parse the string
        String[] rollsList = text.split("[^0-9+]");

        if (rollsList.length == 0) {
            error = Combiner.INVALID_TEXT_ERROR;
            return false;
        }

        int[] rolls = new int[rollsList.length];

        for (int i = 0; i < rollsList.length; i++) {
            rolls[i] = Integer.parseInt(rollsList[i]);
            //Make sure the roll is within range
            if(allowD8 && rolls[i]>8 || !allowD8 && rolls[i] > 6){
                error = Combiner.DIE_VALUE_OUT_OF_BOUNDS;
                return false;
            }
        }
        
        return calculatePrimes(rolls);
    }
    
    public boolean calculatePrimes(int[] rolls){
        //Make sure there is a correct number of rolls
        if (rolls.length != comboLength - 1) {
            error = Combiner.NOT_ENOUGH_ROLLS_ERROR;
            return false;
        }

        //Place the rolls through each combo and compute     
        for (String[] combo : betweenCombos) {
            this.evalExpression(combo, rolls);
        }
        
        return true;
    }
    
    
    public void evalExpression(String[] betweens, int[] values) {
        //Generate the expression
        String exp = "";
        if (betweens[0] != null) {
            exp += betweens[0];
        }

        for (int i = 0; i < values.length; i++) {
            exp += values[i];
            if (betweens[i + 1] != null) {
                exp += betweens[i + 1];
            }
        }
        //Evaluate
        int result = this.evalExpression(exp);
        if (this.isAValidPrime(result)) {
            this.currentPrimes.add(result);
            this.currentPrimeEq.add(exp);
        }
    }
    
    public int evalExpression(String foo) {
        //Calculate the possible combinations
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("JavaScript");
        try {
            return (int) ((double) engine.eval(foo));
        } catch (ScriptException ex) {
            Logger.getLogger(Combiner.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }
    
    /**
     *
     * @param num The number to check.
     * @return True if num is a prime recognized by this program.
     */
    private boolean isAValidPrime(int num) {
        return Arrays.binarySearch(PRIMES, num) >= 0;
    }
    
    
    public int getLevelForPrime(int prime){
        int index = Arrays.binarySearch(PRIMES, prime);
        return this.getLevelForPrimeIndex(index);
    }
    
    public int getLevelForPrimeIndex(int primeIndex) {
        //Level 1 - 3, 5, 7
        //Each level has 3 primes
        return ((primeIndex + 1) / 3) + 1;
    }
    
    public void calculateBetweenCombos() {
        int length = numDie+1;
        comboLength = length;
        //Consider all possibilities
        //We don't want duplicates
        Set<String[]> tempCombos = new HashSet<>();
        String[] betweens = new String[length];
        getCombinations(betweens, 1, tempCombos);
        betweenCombos = tempCombos;
        //Add in all the possible parenthisies
        betweenCombos = this.getCombinationsP(tempCombos);
    }
    
    /**
     * Calculates all the combinations that can be made on top of the given
     * combos by adding parenthesis. This is a startup method for the recursive
     * portion.
     *
     * @param set The set that holds the preliminary combinations.
     * @return The set that will ultimately hold all the combinations.
     */
    private Set<String[]> getCombinationsP(Set<String[]> set) {
        Set<String[]> output = new HashSet<>();
        //Recurse for each full combo
        for (String[] combo : set) {
            this.getCombinationsP(combo, 0, output);
        }
        return output;
    }

    /**
     * The recursive portion of the getCombinationsP function.
     *
     * @param btwns The list of betweens being worked on by this part of the
     * recursion.
     * @param index The index of the betweens being worked on by this part of
     * the recursion.
     * @param set The set that will ultimately hold all the combinations.
     */
    private void getCombinationsP(String[] btwns, int index, Set<String[]> set) {
        //No need to preserve if both the first and last are not null
        //Proof: if there is a parenthesis in both the first and last indexes
        //there is already a logically identicle btwns
        if (btwns[0] != null && btwns[btwns.length - 1] != null) {
            return;
        }

        //First preserve one with no modifications
        set.add(btwns);
        btwns = btwns.clone();

        //Exit if out of range
        if (index >= btwns.length) {
            return;
        }

        //Place the open parenthesis
        if (index + 2 < btwns.length) {
            if (btwns[index] == null) {
                btwns[index] = "(";
            } else {
                btwns[index] = btwns[index] + "(";
            }
        }

        //Do each possible close parenthesis
        for (int i = index + 2; i < btwns.length; i++) {
            //Skip if this is the first and last
            if (index == 0 && i == btwns.length - 1) {
                continue;
            }

            //Clone the array if its not the last iteration
            String[] temp = btwns;
            if (i + 1 < btwns.length) {
                temp = temp.clone();
            }

            //Do the modification possibility
            if (temp[i] == null) {
                temp[i] = ")";
            } else {
                temp[i] = ")" + temp[i];
            }

            //Recurse
            this.getCombinationsP(temp, index + 1, set);
        }
    }

    /**
     * Calculates all the combinations that can be made with the operators.
     * Don't recurse this over the first or last between.
     *
     * @param btwns The list of betweens being worked on by this part of the
     * recursion.
     * @param index The index of the betweens being worked on by this part of
     * the recursion.
     * @param set The set that will ultimately hold all the combinations.
     */
    private void getCombinations(String[] btwns, int index, Set<String[]> set) {
        String[] ops = {"+", "-", "*", "/"};

        //If at the bottom, just add to the arraylist
        if (btwns.length - 1 == index) {
            set.add(btwns);
            return;
        }

        for (int i = 0; i < ops.length; i++) {
            //Place in the opp
            btwns[index] = ops[i];
            //Recurse for each opperator
            getCombinations(btwns, index + 1, set);
            //Clone the array if its not the last one
            if (i < ops.length - 1) {
                btwns = btwns.clone();
            }
        }
    }

    

    public boolean isCombosUpToDate() {
        return this.comboLength == numDie + 1;
    }

    public int getNumDie() {
        return numDie;
    }
    
    public int getNumBetweens(){
        return this.betweenCombos.size();
    }
    
}
