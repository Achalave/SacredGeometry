/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.Timer;

/**
 *
 * @author Michael
 */
public class MainPanel extends javax.swing.JPanel {

    
    final static int TIME_BEFORE_REFRESH = 1200;
    final static int ERROR_DISPLAY_TIME = 1200;

    
    Timer comboRecalcTimer;
    Timer rollsError;

    String tempRollsStorage;
    String tempSpellsStorage;
    String tempMetaStorage;

    Combiner combiner;

    HashMap<String, Spell> spells;
    HashMap<String, Metamagic> metamagic;
    ArrayList<Metamagic> selectedMeta;

    DefaultListModel spellListModel;
    
    String rolls = "";
    
    AddSpellPanel addSpell;
    AddMetaPanel addMeta;
    
    public static void main(String[] args){
        JFrame frame = new JFrame();
        MainPanel mp = new MainPanel();
        frame.add(mp);
        frame.setSize(mp.getPreferredSize());
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    
    /**
     * Creates new form Test
     */
    public MainPanel() {
        initComponents();

        combiner = new Combiner();
        combiner.calculateBetweenCombos();
        
        this.metamagicPanel.setLayout(new BoxLayout(metamagicPanel, BoxLayout.Y_AXIS));
        spellListModel = new DefaultListModel();
        this.spellsList.setModel(spellListModel);

        spells = new HashMap<>();
        metamagic = new HashMap<>();
        selectedMeta = new ArrayList<>();
        
        loadSpells();
        loadMetamagic();

        addSpell = new AddSpellPanel();
        addMeta = new AddMetaPanel();
        
        this.comboRecalcTimer = new Timer(TIME_BEFORE_REFRESH, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                combiner.calculateBetweenCombos();
            }
        });
        this.comboRecalcTimer.setRepeats(false);

        this.rollsError = new Timer(ERROR_DISPLAY_TIME, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                rollTextField.setText(tempRollsStorage);
                rollTextField.setForeground(Color.black);
            }
        });
        this.rollsError.setRepeats(false);

    }

    private void loadSpells() {

    }

    private void loadMetamagic() {

    }

    private void setupForRollsError() {
        //Store the previous text
        this.tempRollsStorage = rollTextField.getText();
        rollTextField.setForeground(Color.red);
        //Place new text
        rollsError.start();
    }

    private void displayNotEnoughRollsError() {
        this.setupForRollsError();
        this.rollTextField.setText("Not Enough Roll Values!");
    }
    
    private void displayInvalidRollTextError(){
        this.setupForRollsError();
        this.rollTextField.setText("Invalid Text!");
    }
    
    private void displayNoRollTextEntered(){
        this.setupForRollsError();
        this.rollTextField.setText("Invalid Text!");
    }    

    public void castSpell(){
        //Find the primes that can be created
        if(!combiner.calculatePrimes(rolls)){
            //If it failded to calculate the primes check the error message
            switch(combiner.getError()){
                case Combiner.INVALID_TEXT_ERROR:
                    this.displayInvalidRollTextError();
                    return;
                case Combiner.NOT_ENOUGH_ROLLS_ERROR:
                    this.displayNotEnoughRollsError();
                    return;
                case Combiner.NO_TEXT_ENTERED_ERROR:
                    this.displayNoRollTextEntered();
                    return;
            }
        }
        
        //Find the effective spell level
        int el = this.getEffectiveSpellLevel();
        
        //See if any of the primes allow this to happen
        ArrayList<Integer> currentPrimes = combiner.getCurrentPrimes();
        ArrayList<String> currentPrimeEq = combiner.getCurrentPrimeEq();
        for(int i=0; i<currentPrimes.size();i++){
            int primeLevel = combiner.getLevelForPrime(currentPrimes.get(i));
            if(primeLevel == el){
                succeedSpell(currentPrimeEq.get(i),currentPrimes.get(i));
                return;
            }
        }
        failSpell();
    }
    
    private void succeedSpell(String equation, int prime){
        String text = spellCastText.getText()+"\n\n";
        text += "Using the equation "+equation+" = "+prime;
    }
    
    private void failSpell(){
        String text = spellCastText.getText()+"\n\n";
        text += "No adequate prime could be generated to cast this spell.";
    }
    
    private void updateTextForEffectiveLevel(){
        int el = this.getEffectiveSpellLevel();
        if(el < 0){
            spellCastText.setText("");
            return;
        }
        spellCastText.setText("The effective level to cast this spell is "+el);
    }
    
    public int getEffectiveSpellLevel(){
        String s = this.spellsList.getSelectedValue();
        Spell spell = spells.get(s);
        System.out.println(this.spellsList.getSelectedValue());
        if(spell == null){
            return -1;
        }
        
        int effectiveLevel = spell.level;
        for(Metamagic m:this.selectedMeta){
            effectiveLevel += m.levelInc;
        }
        return effectiveLevel;
    }
    

    private void addMetamagic(String name, int level) {
        String label = name + " (+" + level + ")";

        //Create the spell object
        final Metamagic meta = new Metamagic(level, name);
        this.metamagic.put(name, meta);

        //Create the checkbox
        final JCheckBox b = new JCheckBox();
        b.setText(label);

        b.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent ie) {
                if (ie.getStateChange() == ItemEvent.DESELECTED) {
                    selectedMeta.remove(meta);
                } else if (ie.getStateChange() == ItemEvent.SELECTED) {
                    selectedMeta.add(meta);
                }
                updateTextForEffectiveLevel();
            }

        });

        this.metamagicPanel.add(b);
        this.metamagicPanel.revalidate();
        this.metamagicPanel.repaint();
    }

    private void addSpell(String name, int level) {
        String label = name + " (" + level + ")";

        //Create the spell object
        final Spell spell = new Spell(level, name);
        this.spells.put(label, spell);
        this.spellListModel.addElement(label);
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        metamagicPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        spellCastText = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        spellsList = new javax.swing.JList<>();
        rollTextField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();

        javax.swing.GroupLayout metamagicPanelLayout = new javax.swing.GroupLayout(metamagicPanel);
        metamagicPanel.setLayout(metamagicPanelLayout);
        metamagicPanelLayout.setHorizontalGroup(
            metamagicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 189, Short.MAX_VALUE)
        );
        metamagicPanelLayout.setVerticalGroup(
            metamagicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 298, Short.MAX_VALUE)
        );

        jScrollPane1.setViewportView(metamagicPanel);

        spellCastText.setColumns(20);
        spellCastText.setRows(5);
        jScrollPane2.setViewportView(spellCastText);

        jScrollPane3.setViewportView(spellsList);

        jLabel1.setFont(new java.awt.Font("Viner Hand ITC", 0, 18)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Spells");

        jLabel2.setFont(new java.awt.Font("Viner Hand ITC", 0, 18)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Metamagic");

        jLabel3.setFont(new java.awt.Font("Viner Hand ITC", 0, 18)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Result");

        jButton1.setText("Add Spell");

        jButton2.setText("Delete Spell");

        jButton3.setText("Add Metamagic");

        jButton4.setText("Delete Metamagic");

        jButton5.setText("Submit Rolls");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, 191, Short.MAX_VALUE)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(4, 4, 4)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                        .addGap(6, 6, 6))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE)
                            .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE))
                    .addComponent(rollTextField)
                    .addComponent(jButton5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel3)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1)
                        .addGap(75, 75, 75))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane2)
                        .addGap(18, 18, 18)
                        .addComponent(rollTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(37, 37, 37))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jScrollPane3)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton1)
                            .addComponent(jButton3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton2)
                            .addComponent(jButton4)
                            .addComponent(jButton5))))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addMetaButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addMetaButtonActionPerformed
        String name;
        int level;

        addMeta.displaySelf();
        
        if(addMeta.getExitStatus() == AddMetaPanel.EXIT_FAILED){
            return;
        }
        
        name = addMeta.getName();
        level = addMeta.getSpellLevel();
        
        this.addMetamagic(name, level);
    }//GEN-LAST:event_addMetaButtonActionPerformed

    private void spellsListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_spellsListValueChanged
        updateTextForEffectiveLevel();
    }//GEN-LAST:event_spellsListValueChanged

    private void calcProbButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_calcProbButtonActionPerformed

    }//GEN-LAST:event_calcProbButtonActionPerformed

    private void dieCounterStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_dieCounterStateChanged
        if (!combiner.isCombosUpToDate()) {
            if (comboRecalcTimer.isRunning()) {
                this.comboRecalcTimer.restart();
            } else {
                this.comboRecalcTimer.start();
            }
        } //Don't update if it is no longer nessesary
        else if (this.comboRecalcTimer.isRunning()) {
            this.comboRecalcTimer.stop();
        }
    }//GEN-LAST:event_dieCounterStateChanged

    private void addSpellButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSpellButtonActionPerformed
        String name;
        int level;

        addSpell.displaySelf();
        
        if(addSpell.getExitStatus() == AddSpellPanel.EXIT_FAILED){
            return;
        }
        
        name = addSpell.getName();
        level = addSpell.getSpellLevel();

        this.addSpell(name, level);
    }//GEN-LAST:event_addSpellButtonActionPerformed

    private void attemptCastButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attemptCastButtonActionPerformed
        //Make sure the combos are up to date
        if (this.comboRecalcTimer.isRunning()) {
            this.comboRecalcTimer.stop();
        }
        if (!combiner.isCombosUpToDate()) {
            combiner.calculateBetweenCombos();
        }

        //Calculate the primes
        castSpell();
    }//GEN-LAST:event_attemptCastButtonActionPerformed



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JPanel metamagicPanel;
    private javax.swing.JTextField rollTextField;
    private javax.swing.JTextArea spellCastText;
    private javax.swing.JList<String> spellsList;
    // End of variables declaration//GEN-END:variables
}
