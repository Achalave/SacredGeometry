/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
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
    
    ArrayList<Integer> rolls;
    
    AddSpellPanel addSpell;
    AddMetaPanel addMeta;
    
    boolean d8 = false;
    JCheckBox lastSelectedMeta;
    
    public static void main(String[] args){
        JFrame frame = new JFrame();
        MainPanel mp = new MainPanel();
        frame.add(mp);
        frame.setJMenuBar(mp.generateMenuBar());
        frame.setSize(mp.getPreferredSize());
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    /**
     * Generates the menu bar object used by the frame for this application.
     * @return One JMenuBar.
     */
    private JMenuBar generateMenuBar(){
        rolls = new ArrayList<>();
        
        final Container container = this;
        JMenuBar menuBar = new JMenuBar();
        
        JMenu calculate = new JMenu("Features");
        menuBar.add(calculate);
        
        JMenuItem item = new JRadioButtonMenuItem("D8 option");
        ((JRadioButtonMenuItem)item).setSelected(d8);
        item.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae) {
                System.out.println("TEST");
            }
        });
        calculate.add(item);
        
        
        item = new JMenuItem("Probabilities");
        item.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae) {
                new ProbabilityPanel().displaySelf(container);
            }
        });
        calculate.add(item);
        
        return menuBar;
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
                combiner.setNumDie((int)dieCounter.getValue());
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
    
    /**
     * Loads all the spells stored in the spells file
     */
    private void loadSpells() {
        try (Scanner scan = FileManager.openFileForReading(FileManager.SPELLS_PATH)) {
            if(scan == null){
                return;
            }
            
            String[] line;
            while(scan.hasNext()){
                line = scan.nextLine().split("@");
                this.addSpell(line[0], Integer.parseInt(line[1]));
            }
        }
    }

    /**
     * Loads all the metamagic stored in the metamagic file
     */
    private void loadMetamagic() {
        try (Scanner scan = FileManager.openFileForReading(FileManager.METAMAGIC_PATH)) {
            if(scan == null){
                return;
            }
            
            String[] line;
            while(scan.hasNext()){
                line = scan.nextLine().split("@");
                this.addMetamagic(line[0], Integer.parseInt(line[1]));
            }
        }
    }

    private void saveSpells(){
        //Aggregate all the spells
        ArrayList<Spell> sp = new ArrayList(spells.values());
        Collections.sort(sp);
        //Save the spells to their file
        FileManager.deleteFile(FileManager.SPELLS_PATH);
        try (PrintWriter write = FileManager.openFileForWriting(FileManager.SPELLS_PATH)) {
            for(Spell s:sp){
                write.println(s);
            }
        }
    }
    
    private void saveMetamagic(){
        //Aggregate all the metamagic
        ArrayList<Metamagic> mm = new ArrayList(metamagic.values());
        Collections.sort(mm);
        //Save the metamagic to their file
        FileManager.deleteFile(FileManager.METAMAGIC_PATH);
        try (PrintWriter write = FileManager.openFileForWriting(FileManager.METAMAGIC_PATH)) {
            for(Metamagic m:mm){
                write.println(m);
            }
        }
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

    private void displayDieOutOfBoundsError(){
        this.setupForRollsError();
        this.rollTextField.setText("Die Value Out of Bounds!");
    }  
    
    public void castSpell(){
        //Find the primes that can be created
        int[] rollsA = new int[rolls.size()];
        for(int i=0; i<rolls.size();i++){
            rollsA[i] = rolls.get(i);
        }
        
        CombinationData data = combiner.calculatePrimes(rollsA, d8);
        
        if(data == null){
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
                case Combiner.DIE_VALUE_OUT_OF_BOUNDS:
                    this.displayDieOutOfBoundsError();
                    return;
            }
        }
        
        //Find the effective spell level
        int effectiveLevel = this.getEffectiveSpellLevel();
        
        //See if any of the primes allow this to happen
        HashMap<Integer,String> currentPrimes = data.getCurrentLevels();
        for(int i=0; i<currentPrimes.size();i++){
            if(currentPrimes.containsKey(effectiveLevel)){
                succeedSpell(currentPrimes.get(effectiveLevel),data.getCurrentPrimes().get(effectiveLevel));
                return;
            }
        }
        failSpell();
    }
    
    private void succeedSpell(String equation, int prime){
        String text = spellCastText.getText()+"\n\n";
        text += "Using the equation "+equation+" = "+prime;
        spellCastText.setText(text);
    }
    
    private void failSpell(){
        String text = spellCastText.getText()+"\n\n";
        text += "No adequate prime could be generated to cast this spell.";
        spellCastText.setText(text);
    }
    
    private void updatePerliminaryText(){
        int el = this.getEffectiveSpellLevel();
        if(el < 0){
            spellCastText.setText("");
            return;
        }
        String text = "The effective level to cast this spell is "+el;
        spellCastText.setText(text);
    }
    
    public int getEffectiveSpellLevel(){
        String s = this.spellsList.getSelectedValue();
        Spell spell = spells.get(s);
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
        this.metamagic.put(label, meta);

        //Create the checkbox
        final JCheckBox b = new JCheckBox();
        b.setText(label);

        b.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent ie) {
                lastSelectedMeta = b;
                if (ie.getStateChange() == ItemEvent.DESELECTED) {
                    selectedMeta.remove(meta);
                } else if (ie.getStateChange() == ItemEvent.SELECTED) {
                    selectedMeta.add(meta);
                }
                updatePerliminaryText();
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
    
    private void removeSpell(){
        String s = this.spellsList.getSelectedValue();
        this.spells.remove(s);
        this.spellListModel.removeElementAt(this.spellsList.getSelectedIndex());
        this.saveSpells();
    }
    
    private void removeMeta(){
        if(this.lastSelectedMeta != null){
            this.metamagicPanel.remove(lastSelectedMeta);
            this.metamagic.remove(lastSelectedMeta.getText());
            this.metamagicPanel.revalidate();
            this.metamagicPanel.repaint();
            this.saveMetamagic();
        }
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
        addSpellButton = new javax.swing.JButton();
        deleteSpellButton = new javax.swing.JButton();
        addMetaButton = new javax.swing.JButton();
        deleteMetaButton = new javax.swing.JButton();
        submitRollButton = new javax.swing.JButton();
        dieCounter = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();

        javax.swing.GroupLayout metamagicPanelLayout = new javax.swing.GroupLayout(metamagicPanel);
        metamagicPanel.setLayout(metamagicPanelLayout);
        metamagicPanelLayout.setHorizontalGroup(
            metamagicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 241, Short.MAX_VALUE)
        );
        metamagicPanelLayout.setVerticalGroup(
            metamagicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 429, Short.MAX_VALUE)
        );

        jScrollPane1.setViewportView(metamagicPanel);

        spellCastText.setEditable(false);
        spellCastText.setColumns(20);
        spellCastText.setRows(5);
        jScrollPane2.setViewportView(spellCastText);

        spellsList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                spellsListValueChanged(evt);
            }
        });
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

        addSpellButton.setText("Add Spell");
        addSpellButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addSpellButtonActionPerformed(evt);
            }
        });

        deleteSpellButton.setText("Delete Spell");
        deleteSpellButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteSpellButtonActionPerformed(evt);
            }
        });

        addMetaButton.setText("Add Metamagic");
        addMetaButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addMetaButtonActionPerformed(evt);
            }
        });

        deleteMetaButton.setText("Delete Metamagic");
        deleteMetaButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteMetaButtonActionPerformed(evt);
            }
        });

        submitRollButton.setText("Submit Roll");
        submitRollButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                submitRollButtonActionPerformed(evt);
            }
        });

        dieCounter.setModel(new javax.swing.SpinnerNumberModel(2, 2, null, 1));
        dieCounter.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                dieCounterStateChanged(evt);
            }
        });

        jLabel4.setText("Num Die:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE)
                    .addComponent(deleteSpellButton, javax.swing.GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE)
                    .addComponent(addSpellButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(4, 4, 4)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE)))
                        .addGap(6, 6, 6))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(deleteMetaButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(addMetaButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE))
                    .addComponent(submitRollButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dieCounter, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rollTextField)))
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
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 431, Short.MAX_VALUE)
                    .addComponent(jScrollPane2)
                    .addComponent(jScrollPane3))
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel4)
                        .addComponent(dieCounter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(rollTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(addSpellButton)
                            .addComponent(addMetaButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(deleteSpellButton)
                            .addComponent(deleteMetaButton)
                            .addComponent(submitRollButton))))
                .addGap(13, 13, 13))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addMetaButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addMetaButtonActionPerformed
        String name;
        int level;

        addMeta.displaySelf(this);
        
        if(addMeta.getExitStatus() == AddMetaPanel.EXIT_FAILED){
            return;
        }
                
        name = addMeta.getMetaName();
        level = addMeta.getMetaLevel();
        
        if(name == null || name.isEmpty()){
            JOptionPane.showMessageDialog(this, "You must provide a metamagic name.");
            return;
        }else if(spells.containsKey(name)){
            JOptionPane.showMessageDialog(this, "That metamagic already exists in the list.");
            return;
        }
        
        this.addMetamagic(name, level);
        
        this.saveMetamagic();
    }//GEN-LAST:event_addMetaButtonActionPerformed

    private void spellsListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_spellsListValueChanged
        updatePerliminaryText();
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

        addSpell.displaySelf(this);
        
        if(addSpell.getExitStatus() == AddSpellPanel.EXIT_FAILED){
            return;
        }
        
        name = addSpell.getSpellName();
        level = addSpell.getSpellLevel();

        if(name == null || name.isEmpty()){
            JOptionPane.showMessageDialog(this, "You must provide a spell name.");
            return;
        }else if(spells.containsKey(name)){
            JOptionPane.showMessageDialog(this, "That spell already exists in the list.");
            return;
        }
        
        this.addSpell(name, level);
        
        this.saveSpells();
    }//GEN-LAST:event_addSpellButtonActionPerformed

    private void submitRollButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_submitRollButtonActionPerformed
        //Make sure the combos are up to date
        if (this.comboRecalcTimer.isRunning()) {
            this.comboRecalcTimer.stop();
        }
        if (!combiner.isCombosUpToDate()) {
            combiner.calculateBetweenCombos();
        }

        //Calculate the primes
        castSpell();
    }//GEN-LAST:event_submitRollButtonActionPerformed

    private void deleteSpellButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteSpellButtonActionPerformed
        removeSpell();
    }//GEN-LAST:event_deleteSpellButtonActionPerformed

    private void deleteMetaButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteMetaButtonActionPerformed
        removeMeta();
    }//GEN-LAST:event_deleteMetaButtonActionPerformed



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addMetaButton;
    private javax.swing.JButton addSpellButton;
    private javax.swing.JButton deleteMetaButton;
    private javax.swing.JButton deleteSpellButton;
    private javax.swing.JSpinner dieCounter;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JPanel metamagicPanel;
    private javax.swing.JTextField rollTextField;
    private javax.swing.JTextArea spellCastText;
    private javax.swing.JList<String> spellsList;
    private javax.swing.JButton submitRollButton;
    // End of variables declaration//GEN-END:variables
}
