package main;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashMap;
import javax.swing.JFrame;
import javax.swing.Timer;

/**
 *
 * @author Michael
 */
public class ProbabilityPanel extends javax.swing.JPanel {

    private final static int PROGRESS_UPDATE_SPEED = 75;
    private final static int TIME_ESTIMATE_UPDATE_SPEED = 1000;

    JFrame frame;
    ProbabilityCalculator calc;
    Timer progressTimer;

    private int timeEstimate;
    private long lastTimeEstimateUpdate;
    private long timeToCalculate;

    /**
     * Creates new form ProbabilityPanel
     */
    public ProbabilityPanel() {
        initComponents();
        calc = new ProbabilityCalculator();
        progressTimer = new Timer(PROGRESS_UPDATE_SPEED, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {

                progressBar.setValue(calc.getCumulativeProgress());
                updateText();
                if (!calc.isRunning()) {
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
        String text = "";
        double et = this.getElapsedTime() / 1000.0;
        updateTimeEstimate();
        text += "Time Elapsed: " + et;
        text += "\nEstimated Time: " + timeEstimate;
        text += "\nRoll Combinations Tried: " + Arrays.toString(calc.getProgress());
        this.jTextArea1.setText(text);
    }

    /**
     * This is called by updateText to periodically update the time estimate.
     */
    private void updateTimeEstimate() {
        if (System.currentTimeMillis() - this.lastTimeEstimateUpdate > TIME_ESTIMATE_UPDATE_SPEED) {
            this.timeEstimate = (int) ((this.getElapsedTime() * calc.getProgressTotal() / 1000.0) / calc.getCumulativeProgress());
            this.lastTimeEstimateUpdate = System.currentTimeMillis();
        }
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
        if (calc.didFinish()) {
            //Set the final text
            String text = "";
            text += "Total Computation Time: " + this.getElapsedTime() / 1000.0;
            text += "\nTotal Roll Combinations: " + calc.getProgressTotal();
            text += "\n+";
            text += "\nPrimes By Level";
            text += "\n+";
            HashMap<Integer, Integer>[] levels = calc.getPrimesByLevel();
            for (int lv = 1; lv < Combiner.NUM_LEVELS; lv++) {
                //Count the possibilities for this level from each thread
                int total = 0;
                for (HashMap<Integer, Integer> level : levels) {
                    Integer temp = level.get(lv);
                    if (temp != null) {
                        total += temp;
                    }
                }

                //No need to print if there is no possibilities
                if (total == 0) {
                    continue;
                }

                text += "\nThere are " + total + " rolls for level " + lv;
                text += "\nLikelihood: " + ((double) total / calc.determineRollPermutations()) * 100 + "\n";
                this.calc.finalizeFile();
            }
            this.jTextArea1.setText(text);
        } else {
            this.jTextArea1.setText("Calculation Canceled.");
        }
    }

    /**
     * Begins the calculation based on the current settings.
     */
    private void calculate() {
        this.timeToCalculate = System.currentTimeMillis();
        beginCalculationButton.setEnabled(false);
        this.calcelCaclulationButton.setEnabled(true);
        calc.setNumDie((int) this.dieSpinner.getValue(), this.d8Checkbox.isSelected());
        progressBar.setMaximum(calc.getProgressTotal());
        calc.calculateProbabilities(this.outputToFileCheckbox.isSelected(), this.threadCheckbox.isSelected());
        progressTimer.start();
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
        loadCalculationButton = new javax.swing.JButton();
        threadCheckbox = new javax.swing.JCheckBox();

        dieSpinner.setModel(new javax.swing.SpinnerNumberModel(2, 2, null, 1));

        jLabel1.setText("Number of die:");

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

        outputToFileCheckbox.setText("Output To File");

        loadCalculationButton.setText("Load Calculation");
        loadCalculationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadCalculationButtonActionPerformed(evt);
            }
        });

        threadCheckbox.setText("Multi-thread");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addComponent(beginCalculationButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(progressBar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(calcelCaclulationButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dieSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(loadCalculationButton, javax.swing.GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE)
                        .addGap(1, 1, 1))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(d8Checkbox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(outputToFileCheckbox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(threadCheckbox)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dieSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(loadCalculationButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(d8Checkbox)
                    .addComponent(outputToFileCheckbox)
                    .addComponent(threadCheckbox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
        calculate();
    }//GEN-LAST:event_beginCalculationButtonActionPerformed

    private void calcelCaclulationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_calcelCaclulationButtonActionPerformed
        calc.cancelCalculation();
    }//GEN-LAST:event_calcelCaclulationButtonActionPerformed

    private void loadCalculationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadCalculationButtonActionPerformed
        loadCalculation();
    }//GEN-LAST:event_loadCalculationButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton beginCalculationButton;
    private javax.swing.JButton calcelCaclulationButton;
    private javax.swing.JCheckBox d8Checkbox;
    private javax.swing.JSpinner dieSpinner;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JButton loadCalculationButton;
    private javax.swing.JCheckBox outputToFileCheckbox;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JCheckBox threadCheckbox;
    // End of variables declaration//GEN-END:variables
}
