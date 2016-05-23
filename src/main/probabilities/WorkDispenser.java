package main.probabilities;



//@author Michael Haertling

public interface WorkDispenser {
    
    public WorkSection getNextWorkSection(ComputationThread thread);
    
}
