package main.probabilities;



//@author Michael Haertling

public interface ComputationAction {
    
    /**
     * Called by a ComputationThread when a computation has been completed.
     * @param thread The thread that is calling this method.
     * @param work
     */
    public void computationCompleted(ComputationThread thread, WorkSection work);
    
    /**
     * Called by a ComputationThread when a computation has been canceled.
     * @param thread The thread that is calling this method.
     * @param work
     */
    public void computationCanceled(ComputationThread thread, WorkSection work);
    
}
