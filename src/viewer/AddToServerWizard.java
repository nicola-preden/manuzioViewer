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
import java.util.Arrays;
import java.util.Iterator;
import java.util.ResourceBundle;
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

    private static final ResourceBundle lang = ResourceBundle.getBundle("viewer/language/lang", ManuzioViewer.LANGUAGE);

    /**
     * <p>Classe ausigliaria per creare una struttura contennente tutti i
     * componenti del pannello regex. </p>
     */
    private class AuxJP_regex {

        private viewer.manuzioParser.Type type;
        private javax.swing.JComponent jcomponent;
        private javax.swing.JComboBox<String> jComboBox;

        public AuxJP_regex(viewer.manuzioParser.Type type, JComboBox<String> jComboBox, JComponent jcomponent) {
            this.type = type;
            this.jcomponent = jcomponent;
            this.jComboBox = jComboBox;
        }

        public viewer.manuzioParser.Type getType() {
            return type;
        }

        public JComponent getJcomponent() {
            return jcomponent;
        }

        public JComboBox<String> getjComboBox() {
            return jComboBox;
        }
    }

    /**
     * <p>ActionListener delle JComboBox presenti nel pannello jP_regex. </p>
     */
    private class JComboBoxActionListener implements ActionListener {

        private JTextField jtf;

        private JComboBoxActionListener(JTextField c3) {
            jtf = c3;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int x = ((JComboBox) (e.getSource())).getSelectedIndex();
            switch (x) {
                case 6:
                    jtf.setEnabled(true);
                    jtf.setEditable(true);
                    jtf.setText(lang.getString("INSERT_REGEX"));
                    break;
                default:
                    jtf.setEnabled(false);
                    jtf.setEditable(false);
                    jtf.setText("");
                    break;
            }
        }
    }
    private static final String firstStep = "firstStep";    // nome primo gruppo di pannelli (caricamento dati)
    private static final String file = "file";
    private static final String regex = "regex";
    private static final String regexLarge = "regexLarge";  // Corferma dati
    private static final String secondStep = "secondStep";  // nome secondo gruppo di panelli (Caricamento oggetti)
    private SecondStepStrategy sss = null;
    /**
     * <p>Esegue l'inserimento usando tutti i type disponibili. </p>
     */
    public static final int COMPLETE_PROCEDURE = -1;
    private int idX_to; // id della testa
    private String stringX_to; // type della testa
    private viewer.manuzioParser.Type[] orderType; // l'elenco dei soli tipi da utilizzare
    /**
     * <p>Stringhe da visualzzare nei combobox del pannello jP_regex e indicano
     * i tipi comunemente usati di formattazione nei testi. </p>
     */
    private static final String tab_type[] = {
        lang.getString("CHAR"),
        lang.getString("WORD"),
        lang.getString("SENTENCE"),
        lang.getString("PARAGRAPH"),
        lang.getString("ALL_TEXT"),
        lang.getString("SELECTION"),
        lang.getString("REG_EXP")
    };
    private String currentStep; // step attuale
    private String currentCard; // carta raggiunta
    private TaskRawInput taskRawInput = null;
    /**
     * <p>Il file diviso i paragrafi. </p>
     */
    private String filetext;
    private ArrayList<AuxJP_regex> type_setting;

    /**
     * <p>Crea una nuovo AddToServerWizard. Se id è uguale a
     * <tt>AddToServerWizard.COMPLETE_PROCEDURE</tt> allora il nuovo testo verra
     * un nuovo texual object avente come type il maxType dello schema
     * corrente</p>
     *
     * @param id intero indicante id di un textual object
     * @param type il tipo dell'id spassato come input
     * @throws IllegalArgumentException se i parametri sono incoerenti
     */
    public AddToServerWizard(int id, String type) {
        initComponents();    
        idX_to = id;
        if (type == null) {
            // type mancante thrown exception
            throw new IllegalArgumentException();
        } else {
            stringX_to = type;
        }


        jB_previous.setEnabled(false);
        jProgressBar.setVisible(false);
        currentStep = firstStep;
        currentCard = file;
        // inizianuzzo il pannello jP_regex
        initRegex();
        pack();

    }

    /**
     * <p>Inizializza in particolare il panello
     * <tt>jP_regex</tt>, in base ai parametri usati per costruire la classe.
     * </p>
     */
    private void initRegex() {
        int rowN = ManuzioViewer.schema.sizeTypes(); // Numero di tipi presenti
        // Creo un layout a griglia e lo aggiungo al pannello interno
        GridLayout gL_regIn = new GridLayout(rowN + 1, 1);
        jP_regexInner.setLayout(gL_regIn);

        // Creo il primo pannello variabile a secodo del valore di idX_to
        JPanel jPsub_comment = new JPanel();
        // Aggiunta JLabel commenti
        String commentText = lang.getString("TEXT_1")
                + lang.getString("TEXT_2")
                + lang.getString("TEXT_3")
                + lang.getString("TEXT_4")
                + lang.getString("TEXT_5")
                + lang.getString("TEXT_6");
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

        // creo contenitore configurazioni temporanee
        type_setting = new ArrayList<AuxJP_regex>(rowN);
        if (idX_to == AddToServerWizard.COMPLETE_PROCEDURE) { // elenco tipi utilizzabili
            orderType = getOrderType(ManuzioViewer.schema.getMaximalUnit());
        } else {
            orderType = getOrderType(ManuzioViewer.schema.getType(stringX_to));
        }
        // caricamento grafica
        for (viewer.manuzioParser.Type type : orderType) {
            JPanel tmp = new JPanel();
            GroupLayout layout = new GroupLayout(tmp);
            tmp.setLayout(layout);
            layout.setAutoCreateGaps(true);
            layout.setAutoCreateContainerGaps(true);
            JLabel c1 = new JLabel(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("viewer/language/lang").getString("TYPE: {0}"), new Object[]{type.getTypeName()}));
            JComboBox c2 = new JComboBox(tab_type);
        c2.setEditable(false);
        JTextField c3 = new JTextField(30);
        c3.setEnabled(false);

        c2.addActionListener(new JComboBoxActionListener(c3));
        // http://docs.oracle.com/javase/tutorial/uiswing/layout/groupExample.html
        // layout Orizzontale
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(c1)
                .addComponent(c2))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(c3)));
        // layout Verticale
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(c1)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(c2)
                .addComponent(c3)));
        // Aggiunto i controlli
        type_setting.add(new AuxJP_regex(type, c2, c3));
        // Aggiungo il pannello a qello interno
        jP_regexInner.add(tmp);
    }
}
/**
 * <p> Ritorna un array ordinato contenente i tipi disponibili a partire da
 * quello indicato. </p>
 *
 * @param max iniziale per creare l'array
 * @return un array contenente i tipi disponibili
 */
protected viewer.manuzioParser.Type[] getOrderType(viewer.manuzioParser.Type max) {
        ArrayList<viewer.manuzioParser.Type> a = new ArrayList<viewer.manuzioParser.Type>();
        int idx = 0;
        if (max.isMaximalUnit()) { // se è maximalUnit inserisco anche lui
            a.add(max);
        } else {                    // se non maximal inserisco solo le sue componenti
            a.addAll(Arrays.asList(max.getComponentTypes()));
        }
        boolean test;
        while (idx < a.size()) {
            viewer.manuzioParser.Type get = a.get(idx);
            if (get.hasComponents()) {
                viewer.manuzioParser.Type[] componentTypes = get.getComponentTypes();
                for (int i = 0; i < componentTypes.length; i++) { // per ogni nuovo type
                    test = true;
                    String s = componentTypes[i].getTypeName();
                    for (int j = 0; j < a.size() && test; j++) {
                        if (s.compareTo(a.get(j).getTypeName()) == 0) {
                            test = false;
                        }
                    }
                    if (test) {
                        a.add(componentTypes[i]);
                    }
                }
            }
            idx++;
        }
        return a.toArray(new viewer.manuzioParser.Type[a.size()]);
    }

    /**
     * <p> Inizializza il pannello
     * <tt>jP_RegexLarge</tt>, in base ai dati ottenuti dal pannello precedente.
     * Il metodo non va chiamato al memento dell'inializzazione del oggetto, ma
     * runtime</p>
     */
    private void initRegexLarge() {
        String path = jFileChooser.getSelectedFile().getPath();
        jTA_confirmOutput.setText("");
        jTA_confirmOutput.append("File:" + " \"" + path + "\"\n\n" + "Type List: \n");
        Iterator<AuxJP_regex> iterator = type_setting.iterator();
        while (iterator.hasNext()) { // scorre tutti gli oggetti inseriti
            AuxJP_regex next = iterator.next();
            JComboBox<String> jComboBox = next.getjComboBox();
            int x = jComboBox.getSelectedIndex();
            jTA_confirmOutput.append(next.getType().getTypeName() + ": ");

            switch (x) {
                case 0:
                    jTA_confirmOutput.append(tab_type[x] + "\n");
                    break;
                case 1:
                    jTA_confirmOutput.append(tab_type[x] + "\n");
                    break;
                case 2:
                    jTA_confirmOutput.append(tab_type[x] + "\n");
                    break;
                case 3:
                    jTA_confirmOutput.append(tab_type[x] + "\n");
                    break;
                case 4:
                    jTA_confirmOutput.append(tab_type[x] + "\n");
                    break;
                case 5:
                    jTA_confirmOutput.append(tab_type[x] + "\n");
                    break;
                case 6:
                    jTA_confirmOutput.append(tab_type[x] + "\n\t" + ((JTextField) next.getJcomponent()).getText() + "\n");
                    break;
            }
        }
    }

    /**
     * <p>Attiva e disattiva i JButton e predispone se necessario le variabili
     * per eseguire i possibili cambiamenti, ma non cambia il pannello da
     * visualizzare. </p>
     *
     * @param name stringa contenete il nome del jpanel da preparare
     */
    private void prepareFirstStepCard(String name) {
        switch (name) {
            case file: // inizializza la finesta 
                currentCard = file;
                taskRawInput = null;
                jB_previous.setEnabled(false);
                jB_next.setEnabled(true);
                jB_next.setText(lang.getString("NEXT"));
                jFileChooser.setSelectedFile(null);
                break;
            case regex:
                currentCard = regex;
                jB_previous.setEnabled(true);
                jB_next.setEnabled(true);
                jB_next.setText(lang.getString("NEXT"));
                break;
            case regexLarge: // Inizializza il 3 pannello per la conferma dei dati
                currentCard = regexLarge;
                jB_previous.setEnabled(true);
                jB_next.setEnabled(true);
                jB_next.setText(lang.getString("CONFIRM"));
                initRegexLarge();
                break;
        }
    }

    /**
     * <p>Una volta raggiunto il pannello jP_regexLarge, inizializza un oggetto
     * responsabile di muoversi attraverso i pannelli e generarli sino a creare
     * una struttura delle query da eseguire per l'inserimeto. </p>
     *
     */
    private SecondStepStrategy prepareSecondStepCard() {
        // creazione struttura dati per l'inserimento
        sss = new SecondStepStrategy(jP_secondStep, idX_to, stringX_to, filetext, jB_previous, jB_next, jB_close);
        Iterator<AuxJP_regex> iterator = type_setting.iterator();
        while (iterator.hasNext()) {
            AuxJP_regex next = iterator.next();
            JComboBox<String> jComboBox = next.getjComboBox();
            int x = jComboBox.getSelectedIndex(); // controllo valore della scelta
            switch (x) {
                case 0:
                    sss.addType(next.getType().getTypeName(), SecondStepStrategy.CHAR);
                    break;
                case 1:
                    sss.addType(next.getType().getTypeName(), SecondStepStrategy.WORD);
                    break;
                case 2:
                    sss.addType(next.getType().getTypeName(), SecondStepStrategy.SENTENCE);
                    break;
                case 3:
                    sss.addType(next.getType().getTypeName(), SecondStepStrategy.PARAGRAPH);
                    break;
                case 4:
                    sss.addType(next.getType().getTypeName(), SecondStepStrategy.ALLTEXT);
                    break;
                case 5:
                    sss.addType(next.getType().getTypeName(), SecondStepStrategy.SELECTEDTEXT);
                    break;
                case 6:
                    sss.addType(next.getType().getTypeName(), SecondStepStrategy.REGULAR_EXP, ((JTextField) next.jcomponent).getText());
                    break;
            }
        }
        return sss;
    }

    @Override
        public void propertyChange(PropertyChangeEvent evt) {
        if ("progress".equals(evt.getPropertyName())) { // Se vero stiamo caricando il file
            if (currentStep.compareTo(firstStep) == 0) {
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
                            CardLayout layout = (CardLayout) jP_firstStep.getLayout();
                            prepareFirstStepCard(regex);
                            layout.next(jP_firstStep);
                        } else {
                            // c'e stato un errore di qualche tipo
                            taskRawInput = null;
                            jB_next.setEnabled(true);
                            jProgressBar.setVisible(false);
                            JOptionPane.showMessageDialog(this, lang.getString("FILE_ERROR_LOADING"), lang.getString("ERROR"), JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
            if (currentStep.compareTo(secondStep) == 0) {
                int progress = (Integer) evt.getNewValue();
                if (progress == 0) {
                    jProgressBar.setIndeterminate(true);
                } else {
                    jProgressBar.setIndeterminate(false);
                    jProgressBar.setValue(progress);
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
        jL_regexLargeTittle = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTA_confirmOutput = new javax.swing.JTextArea();
        jP_secondStep = new javax.swing.JPanel();
        jP_control = new javax.swing.JPanel();
        jB_close = new javax.swing.JButton();
        jB_next = new javax.swing.JButton();
        jB_previous = new javax.swing.JButton();
        jProgressBar = new javax.swing.JProgressBar();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle(lang.getString("ADD")); // NOI18N
        setAlwaysOnTop(true);
        setMinimumSize(new java.awt.Dimension(400, 300));
        setResizable(false);

        cards.setLayout(new java.awt.CardLayout());

        jP_firstStep.setLayout(new java.awt.CardLayout());

        jP_file.setMaximumSize(new java.awt.Dimension(595, 363));
        jP_file.setLayout(new java.awt.BorderLayout());

        jL_fileTittle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jL_fileTittle.setText(lang.getString("FILE_CHOOSE")); // NOI18N
        jL_fileTittle.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(0, 0, 0)));
        jP_file.add(jL_fileTittle, java.awt.BorderLayout.PAGE_START);

        jP_fileInner.setMaximumSize(new java.awt.Dimension(595, 346));

        jFileChooser.setAcceptAllFileFilterUsed(false);
        jFileChooser.setControlButtonsAreShown(false);
        jFileChooser.setCurrentDirectory(null);
        jFileChooser.setFileFilter(new FileNameExtensionFilter(lang.getString("PLAIN_TEXT_FILE"), lang.getString("TXT")));

        org.jdesktop.layout.GroupLayout jP_fileInnerLayout = new org.jdesktop.layout.GroupLayout(jP_fileInner);
        jP_fileInner.setLayout(jP_fileInnerLayout);
        jP_fileInnerLayout.setHorizontalGroup(
            jP_fileInnerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jP_fileInnerLayout.createSequentialGroup()
                .addContainerGap()
                .add(jFileChooser, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 597, Short.MAX_VALUE)
                .addContainerGap())
        );
        jP_fileInnerLayout.setVerticalGroup(
            jP_fileInnerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jP_fileInnerLayout.createSequentialGroup()
                .addContainerGap()
                .add(jFileChooser, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE)
                .addContainerGap())
        );

        jP_file.add(jP_fileInner, java.awt.BorderLayout.CENTER);

        jP_firstStep.add(jP_file, "file");

        jP_regex.setMaximumSize(new java.awt.Dimension(595, 363));
        jP_regex.setMinimumSize(new java.awt.Dimension(595, 363));
        jP_regex.setLayout(new java.awt.BorderLayout());

        jL_regexTittle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jL_regexTittle.setText(lang.getString("ASSOCIATION_MANUZIO'S_TYPE")); // NOI18N
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
            .add(0, 681, Short.MAX_VALUE)
        );
        jP_regexInnerLayout.setVerticalGroup(
            jP_regexInnerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 502, Short.MAX_VALUE)
        );

        jScroll_regex.setViewportView(jP_regexInner);

        jP_regex.add(jScroll_regex, java.awt.BorderLayout.CENTER);

        jP_firstStep.add(jP_regex, "regex");

        jP_regexLarge.setLayout(new java.awt.BorderLayout());

        jL_regexLargeTittle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jL_regexLargeTittle.setText(lang.getString("DATA_CONFIRM")); // NOI18N
        jL_regexLargeTittle.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(0, 0, 0)));
        jP_regexLarge.add(jL_regexLargeTittle, java.awt.BorderLayout.PAGE_START);

        jTA_confirmOutput.setEditable(false);
        jTA_confirmOutput.setColumns(20);
        jTA_confirmOutput.setLineWrap(true);
        jTA_confirmOutput.setRows(5);
        jScrollPane1.setViewportView(jTA_confirmOutput);

        jP_regexLarge.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jP_firstStep.add(jP_regexLarge, "regexLarge");

        cards.add(jP_firstStep, "firstStep");

        jP_secondStep.setMinimumSize(new java.awt.Dimension(595, 363));
        jP_secondStep.setPreferredSize(new java.awt.Dimension(609, 523));
        jP_secondStep.setLayout(new java.awt.BorderLayout());
        cards.add(jP_secondStep, "secondStep");

        getContentPane().add(cards, java.awt.BorderLayout.CENTER);

        jP_control.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(0, 0, 0)));

        jB_close.setText(lang.getString("CANCEL")); // NOI18N
        jB_close.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jB_closeActionPerformed(evt);
            }
        });

        jB_next.setText(lang.getString("NEXT")); // NOI18N
        jB_next.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jB_nextActionPerformed(evt);
            }
        });

        jB_previous.setText(lang.getString("BACK")); // NOI18N
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
                .addContainerGap(322, Short.MAX_VALUE)
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
        CardLayout layout;
        if (currentStep.compareTo(firstStep) == 0) { // Se siamo nella prima fase di selezione
            switch (currentCard) {
                case file:    // Seleziono il file da aggiungere
                    File selectedFile = jFileChooser.getSelectedFile();
                    if (selectedFile == null || !selectedFile.isFile()) {
                        JOptionPane.showMessageDialog(this, lang.getString("FILE_SELECT"), lang.getString("CAUTION"), JOptionPane.WARNING_MESSAGE);
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
                    Iterator<AuxJP_regex> iterator = type_setting.iterator();
                    boolean err = false;
                    boolean allText = false;
                    String notReg = lang.getString("INSERT_REGEX");
                    while (iterator.hasNext() && !err) { // controllo la corretteza di tutti i campi e conto i tipi
                        AuxJP_regex next = iterator.next();
                        // un solo allText
                        JComboBox<String> jComboBox = next.getjComboBox();
                        int x = jComboBox.getSelectedIndex();
                        if (x == 4) { // seleziona tutto il testo
                            if (!allText && this.stringX_to.compareTo(next.getType().getTypeName()) == 0) {
                                allText = true;
                            } else {
                                err = true;
                                JOptionPane.showMessageDialog(this, lang.getString("TEXT_7"), lang.getString("CAUTION"), JOptionPane.WARNING_MESSAGE);
                            }
                        }
                        if (x == 6) { // campi regex
                            JTextField jtf = (JTextField) next.getJcomponent();
                            String text = jtf.getText();
                            if (text.compareTo(notReg) == 0) {
                                err = true;
                                JOptionPane.showMessageDialog(this, lang.getString("CAUTION_FIELDS_INCOMPLETE"), lang.getString("CAUTION"), JOptionPane.WARNING_MESSAGE);
                            }
                        }
                    }
                    if (!allText && !err) {
                        err = true;
                        JOptionPane.showMessageDialog(this, lang.getString("TEXT_8"), lang.getString("CAUTION"), JOptionPane.WARNING_MESSAGE);
                    }

                    if (!err) { // passo al riepilogo
                        layout = (CardLayout) jP_firstStep.getLayout();
                        prepareFirstStepCard(regexLarge);
                        layout.next(jP_firstStep);
                    }
                    break;
                case regexLarge:
                    int x = JOptionPane.showConfirmDialog(this, lang.getString("CONFIRM_DATA"), lang.getString("CONFIRM"), JOptionPane.YES_NO_OPTION);
                    if (x == JOptionPane.YES_OPTION) {
                        this.jB_next.setEnabled(false);
                        this.jB_previous.setEnabled(false);
                        this.jB_close.setEnabled(false);
                        sss = prepareSecondStepCard();
                        sss.start();
                        layout = (CardLayout) cards.getLayout();
                        layout.next(cards);
                        currentStep = secondStep;
                        pack();
                    }
                    break;
                default:
                    break;
            }
        } else if (currentStep.compareTo(secondStep) == 0) {
            if (sss.hasNext()) {
                sss.next();
            } else {
                jProgressBar.setVisible(true);
                jProgressBar.setIndeterminate(true);
                sss.doLoading(this);
            }
        }

    }//GEN-LAST:event_jB_nextActionPerformed

    private void jB_previousActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jB_previousActionPerformed
        CardLayout layout;
        if (currentStep.compareTo(firstStep) == 0) { // Se siamo nella prima fase di selezione
            switch (currentCard) {
                case file: // Impossibile dovrei essere disattivato
                    break;
                case regex: // torno a file
                    layout = (CardLayout) jP_firstStep.getLayout();
                    prepareFirstStepCard(file);
                    layout.previous(jP_firstStep);
                    break;
                case regexLarge:    // 
                    layout = (CardLayout) jP_firstStep.getLayout();
                    prepareFirstStepCard(regex);
                    layout.previous(jP_firstStep);
                    break;
                default:
                    break;
            }
        }
    }//GEN-LAST:event_jB_previousActionPerformed

    private void jB_closeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jB_closeActionPerformed
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
                    this.setVisible(false);
                    break;
                //chiudi possibili task già avviati
                default:
                    break;
            }
        }
        if (currentStep.compareTo(secondStep) == 0) {
            sss.cancel();
            this.setVisible(false);
        }
    }//GEN-LAST:event_jB_closeActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel cards;
    private javax.swing.JButton jB_close;
    private javax.swing.JButton jB_next;
    private javax.swing.JButton jB_previous;
    private javax.swing.JFileChooser jFileChooser;
    private javax.swing.JLabel jL_fileTittle;
    private javax.swing.JLabel jL_regexLargeTittle;
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
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScroll_regex;
    private javax.swing.JTextArea jTA_confirmOutput;
    // End of variables declaration//GEN-END:variables
}
