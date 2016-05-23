package main.probabilities;

//@author Michael Haertling
import java.io.Serializable;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WorkSection implements Serializable {

    int[] start;
    int[] end;
    int[] current;
    int maxRoll;
    int numIncrements;
    int numCombos;

    public WorkSection(int[] start, int numCombosIncluded, int maxRoll) throws IncrementOutOfBoundsException {
        this.start = start;
        this.current = start.clone();
        this.maxRoll = maxRoll;
        numCombos = numCombosIncluded;
        numCombosIncluded--;
        this.numIncrements = numCombosIncluded;
        this.end = current.clone();

        //Increment end numCombosIncluded times
        incrementCurrent(numCombosIncluded);
    }

    public final void incrementCurrent(int num) throws IncrementOutOfBoundsException{
        if(num > numIncrements){
            throw new IncrementOutOfBoundsException();
        }
        
        int carry = num;
        for (int i = end.length - 1; i >= 0; i--) {
            end[i] += carry;
            carry = 0;
            if (end[i] > maxRoll) {

                carry = end[i] / (maxRoll);
                end[i] %= maxRoll;

                if (end[i] == 0) {
                    carry--;
                    end[i] = 6;
                }
            }
        }

        //Make sure there is no overflow
        if (carry != 0 && !true) {
            throw new IncrementOutOfBoundsException();
        }
    }
    
    public void incrementCurrent() {
        //Check if completed
        if (isCompleted()) {
            return;
        }

        //Decrement numIncrements
        numIncrements--;
        incrementCombo(current, maxRoll);
    }

    public static void incrementCombo(int[] combo, int maxRoll) {
        //Increment
        for (int i = combo.length - 1; i >= 0; i--) {
            if (combo[i] == maxRoll) {
                //reset this value to 1 and go to next
                combo[i] = 1;
            } else {
                //increment and move on
                combo[i]++;
                return;
            }
        }
    }

    public boolean isCompleted() {
        return this.numIncrements < 0;
    }

    public int[] getStart() {
        return start;
    }

    public int[] getEnd() {
        return end;
    }

    public int[] getCurrent() {
        return current;
    }

    public int getNumCombos() {
        return numCombos;
    }

    public int numCombosRemaining(){
        return this.numIncrements;
    }
    
    public WorkSection[] split(int num) {
        WorkSection[] sections = new WorkSection[num];
        int[] numPerSection = new int[num];
        int increments = numIncrements+1;
        int numPer = increments / num;
        Arrays.fill(numPerSection, numPer);

        //Place in what is left
        int remainder = increments - numPer * num;
        int index = 0;
        while (remainder > 0) {
            numPerSection[index++]++;
            remainder--;
            if (index == numPerSection.length) {
                index = 0;
            }
        }


        int[] s = start;
        for (int i = 0; i < sections.length; i++) {
            try {
                sections[i] = new WorkSection(s, numPerSection[i], maxRoll);
                int[] nextStart = sections[i].getEnd().clone();
                incrementCombo(nextStart, maxRoll);
                s = nextStart;
            } catch (IncrementOutOfBoundsException ex) {
                Logger.getLogger(WorkSection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return sections;
    }

    public int getMaxRoll(){
        return maxRoll;
    }
    
    public boolean usesD8(){
        return maxRoll==8;
    }
    
    public int getNumDie(){
        return start.length;
    }
    
    public void reset(){
        this.numIncrements=this.numCombos-1;
        this.current = this.start.clone();
    }
    
    public double getPercentageComplete(){
        return ((double)(numCombos-this.numCombosRemaining())/numCombos)*100;
    }
    
    @Override
    public String toString() {
        return Arrays.toString(start) + " - " + Arrays.toString(end);
    }
}
