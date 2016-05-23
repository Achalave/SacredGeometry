package main.probabilities;

import java.util.HashMap;

//@author Michael Haertling
public class ProbabilityData {

    private int combosTried;
    private final HashMap<Integer, Integer> levels;

    public ProbabilityData() {
        combosTried = 0;
        levels = new HashMap<>();
    }

    public int incrementLevel(int level) {
        return incrementLevel(level,1);
    }
    
    public int incrementLevel(int level, int num){
        Integer amt = levels.get(level);
        if (amt == null) {
            amt = num;
        }else{
            amt+=num;
        }
        levels.put(level, amt);
        return amt;
    }
    
    public void incrementCombosTried(){
        combosTried++;
    }
    
    public void mergeIn(ProbabilityData data){
        //Get all the level data
        HashMap<Integer, Integer> l = data.levels;
        for(Integer level:l.keySet()){
            incrementLevel(level,l.get(level));
        }
        //Get the combos tried
        combosTried+=data.combosTried;
    }

    public int getCombosTried() {
        return combosTried;
    }

    public HashMap<Integer, Integer> getLevels() {
        return levels;
    }
    
    
}
