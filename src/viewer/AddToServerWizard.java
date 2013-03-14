/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package viewer;

import java.awt.CardLayout;
import java.awt.Desktop;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
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
    private class AuxJP_regex {

        private viewer.manuzioParser.Type type;
        private javax.swing.JComponent jcomponent;
        private javax.swing.JComboBox<String> jComboBox;

        public AuxJP_regex(viewer.manuzioParser.Type type, JComponent jcomponent, JComboBox<String> jComboBox) {
            this.type = type;
            this.jcomponent = jcomponent;
            this.jComboBox = jComboBox;
        }

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

    /**
     * ActionListener delle JComboBox presenti nel pannello jP_regex
     */
    private static class JComboBoxActionListener implements ActionListener {

        private JTextField jtf;

        private JComboBoxActionListener(JTextField c3) {
            jtf = c3;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (((JComboBox) (e.getSource())).getSelectedIndex() == 5) {
                jtf.setEnabled(true);
                jtf.setText("Inserire una stringa Regex");
            } else {
                jtf.setEditable(false);
                jtf.setText("");
            }
        }
    }
    private static final String firstStep = "firstStep"; // nome primo gruppo di pannelli (caricamento dati)
    private static final String file = "file";
    private static final String regex = "regex";
    private static final String regexLarge = "regexLarge";
    private static final String secondStep = "secondStep"; // nome secondo gruppo di panelli (Caricamento attributi)
    /**
     * <p>Esegue l'inserimento usando tutti i type disponibili</p>
     */
    public static final int COMPLETE_PROCEDURE = -1;
    /**
     * <p>Stringhe da visualzzare nei combobox del pannello jP_regex e indicano
     * i tipi comunemente usati di formattazione nei testi
     */
    public static final String tab_type[] = {
        "Carattere",
        "Parola",
        "Frase",
        "Paragrafo",
        "Oggetto Complesso",
        "Altro"
    };
    private static final String tab_typeToolTips[] = {
        "<html><p>Un semplice carattere/simbolo</p></html>",
        "<html><p>Un insieme di \"caratteri\"</p></html>",
        "<html><p>Un insieme di \"Parole\" che <br />iniziano con una lettera maiuscola e teminano <br /> con un \".\" e senza un capoverso</p></html>",
        "<html><p>Un insieme di \"Frasi\" terminanti con un capoverso</p></html>",
        "<html><p>Un oggetto/testo rappresentabile solo attraverso la selezione diretta,<br /> come ad esempio un capitolo</p></html>",
        "<html><p>Un oggetto rappresentabile attraverso una Espressione Regolare.</p></html>"
    };
    private int idx_to;
    private String currentStep;
    private String currentCard;
    private TaskRawInput taskRawInput = null;
    private MainWindow mw;
    private ArrayList<String> filetext;

    /**
     * <p>Crea una nuovo AddToServerWizard. Se id è uguale a
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
        // inizianuzzo il pannello jP_regex
        initRegex();
        pack();

    }

    /**
     * <p>Inizializza in particolare il 3° panello,
     * <code>jP_regex</code>, in base ai parametri usati per costruire la
     * classe. </p>
     */
    private void initRegex() {
        int rowN = ManuzioViewer.schema.sizeTypes(); // Numero di tipi presenti
        // Creo un layout a griglia e lo aggiungo al pannello interno
        GridLayout gL_regIn = new GridLayout(rowN + 1, 1);
        jP_regexInner.setLayout(gL_regIn);

        // Creo il primo pannello variabile a secodo del valore di idx_to
        JPanel jPsub_comment = new JPanel();
        // Aggiunta JLabel commenti
        String commentText = "<html><p>Ora è necesssario associare per ogni textual object, specificato nello Schema, <br />"
                + "il relativo testo che lo compone. Per facilitare l'operazione per i tipi più semplici basterà <br />"
                + "scegliere tra le tipologie già suggerite, usare una <a href=\"http://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html\">Espressione Regolare</a> oppure selezionare il <br />"
                + "testo scelto attreverso un editor. Si noti che i simboli di punteggiatura quali, punti, <br />"
                + "virgole e similari, verranno automaricamente separati dal testo ed inseriti con il tipo <br />"
                + "minimo presente nello schema. Se è stata scelta l'aggiunta ad un textual object verranno <br />"
                + "presentati solo i sottotipi interessati</p></html>";
        javax.swing.JLabel comment = new javax.swing.JLabel(commentText);
        comment.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop desktop = Desktop.getDesktop();
                        desktop.browse(new java.net.URI("http://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html"));
                    } catch (URISyntaxException | IOException ex) {
                        Logger.getLogger(AddToServerWizard.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
        jPsub_comment.add(comment);
        // Aggiungo il pannello dei commenti
        jP_regexInner.add(jPsub_comment);

        // array valori combobox
        JLabel c_label[] = {null, null, null, null, null, null};

        for (int i = 0; i < c_label.length; i++) {
            c_label[i] = new JLabel(tab_type[i]);
            c_label[i].setToolTipText(tab_typeToolTips[i]);
        }

        if (idx_to == -1) {
            // inserisco tutti i tipi
            viewer.manuzioParser.Type[] typeSet = ManuzioViewer.schema.getTypeSet();
            for (viewer.manuzioParser.Type type : typeSet) {
                JPanel tmp = new JPanel();
                GroupLayout layout = new GroupLayout(tmp);
                tmp.setLayout(layout);
                layout.setAutoCreateGaps(true);
                layout.setAutoCreateContainerGaps(true);
                JLabel c1 = new JLabel("Tipo: " + type.getTypeName());
                JComboBox c2 = new JComboBox(c_label);
                c2.setEditable(false);
                JTextField c3 = new JTextField();
                c3.setEditable(true);
                c3.setEnabled(false);
                c2.addActionListener(new JComboBoxActionListener(c3));
                layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(c1)
                        .addGroup(layout.createSequentialGroup()
                        .addComponent(c2)
                        .addComponent(c3)));
                // Aggiungo il pannello a qello interno
                jP_regexInner.add(tmp);
            }

        } else {
            // inserisco solo i sotto tipi di idx_to
        }

    }

    /**
     * <p>Attiva e disattiva i JButton e predispone se necessario le variabili
     * per eseguire i possibili cambiamenti. </p>
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
                break;
            case regex:
                currentCard = regex;
                jB_previous.setEnabled(true);
                jB_next.setEnabled(true);
                // resetto tutti gli oggetti del pannello regex in base ai dati 
                // di input ottenuti dal pannello file se necessario
                break;
            case regexLarge: // Inizializza il 3 pannello aggiungendolo al CardLayer se non presente
                break;
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
                            CardLayout layout = (CardLayout) jP_firstStep.getLayout();;
                            prepareFirstStepCard(regex);
                            layout.next(jP_firstStep);
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
        jL_fileTittle = new javax.swing.JLabel();
        jP_fileInner = new javax.swing.JPanel();
        jFileChooser = new JFileChooser(FileSystemView.getFileSystemView());
        jP_regex = new javax.swing.JPanel();
        jL_regexTittle = new javax.swing.JLabel();
        jScroll_regex = new javax.swing.JScrollPane();
        jP_regexInner = new javax.swing.JPanel();
        jP_regexLarge = new javax.swing.JPanel();
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

        jP_file.setMaximumSize(new java.awt.Dimension(595, 363));
        jP_file.setLayout(new java.awt.BorderLayout());

        jL_fileTittle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jL_fileTittle.setText("<html><b>Scegli un file</b></html>");
        jL_fileTittle.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(0, 0, 0)));
        jP_file.add(jL_fileTittle, java.awt.BorderLayout.PAGE_START);

        jP_fileInner.setMaximumSize(new java.awt.Dimension(595, 346));

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
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jFileChooser, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 334, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jP_file.add(jP_fileInner, java.awt.BorderLayout.CENTER);

        jP_firstStep.add(jP_file, "file");

        jP_regex.setMaximumSize(new java.awt.Dimension(595, 363));
        jP_regex.setMinimumSize(new java.awt.Dimension(595, 363));
        jP_regex.setLayout(new java.awt.BorderLayout());

        jL_regexTittle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jL_regexTittle.setText("<html><b>Associazione Manuzio's Type</b></html>");
        jL_regexTittle.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(0, 0, 0)));
        jP_regex.add(jL_regexTittle, java.awt.BorderLayout.PAGE_START);

        jScroll_regex.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScroll_regex.setMaximumSize(new java.awt.Dimension(595, 346));
        jScroll_regex.setMinimumSize(new java.awt.Dimension(595, 346));
        jScroll_regex.setPreferredSize(new java.awt.Dimension(595, 346));

        org.jdesktop.layout.GroupLayout jP_regexInnerLayout = new org.jdesktop.layout.GroupLayout(jP_regexInner);
        jP_regexInner.setLayout(jP_regexInnerLayout);
        jP_regexInnerLayout.setHorizontalGroup(
            jP_regexInnerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 591, Short.MAX_VALUE)
        );
        jP_regexInnerLayout.setVerticalGroup(
            jP_regexInnerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 342, Short.MAX_VALUE)
        );

        jScroll_regex.setViewportView(jP_regexInner);

        jP_regex.add(jScroll_regex, java.awt.BorderLayout.CENTER);

        jP_firstStep.add(jP_regex, "regex");

        org.jdesktop.layout.GroupLayout jP_regexLargeLayout = new org.jdesktop.layout.GroupLayout(jP_regexLarge);
        jP_regexLarge.setLayout(jP_regexLargeLayout);
        jP_regexLargeLayout.setHorizontalGroup(
            jP_regexLargeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 595, Short.MAX_VALUE)
        );
        jP_regexLargeLayout.setVerticalGroup(
            jP_regexLargeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 363, Short.MAX_VALUE)
        );

        jP_firstStep.add(jP_regexLarge, "regexLarge");

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
                case file:    // Seleziono il file da aggiungere
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
                        // le chiamate alle funzioni successive da prendere
                        // in questa fase sono all'interno del listener per una
                        // questione di pura praticità
                    }
                    break;
                case regex:
                    break;
                case regexLarge:
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
                case regex: // torno a file
                    CardLayout layout = (CardLayout) jP_firstStep.getLayout();
                    prepareFirstStepCard(file);
                    layout.previous(jP_firstStep);
                    break;
                case regexLarge:
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
                    break;
                case regex:
                    this.setVisible(false);
                    break;
                case regexLarge:
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
    private javax.swing.JLabel jL_fileTittle;
    private javax.swing.JLabel jL_regexTittle;
    private javax.swing.JPanel jP_control;
    private javax.swing.JPanel jP_file;
    private javax.swing.JPanel jP_fileInner;
    private javax.swing.JPanel jP_firstStep;
    private javax.swing.JPanel jP_regex;
    private javax.swing.JPanel jP_regexInner;
    private javax.swing.JPanel jP_regexLarge;
    private javax.swing.JPanel jP_secondStep;
    private javax.swing.JProgressBar jProgressBar;
    private javax.swing.JScrollPane jScroll_regex;
    // End of variables declaration//GEN-END:variables
}
