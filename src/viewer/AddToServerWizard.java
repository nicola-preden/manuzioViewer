/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package viewer;

import java.awt.CardLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import viewer.taskThread.TaskRawInput;

/**
 *
 * @author Nicola Preden, matricola 818578, Facoltà di informatica Ca' Foscari
 * in Venice
 */
public class AddToServerWizard extends javax.swing.JFrame implements PropertyChangeListener {

    /**
     * <p>Classe ausigliaria per creare una struttura contennente tutti i 
     * componenti del pannello regex. </p>
     */
    private class AuxBean {
        private viewer.manuzioParser.Type type;
        private javax.swing.JComponent jcomponent;
        private javax.swing.JComboBox<String> jComboBox;

        public viewer.manuzioParser.Type getType() {
            return type;
        }

        public void setType(viewer.manuzioParser.Type type) {
            this.type = type;
        }

        public JComponent getJcomponent() {
            return jcomponent;
        }

        public void setJcomponent(JComponent jcomponent) {
            this.jcomponent = jcomponent;
        }

        public JComboBox<String> getjComboBox() {
            return jComboBox;
        }

        public void setjComboBox(JComboBox<String> jComboBox) {
            this.jComboBox = jComboBox;
        }
        
    }
    private static final String firstStep = "firstStep"; // nome primo gruppo di pannelli
    private static final String file = "file";
    private static final String regex = "regex";
    private static final String confirm = "confirm";
    private static final String secondStep = "secondStep"; // nome secondo gruppo di panelli
    public static final int COMPLETE_PROCEDURE = -1;
    private int idx_to;
    private String currentStep;
    private String currentCard;
    private TaskRawInput taskRawInput = null;
    private MainWindow mw;
    private ArrayList<String> filetext;

    /**
     * <p>Crea una nuovo AddToServerWizard. Se
     * <code>id</code> è uguale a
     * <code>AddToServerWizard.COMPLETE_PROCEDURE</code> allora il nuovo testo
     * verra un nuovo texual object avente come type il maxType dello schema
     * corrente</p>
     *
     * @param id intero indicante id di un textual object
     * @param mw jframe padre
     */
    public AddToServerWizard(int id, MainWindow mw) {
        initComponents();
        idx_to = id;
        jB_previous.setEnabled(false);
        jProgressBar.setVisible(false);
        currentStep = firstStep;
        currentCard = file;
        this.mw = mw;

    }

    /**
     * Carica i dati nel JFrame successivo
     *
     * @param name stringa contenete il nome del jpanel da preparare
     */
    private void prepareFirstStepCard(String name) {
        switch (name) {
            case file: // inizializza la finesta 
                currentCard = file;
                taskRawInput = null;
                jB_previous.setEnabled(false);
                jB_next.setEnabled(false);
                jFileChooser.setSelectedFile(null);
            case regex:
                currentCard = regex;
                jB_previous.setEnabled(true);
                jB_next.setEnabled(true);
                // resetto tutti gli oggetti del pannello regex in base ai dati 
                // di input ottenuti dal pannello file
                
                
            case confirm:
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (currentStep.compareTo(firstStep) == 0) { // Se vero stiamo caricando il file
            if ("progress".equals(evt.getPropertyName())) {
                int progress = (Integer) evt.getNewValue();
                jProgressBar.setValue(progress);
                if (progress == 100) {
                    try {
                        filetext = taskRawInput.get();
                    } catch (InterruptedException | ExecutionException ex) {
                        Logger.getLogger(AddToServerWizard.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        taskRawInput.removePropertyChangeListener(this);
                        if (!filetext.isEmpty()) {
                            // è stato caricato qualcosa dal file
                            taskRawInput = null;
                            jProgressBar.setVisible(false);
                            CardLayout layout = (CardLayout) cards.getLayout();
                            prepareFirstStepCard(regex);
                            layout.next(cards);
                        } else {
                            // c'e stato un errore di qualche tipo
                            taskRawInput = null;
                            jB_next.setEnabled(true);
                            jProgressBar.setVisible(false);
                            JOptionPane.showMessageDialog(this, "Errore nel caricamento del file", "Errore", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
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

        cards = new javax.swing.JPanel();
        jP_firstStep = new javax.swing.JPanel();
        jP_file = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jP_fileInner = new javax.swing.JPanel();
        jFileChooser = new JFileChooser(FileSystemView.getFileSystemView());
        jP_regex = new javax.swing.JPanel();
        jP_confirm = new javax.swing.JPanel();
        jP_secondStep = new javax.swing.JPanel();
        jP_control = new javax.swing.JPanel();
        jB_close = new javax.swing.JButton();
        jB_next = new javax.swing.JButton();
        jB_previous = new javax.swing.JButton();
        jProgressBar = new javax.swing.JProgressBar();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Aggiunta");
        setAlwaysOnTop(true);
        setMinimumSize(new java.awt.Dimension(400, 300));
        setResizable(false);

        cards.setLayout(new java.awt.CardLayout());

        jP_firstStep.setLayout(new java.awt.CardLayout());

        jP_file.setLayout(new java.awt.BorderLayout());

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("<html><b>Scegli un file</b></html>");
        jP_file.add(jLabel2, java.awt.BorderLayout.PAGE_START);

        jFileChooser.setAcceptAllFileFilterUsed(false);
        jFileChooser.setControlButtonsAreShown(false);
        jFileChooser.setCurrentDirectory(null);
        jFileChooser.setFileFilter(new FileNameExtensionFilter("Plain text file", "txt"));

        org.jdesktop.layout.GroupLayout jP_fileInnerLayout = new org.jdesktop.layout.GroupLayout(jP_fileInner);
        jP_fileInner.setLayout(jP_fileInnerLayout);
        jP_fileInnerLayout.setHorizontalGroup(
            jP_fileInnerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jP_fileInnerLayout.createSequentialGroup()
                .addContainerGap()
                .add(jFileChooser, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 583, Short.MAX_VALUE)
                .addContainerGap())
        );
        jP_fileInnerLayout.setVerticalGroup(
            jP_fileInnerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jP_fileInnerLayout.createSequentialGroup()
                .addContainerGap(7, Short.MAX_VALUE)
                .add(jFileChooser, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 334, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jP_file.add(jP_fileInner, java.awt.BorderLayout.CENTER);

        jP_firstStep.add(jP_file, "file");

        org.jdesktop.layout.GroupLayout jP_regexLayout = new org.jdesktop.layout.GroupLayout(jP_regex);
        jP_regex.setLayout(jP_regexLayout);
        jP_regexLayout.setHorizontalGroup(
            jP_regexLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 595, Short.MAX_VALUE)
        );
        jP_regexLayout.setVerticalGroup(
            jP_regexLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 363, Short.MAX_VALUE)
        );

        jP_firstStep.add(jP_regex, "regex");

        org.jdesktop.layout.GroupLayout jP_confirmLayout = new org.jdesktop.layout.GroupLayout(jP_confirm);
        jP_confirm.setLayout(jP_confirmLayout);
        jP_confirmLayout.setHorizontalGroup(
            jP_confirmLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 595, Short.MAX_VALUE)
        );
        jP_confirmLayout.setVerticalGroup(
            jP_confirmLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 363, Short.MAX_VALUE)
        );

        jP_firstStep.add(jP_confirm, "confirm");

        cards.add(jP_firstStep, "firstStep");

        jP_secondStep.setLayout(new java.awt.CardLayout());
        cards.add(jP_secondStep, "secondStep");

        getContentPane().add(cards, java.awt.BorderLayout.CENTER);

        jP_control.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(0, 0, 0)));

        jB_close.setText("Annulla");
        jB_close.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jB_closeActionPerformed(evt);
            }
        });

        jB_next.setText("Avanti");
        jB_next.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jB_nextActionPerformed(evt);
            }
        });

        jB_previous.setText("Indietro");
        jB_previous.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jB_previousActionPerformed(evt);
            }
        });

        jProgressBar.setStringPainted(true);

        org.jdesktop.layout.GroupLayout jP_controlLayout = new org.jdesktop.layout.GroupLayout(jP_control);
        jP_control.setLayout(jP_controlLayout);
        jP_controlLayout.setHorizontalGroup(
            jP_controlLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jP_controlLayout.createSequentialGroup()
                .addContainerGap(308, Short.MAX_VALUE)
                .add(jP_controlLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jProgressBar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jP_controlLayout.createSequentialGroup()
                        .add(jB_previous)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jB_next)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jB_close)))
                .addContainerGap())
        );
        jP_controlLayout.setVerticalGroup(
            jP_controlLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jP_controlLayout.createSequentialGroup()
                .addContainerGap(33, Short.MAX_VALUE)
                .add(jProgressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jP_controlLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jB_close)
                    .add(jB_next)
                    .add(jB_previous))
                .addContainerGap())
        );

        getContentPane().add(jP_control, java.awt.BorderLayout.PAGE_END);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jB_nextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jB_nextActionPerformed
        // TODO add your handling code here:
        if (currentStep.compareTo(firstStep) == 0) { // Se siamo nella prima fase di selezione
            switch (currentCard) {
                case file: {    // Seleziono il file da aggiungere
                    File selectedFile = jFileChooser.getSelectedFile();
                    if (selectedFile == null || !selectedFile.isFile()) {
                        JOptionPane.showMessageDialog(this, "Selezionare un file", "Attenzione", JOptionPane.WARNING_MESSAGE);
                    } else {
                        // Aggiungo il contenuto del file in un oggetto.
                        taskRawInput = new TaskRawInput(selectedFile.getPath());
                        jProgressBar.setVisible(true);
                        jProgressBar.setValue(0);
                        taskRawInput.addPropertyChangeListener(this);
                        jB_next.setEnabled(false);
                        taskRawInput.execute();
                    }
                }
                case regex:
                case confirm:
                default:
                    break;
            }
        }
        if (currentStep.compareTo(secondStep) == 0) {
            switch (currentCard) {

            }
        }

    }//GEN-LAST:event_jB_nextActionPerformed

    private void jB_previousActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jB_previousActionPerformed
        // TODO add your handling code here:
        if (currentStep.compareTo(firstStep) == 0) { // Se siamo nella prima fase di selezione
            switch (currentCard) {
                case file: // Impossibile dovrei essere disattivato
                    break;
                case regex:
                case confirm:
                default:
                    break;
            }
        }
        if (currentStep.compareTo(secondStep) == 0) {
            switch (currentCard) {

            }
        }
    }//GEN-LAST:event_jB_previousActionPerformed

    private void jB_closeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jB_closeActionPerformed
        // TODO add your handling code here:
        if (currentStep.compareTo(firstStep) == 0) { // Se siamo nella prima fase di selezione
            switch (currentCard) {
                case file:
                    if (taskRawInput == null) {
                        this.setVisible(false);
                    } else {
                        taskRawInput.cancel(true);
                        this.setVisible(false);
                    }
                case regex:
                case confirm:
                default:
                    break;
            }
        }
        if (currentStep.compareTo(secondStep) == 0) {
            switch (currentCard) {

            }
        }
    }//GEN-LAST:event_jB_closeActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel cards;
    private javax.swing.JButton jB_close;
    private javax.swing.JButton jB_next;
    private javax.swing.JButton jB_previous;
    private javax.swing.JFileChooser jFileChooser;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jP_confirm;
    private javax.swing.JPanel jP_control;
    private javax.swing.JPanel jP_file;
    private javax.swing.JPanel jP_fileInner;
    private javax.swing.JPanel jP_firstStep;
    private javax.swing.JPanel jP_regex;
    private javax.swing.JPanel jP_secondStep;
    private javax.swing.JProgressBar jProgressBar;
    // End of variables declaration//GEN-END:variables
}
