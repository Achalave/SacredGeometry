package main.probabilities;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.Timer;

/**
 *
 * @author Michael
 */
public class ProbabilityPanel extends javax.swing.JPanel {

    private final static int PROGRESS_UPDATE_SPEED = 75;

    JFrame frame;
    //ProbabilityCalculator calc;
    ComputationManager compManager;
    Timer progressTimer;

    private int timeEstimate;
    private long timeToCalculate;

    /**
     * Creates new form ProbabilityPanel
     */
    public ProbabilityPanel() {
        initComponents();
        progressTimer = new Timer(PROGRESS_UPDATE_SPEED, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {

                //progressBar.setValue(calc.getCumulativeProgress());
                updateText();
                if (!compManager.isRunning()) {
                    progressTimer.stop();
                    calculationFinished();
                }
            }
        });
        progressTimer.setRepeats(true);
    }

    /**
     * Displays this panel in a new frame.
     *
     * @param c The component to center this window around.
     */
    public void displaySelf(Component c) {
        if (frame == null) {
            frame = new JFrame("Probabilities");
            frame.add(this);
            frame.pack();
        }
        frame.setLocationRelativeTo(c);
        frame.setVisible(true);
    }

    /**
     * This is called periodically by the progressTimer to display statistics as
     * the calculations are being done.
     */
    private void updateText() {
        this.jTextArea1.setText(compManager.getStatus());
    }


    private long getElapsedTime() {
        return System.currentTimeMillis() - this.timeToCalculate;
    }

    /**
     * This is called by the progressTimer when the calculations have finished.
     */
    public void calculationFinished() {
        this.beginCalculationButton.setEnabled(true);
        this.calcelCaclulationButton.setEnabled(false);
        progressBar.setValue(0);
        if (compManager.didFinish()) {
            this.jTextArea1.setText(compManager.getStatus());
        } else {
            this.jTextArea1.setText("Calculation Canceled.");
        }
    }

    
    private WorkSection generatePrimaryWorkSection(int maxRoll) throws IncrementOutOfBoundsException{
        //Create the starting point
        int[] start = new int[(int)this.dieSpinner.getValue()];
        Arrays.fill(start, 1);
                
        //Find the number of combos that will be included
        int combos = (int)Math.pow(maxRoll, start.length);
        
        WorkSection ws = null;
            //Create the main work object
            ws = new WorkSection(start,combos,maxRoll);
                
        return ws;
    }
    
    private int findMaxRoll(){
        int maxRoll = 6;
        if(this.d8Checkbox.isSelected()){
            maxRoll = 8;
        }
        return maxRoll;
    }
    
    /**
     * Begins the calculation based on the current settings.
     */
    private void beginComputation() {
        setupComputation();
        
        //Find the maxRoll value
        int maxRoll = findMaxRoll();
        
        WorkSection ws = null;
        try {
            ws = this.generatePrimaryWorkSection(maxRoll);
        } catch (IncrementOutOfBoundsException ex) {
            Logger.getLogger(ProbabilityPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        int numThreads = findNumThreads();
        
        //Create the computation manager
        beginComputation(new ComputationManager(ws,numThreads),numThreads);
        
    }
    
    private void setupComputation(){
        this.timeToCalculate = System.currentTimeMillis();
        beginCalculationButton.setEnabled(false);
        this.calcelCaclulationButton.setEnabled(true);
    }
    
    private void beginComputation(ComputationManager cm,int numThreads){
        compManager = cm;
        
        //Create the threads
        compManager.createNewComputationThreads(numThreads);
        
        progressBar.setMaximum(compManager.getProgressTotal());
        
        compManager.startThreads();
        progressTimer.start();
    }
    
    private void beginComputation(ArrayList<Socket> sockets){
        setupComputation();
        
        //Find the maxRoll value
        int maxRoll = findMaxRoll();
        
        WorkSection ws = null;
        try {
            ws = this.generatePrimaryWorkSection(maxRoll);
        } catch (IncrementOutOfBoundsException ex) {
            Logger.getLogger(ProbabilityPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        int numThreads = findNumThreads();
        
        //Create the computation manager
        ComputationManager cm = new ComputationManager(ws,(int)this.workSplitSpinner.getValue());
        
        //Add the network threads
        for(Socket s:sockets){
            try {
                cm.createNewNetworkComputationThread(s);
            } catch (IOException ex) {
                Logger.getLogger(ProbabilityPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        beginComputation(cm,numThreads);
    }
    
    private void beginComputation(Socket server){
        setupComputation();
        
        //Find the maxRoll value
        int maxRoll = findMaxRoll();
        
        WorkSection ws = null;
        try {
            ws = this.generatePrimaryWorkSection(maxRoll);
        } catch (IncrementOutOfBoundsException ex) {
            Logger.getLogger(ProbabilityPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        int numThreads = findNumThreads();
        
        //Create the networked computation manager
        NetworkedComputationManagerClient cm = null;
        try {
            cm = new NetworkedComputationManagerClient(server,numThreads);
        } catch (IOException ex) {
            Logger.getLogger(ProbabilityPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        beginComputation(cm,numThreads);
    }
    
    private int findNumThreads(){
        return (this.threadCheckbox.isSelected())?(int)numThreadsSpinner.getValue():1;
    }
    
    public void loadCalculation() {

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        dieSpinner = new javax.swing.JSpinner();
        jLabel1 = new javax.swing.JLabel();
        beginCalculationButton = new javax.swing.JButton();
        d8Checkbox = new javax.swing.JCheckBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        progressBar = new javax.swing.JProgressBar();
        calcelCaclulationButton = new javax.swing.JButton();
        outputToFileCheckbox = new javax.swing.JCheckBox();
        threadCheckbox = new javax.swing.JCheckBox();
        loadButton = new javax.swing.JButton();
        startServerButton = new javax.swing.JButton();
        startClientButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        numThreadsSpinner = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        workSplitSpinner = new javax.swing.JSpinner();

        dieSpinner.setModel(new javax.swing.SpinnerNumberModel(2, 2, null, 1));
        dieSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                dieSpinnerStateChanged(evt);
            }
        });

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText("Num Die:");

        beginCalculationButton.setText("Begin Calculation");
        beginCalculationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                beginCalculationButtonActionPerformed(evt);
            }
        });

        d8Checkbox.setText("D8 Option");
        d8Checkbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                d8CheckboxActionPerformed(evt);
            }
        });

        jTextArea1.setEditable(false);
        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        calcelCaclulationButton.setText("Cancel Calculation");
        calcelCaclulationButton.setEnabled(false);
        calcelCaclulationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                calcelCaclulationButtonActionPerformed(evt);
            }
        });

        outputToFileCheckbox.setSelected(true);
        outputToFileCheckbox.setText("Output To File");

        threadCheckbox.setSelected(true);
        threadCheckbox.setText("Multi-thread");

        loadButton.setText("Load");

        startServerButton.setText("Start Server");
        startServerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startServerButtonActionPerformed(evt);
            }
        });

        startClientButton.setText("Start Client");
        startClientButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startClientButtonActionPerformed(evt);
            }
        });

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("Num Threads:");

        numThreadsSpinner.setModel(new javax.swing.SpinnerNumberModel(2, 1, null, 1));

        jLabel3.setText("Work Split:");

        workSplitSpinner.setModel(new javax.swing.SpinnerNumberModel(2, 2, null, 1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1)
                    .addComponent(progressBar, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(calcelCaclulationButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(beginCalculationButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(dieSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE)
                            .addComponent(numThreadsSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE)
                            .addComponent(workSplitSpinner))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(d8Checkbox)
                            .addComponent(threadCheckbox)
                            .addComponent(outputToFileCheckbox))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(loadButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(startServerButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(startClientButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 184, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(loadButton)
                    .addComponent(d8Checkbox)
                    .addComponent(jLabel1)
                    .addComponent(dieSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(outputToFileCheckbox)
                    .addComponent(startServerButton)
                    .addComponent(jLabel2)
                    .addComponent(numThreadsSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(startClientButton)
                    .addComponent(threadCheckbox)
                    .addComponent(jLabel3)
                    .addComponent(workSplitSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(beginCalculationButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(calcelCaclulationButton)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void d8CheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_d8CheckboxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_d8CheckboxActionPerformed

    private void beginCalculationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_beginCalculationButtonActionPerformed
        beginComputation();
    }//GEN-LAST:event_beginCalculationButtonActionPerformed

    private void calcelCaclulationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_calcelCaclulationButtonActionPerformed
        compManager.stopComputation();
    }//GEN-LAST:event_calcelCaclulationButtonActionPerformed

    private void startServerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startServerButtonActionPerformed
        ProbabilityServerFrame server = new ProbabilityServerFrame();
        server.setLocationRelativeTo(this);
        server.setVisible(true);
        //Collect the connection to the clients
        ArrayList<Socket> sockets = server.getConnections();
        
        //Check if the splits are adequate
        //There should be at least 2 splits for each thread and roughly 4
        //for each networked thread
        int recNumSplit = (int)numThreadsSpinner.getValue()*2+sockets.size()*4;
        int currentSplit = (int)workSplitSpinner.getValue();
        if((int)this.workSplitSpinner.getValue()<recNumSplit){
            WorkSplitCorrectionFrame splitFrame = new WorkSplitCorrectionFrame(recNumSplit, currentSplit);
            splitFrame.setLocationRelativeTo(this);
            splitFrame.setVisible(true);
            currentSplit = splitFrame.getSplit();
            //Set the value
            this.workSplitSpinner.setValue(currentSplit);
        }
                
        this.beginComputation(sockets);
    }//GEN-LAST:event_startServerButtonActionPerformed

    private void startClientButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startClientButtonActionPerformed
        ProbabilityClientFrame server = new ProbabilityClientFrame();
        server.setLocationRelativeTo(this);
        server.setVisible(true);
        //Collect the connection to the server
        Socket socket = server.getConnectionToServer();
        this.beginComputation(socket);
    }//GEN-LAST:event_startClientButtonActionPerformed

    private void dieSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_dieSpinnerStateChanged
        //Adjust the number of threads default value
        //There should be a thread per die (a rough estimate)
        if((int)numThreadsSpinner.getValue()<(int)dieSpinner.getValue()){
            numThreadsSpinner.setValue(dieSpinner.getValue());
        }
        
        //Adjust the split of the work
        if((int)workSplitSpinner.getValue()<(int)numThreadsSpinner.getValue()){
            workSplitSpinner.setValue(numThreadsSpinner.getValue());
        }
    }//GEN-LAST:event_dieSpinnerStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton beginCalculationButton;
    private javax.swing.JButton calcelCaclulationButton;
    private javax.swing.JCheckBox d8Checkbox;
    private javax.swing.JSpinner dieSpinner;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JButton loadButton;
    private javax.swing.JSpinner numThreadsSpinner;
    private javax.swing.JCheckBox outputToFileCheckbox;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JButton startClientButton;
    private javax.swing.JButton startServerButton;
    private javax.swing.JCheckBox threadCheckbox;
    private javax.swing.JSpinner workSplitSpinner;
    // End of variables declaration//GEN-END:variables
}
