package main;

import java.util.HashMap;



//@author Michael Haertling

public class CombinationData {
    
    public final HashMap<Integer,String> currentLevels;
    public final HashMap<Integer,Integer> currentPrimes;
    
    public CombinationData(){
        currentLevels = new HashMap<>();
        currentPrimes = new HashMap<>();
    }

    public HashMap<Integer, String> getCurrentLevels() {
        return currentLevels;
    }

    public HashMap<Integer, Integer> getCurrentPrimes() {
        return currentPrimes;
    }
    
    
}
