package viewer;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import viewer.manuzioParser.Attribute;
import viewer.manuzioParser.ComponentProperty;
import viewer.manuzioParser.Type;

/**
 * <p>Classe si occupa di generare dinamicamente, intercambiare dei pannelli per
 * l'inserimento effettivo dei dati. </p>
 */
class SecondStepStrategy {

    class TextType {

        /**
         * <p>Descrittore del schema manuzio per stabilire le relazioni da usare
         * durante l'inserimeto. </p>
         */
        private Type type = null;
        /**
         * <p>Eventuali sottotipo dell'oggetto corrente. <p>
         */
        private ArrayList<TextType> subType = null;
        private Map<String, String> attribute = null;
        /**
         * <p>Padre dell'oggetto. </p>
         */
        private TextType conteiner = null;
        private String componentName = null;
        /**
         * <p>Tutto il testo dell oggetto</p>
         */
        private String allText = null;

        public TextType(Type type, TextType conteiner, String componentName, String allText) {
            this.type = type;
            this.conteiner = conteiner;
            this.allText = allText;
            this.subType = new ArrayList<TextType>();
            this.attribute = new HashMap<String, String>();
            this.componentName = componentName;
        }

        public Type getType() {
            return type;
        }

        public String getComponentName() {
            return componentName;
        }

        public boolean hasSubType() {
            return !subType.isEmpty();
        }

        public TextType[] getSubType() {
            return subType.toArray(new TextType[subType.size()]);
        }

        public void addSubType(TextType subType) {
            subType.setConteiner(this);
            this.subType.add(subType);
        }

        public TextType getConteiner() {
            return conteiner;
        }

        public void setConteiner(TextType conteiner) {
            this.conteiner = conteiner;
        }

        public String[] getAllText() {
            if (this.subType.isEmpty()) { // se non ci sono sottotipi siamo al minimo
                return new String[]{
                    allText
                };
            } else {
                Iterator<TextType> iterator = subType.iterator();
                ArrayList<String> al = new ArrayList();
                while (iterator.hasNext()) {
                    TextType next = iterator.next();
                    String[] text = next.getAllText();
                    al.addAll(Arrays.asList(text));
                }
                return al.toArray(new String[al.size()]);
            }
        }

        /**
         *
         * @param name
         * @return <tt>null</tt> se non esiste
         */
        public String getAttribute(String name) {
            String get = this.attribute.get(name);
            return get;
        }

        public void setAttribute(String name, String attribute) {
            this.attribute.put(name, attribute);
        }
    }
    /**
     * <p>Pannello al quale aggiungere il CardLayout. </p>
     */
    private JPanel cards;
    private JLabel tittle;
    /**
     * <p>Se il gruppo di due pannelli è gia stato completato. </p>
     */
    private boolean process = false;
    private boolean isEnd = false;
    /**
     * <p>Pulsante di controllo per andare al JPanel precedente</p>
     */
    private javax.swing.JButton prev;
    /**
     * <p>Pulsante di controllo per andare al JPanel precedente</p>
     */
    private javax.swing.JButton next;
    /**
     * <p>Pulsante di controllo per andare al JPanel precedente</p>
     */
    private javax.swing.JButton close;
    /**
     * <p>Index dell'oggetto root
     */
    private int rootIdx;
    /**
     * <p>Nome del tipo dell'oggetto root. </p>
     */
    private String rootTypeName;
    /**
     * <p>Testa dell'albero contenente i dati. </p>
     */
    private TextType maxTypeList;
    /**
     * <p>Elenco dei tipi inseriti nel oggetto (typeName,Object) per i quali
     * generare delle query. Se Object è un Integer allora parliamo di tutto il
     * testo o selezioni, se String di una espressione regolare. <p>
     */
    private Map<String, Object> typeMap;
    private LinkedList<TextType> ttl; // coda di texttype da analizzare
    private JPanel lastPanel;
    /**
     * <p>Il testo grezzo da inserire. </p>
     */
    private String text;
    /**
     * <p>Indica tutto il testo caricato è da associare al tipo. </p>
     */
    public static final int ALLTEXT = 1;
    /**
     * <p>Indica che solo una selezione è da associare al testo. </p>
     */
    public static final int SELECTEDTEXT = 2;
    /**
     * <p>Indica che un espressione regolare è da associare al testo. </p>
     */
    public static final int REGULAR_EXP = 3;
    /**
     * <p>Indica che solo una paragrafo è da associare al testo. </p>
     */
    public static final int PARAGRAPH = 4;
    /**
     * <p>Indica che solo una frase è da associare al testo. </p>
     */
    public static final int SENTENCE = 5;
    /**
     * <p>Indica che solo una parola è da associare al testo. </p>
     */
    public static final int WORD = 6;
    /**
     * <p>Indica che solo un carattere è da associare al testo. </p>
     */
    public static final int CHAR = 7;

    /**
     * <p>Costruttore, crea la classe alla quale è necessario passare tra i
     * parametri il JPanel al quale aggiungere un CardLayout. È necessario
     * specificare l'id del oggetto padre. Se <tt>idx ==
     * AddToServerWizard.COMPLETE_PROCEDURE</tt> allora il testo userà come
     * padre un nuovo maxType. </p>
     *
     * <p>I parametri relativi a <tt>prev</tt>, <tt>next</tt>, <tt>close</tt>
     * vengo usati sono per semplificare la visualizzazione del layout. Inoltre
     * il JPanel deve essere già contenere un <tt>java.awt.BorderLayout</tt>.
     * </p>
     *
     * @param cards JPanel al quale legare tutti le successive finestre
     * @param idx id del padre
     * @param type Il nome del tipo root
     * @param text il testo da inserire
     * @param prev JButton che esegue <tt>previous()</tt>
     * @param next JButton che esegue <tt>next()</tt>
     * @param close JButton che chiude la finestra
     * @exception NullPointerException
     */
    public SecondStepStrategy(JPanel cards, int idx, String type, String text, javax.swing.JButton prev, javax.swing.JButton next, javax.swing.JButton close) {
        if ((cards == null) || (prev == null) || (next == null) || (close == null)) {
            throw new NullPointerException();
        }
        if (!(cards.getLayout() instanceof BorderLayout)) {
            throw new IllegalArgumentException(cards.toString() + "Don't have a BorderLayout");
        }
        this.tittle = new JLabel();
        this.tittle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        this.tittle.setText("<html><b>Personalizzazione Dati</b></html>");
        this.tittle.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(0, 0, 0)));
        cards.add(this.tittle, java.awt.BorderLayout.PAGE_START);

        this.cards = new JPanel(new CardLayout());
        this.cards.setMaximumSize(new java.awt.Dimension(595, 346));
        cards.add(this.cards, java.awt.BorderLayout.CENTER);
        this.rootIdx = idx;
        this.prev = prev;
        this.next = next;
        this.close = close;
        if (idx == AddToServerWizard.COMPLETE_PROCEDURE) {
            this.rootTypeName = ManuzioViewer.schema.getMaximalUnit().getTypeName();
        } else {
            this.rootTypeName = type;
        }
        typeMap = new HashMap<String, Object>();
        this.ttl = new LinkedList<TextType>();
    }

    /**
     * <p>Prepara la struttura interna a sencoda delle precedenti chiamate ad i
     * vari <tt>addType(...)</tt>. </p>
     *
     * @throws IllegalArgumentException
     */
    public void start() {
        System.gc();
        this.prev.setEnabled(false);
        this.next.setText("Avanti");
        this.close.setEnabled(true);
        this.next.setEnabled(true);
        Type rootType = ManuzioViewer.schema.getType(this.rootTypeName);
        if (!typeMap.containsKey(rootTypeName)) {
            throw new IllegalArgumentException("Missing type");
        }
        Object get = typeMap.get(rootTypeName);
        TextType textType;
        if (!(get instanceof Integer)) {
            throw new IllegalArgumentException("Missing type");
        }

        int x = ((Integer) get).intValue();
        // controllo coerenza tipi
        if (x == SecondStepStrategy.ALLTEXT) {
            textType = new TextType(rootType, null, null, this.text);
            this.maxTypeList = textType;
        } else {
            throw new IllegalArgumentException("Missing type");
        }

        this.isEnd = autoScan(this.maxTypeList); // cerco di minimizzare già il testo


        if (!this.isEnd) { // è necessario gestire dei sotto tipi
            this.printPaneSet(this.maxTypeList);
            ttl.add(this.maxTypeList);
            CardLayout lay = (CardLayout) cards.getLayout();
            lay.first(cards);
            this.process = true; // Ci stanno dati da processare in coda
        } else {
            // non ci stanno dei sotto tipi da gestire
        }

    }

    /**
     * <p>Disegna un pannello atto a visualizzare attributi e sotto-tipi
     * correnti. </p>
     *
     * @param tx TextType corrente
     */
    private void printPaneSet(TextType tx) {
        JPanel pane = new JPanel();
        lastPanel = pane;
        pane.setPreferredSize(cards.getPreferredSize());
        pane.setMaximumSize(cards.getMaximumSize());
        pane.setMinimumSize(cards.getMinimumSize());
        GridBagLayout experimentLayout = new GridBagLayout();
        GridBagConstraints c;
        pane.setLayout(experimentLayout);
        JLabel l;

        int i = 0;
        l = new JLabel("<HTML><p><b>Componente: " + (tx.getComponentName() == null ? "ROOT" : tx.getComponentName()) + " Tipo: " + tx.getType().getTypeName() + "</b></p></HTML>");
        l.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.gridx = 0;
        c.gridwidth = 3;
        c.gridy = i;
        pane.add(l, c);
        i++;
        if (tx.getType().hasComponents()) {
            //<editor-fold defaultstate="collapsed" desc="Inserimento Componenti">
            ComponentProperty[] components = tx.getType().getComponents();
            l = new JLabel("<HTML><p><b>Specificare il numero di componenti</b></p></HTML>");
            l.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.LINE_START;
            c.gridx = 0;
            c.gridwidth = 3;
            c.gridy = i;
            pane.add(l, c);
            i++;
            // nomi colonne
            c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.LINE_START;
            c.gridx = 0;
            c.gridy = i;
            l = new JLabel("Optionale");
            l.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            pane.add(l, c);
            c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.CENTER;
            c.gridx = 1;
            c.gridy = i;
            l = new JLabel("Componente : tipo");
            l.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            pane.add(l, c);
            c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.LINE_END;
            c.gridx = 2;
            c.gridy = i;
            l = new JLabel("Valore");
            l.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            pane.add(l, c);

            i++;
            // Inserimento Componenti
            for (ComponentProperty prop : components) {
                JCheckBox c1 = new JCheckBox();
                c1.setName(prop.getComponentName() + "_checkBox");
                if (prop.isOptional()) {
                    c1.setSelected(false);
                    c1.setEnabled(true);
                } else {
                    c1.setSelected(true);
                    c1.setEnabled(false);
                }
                c1.setText(null);
                c1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                JLabel c2 = new JLabel();
                c2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                c2.setName(prop.getComponentName() + "_label");
                c2.setText(prop.getComponentName() + " : " + (prop.isPlural() ? prop.getComponent().getPluralName() : prop.getComponent().getTypeName()));
                JTextField c3 = new JTextField();
                c3.setName(prop.getComponentName() + "_textField");
                c3.setToolTipText("Numero di oggetti da caricare");
                c3.setText("AUTO");
                c3.setEnabled(false);
                c = new GridBagConstraints();
                c.fill = GridBagConstraints.HORIZONTAL;
                c.anchor = GridBagConstraints.LINE_START;
                c.gridx = 0;
                c.gridy = i;
                pane.add(c1, c);
                c = new GridBagConstraints();
                c.fill = GridBagConstraints.HORIZONTAL;
                c.anchor = GridBagConstraints.CENTER;
                c.gridx = 1;
                c.gridy = i;
                pane.add(c2, c);
                c = new GridBagConstraints();
                c.fill = GridBagConstraints.HORIZONTAL;
                c.anchor = GridBagConstraints.LINE_END;
                c.gridx = 2;
                c.gridy = i;
                pane.add(c3, c);
                i++;
            }
            //</editor-fold>
        }
        if (tx.getType().hasAt(Type.number.SINGULAR)) {
            //<editor-fold defaultstate="collapsed" desc="Inserimento Attributi">
            Attribute[] ownAt = tx.getType().getAt(Type.number.SINGULAR);
            c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.LINE_START;
            c.gridx = 0;
            c.gridwidth = 3;
            c.gridy = i;
            l = new JLabel("<HTML><p><b>Specificare gli attributi</b></p></HTML>");
            l.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            pane.add(l, c);
            i++;
            // Nomi colonne
            c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.LINE_START;
            c.gridx = 0;
            c.gridy = i;
            l = new JLabel("Attributo : Tipo");
            l.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            pane.add(l, c);
            c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.CENTER;
            c.gridx = 1;
            c.gridy = i;
            l = new JLabel("Valore");
            l.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            pane.add(l, c);
            i++;

            for (Attribute att : ownAt) {
                JLabel c1 = new JLabel(att.getAtName() + " : " + att.getAtType());
                c1.setName(att.getAtName() + "_label");
                JTextField c2 = new JTextField();
                c2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                c2.setToolTipText("Valore dell'attributo");
                c2.setName(att.getAtName() + "__textField");
                c = new GridBagConstraints();
                c.fill = GridBagConstraints.HORIZONTAL;
                c.anchor = GridBagConstraints.LINE_START;
                c.gridx = 0;
                c.gridy = i;
                pane.add(c1, c);
                c = new GridBagConstraints();
                c.fill = GridBagConstraints.HORIZONTAL;
                c.anchor = GridBagConstraints.CENTER;
                c.gridx = 1;
                c.gridy = i;
                pane.add(c2, c);
                i++;
            }
            //</editor-fold>
        }
        // genero primo pannello
        cards.add(pane, tx.getType().getTypeName());
    }

    private void printPaneGet(TextType pollFirst) {
        JPanel pane = new JPanel();
        pane.setPreferredSize(cards.getPreferredSize());
        pane.setMaximumSize(cards.getMaximumSize());
        pane.setMinimumSize(cards.getMinimumSize());
        GridBagLayout experimentLayout = new GridBagLayout();
        GridBagConstraints c;
        pane.setLayout(experimentLayout);
        
    }

    /**
     * <p>Esegue una scansione automatica dei figli dell'albero. </p>
     *
     * @param tx il nodo da analizzare
     * @return ritona <tt>false</tt> se c'e un figlio ancora da analizzare
     */
    private boolean autoScan(TextType tx) {
        if (tx == null) {
            return true;
        }
        Type type = tx.getType();
        if (type.isMinimalUnit()) {
            return true;
        } else {
            ComponentProperty[] components = type.getComponents();
            boolean autoScan = true;

            if (components == null) {
                return true;
            }
            if (type.hasAt(Type.number.SINGULAR)) {
                return false;
            }
            if (components.length == 1) {
                if (!components[0].isOptional()) {
                    String componentName = components[0].getComponentName();
                    Type component = components[0].getComponent();
                    Object get = typeMap.get(component.getTypeName());
                    if (!(get instanceof String)) { // se il sotto tipo richiede un input manuale
                        return false;
                    }
                    if (!component.isMinimalUnit()) { // se non è minimo
                        Pattern pattern = Pattern.compile((String) get, Pattern.UNICODE_CHARACTER_CLASS);
                        Matcher matcher = pattern.matcher(tx.allText);
                        while (matcher.find()) {
                            TextType textType = new TextType(component, tx, componentName, matcher.group());
                            if (!autoScan(textType)) { // in casp ci stiano altri tipi non minimizzati un questo ramo
                                autoScan = false;
                            }
                            // chiamata ricorsiva
                            tx.addSubType(textType);
                        }
                        return autoScan;
                    } else { // se il tipo è minimo si divide anche la punteggiatura
                        Pattern min = Pattern.compile((String) get, Pattern.UNICODE_CHARACTER_CLASS);
                        Matcher m = min.matcher(this.text);
                        Pattern min_simple = Pattern.compile("(\\p{Punct})|(\\p{Alnum}+)|(\\p{Punct})", Pattern.UNICODE_CHARACTER_CLASS);
                        Matcher r;
                        while (m.find()) {
                            String t = m.group();
                            r = min_simple.matcher(t);
                            while (r.find()) {
                                TextType textType = new TextType(component, tx, componentName, r.group());
                                tx.addSubType(textType);
                            }
                        }
                        return true;
                    }
                } else {
                    return false;
                }
            } else { // se ha più componeneti
                return false;
            }
        }
    }

    /**
     * <p>Agginge un nuovo tipo specificandone il tipo attraverso le costanti
     * della classe. </p>
     * <ul>
     * <li><tt>SecondStepStrategy.ALLTEXT</tt></li>
     * <li><tt>SecondStepStrategy.SELECTEDTEXT</tt></li>
     * <li><tt>SecondStepStrategy.PARAGRAPH</tt></li>
     * <li><tt>SecondStepStrategy.SENTENCE</tt></li>
     * <li><tt>SecondStepStrategy.WORD</tt></li>
     * <li><tt>SecondStepStrategy.CHAR</tt></li>
     * </ul>
     * <p>In caso di espressioni regolari personalizzate usare il medodo con 3
     * parametri. </p>
     *
     * @param type nome del tipo di textual Object da inserire, preferibilmente
     * singolare
     * @param commontype constante da associare al tipo
     */
    public void addType(String type, int commontype) {
        addType(type, commontype, null);
    }

    /**
     * <p>Agginge un nuovo tipo specificandone il tipo attraverso le costanti
     * della classe. </p>
     * <ul>
     * <li><tt>SecondStepStrategy.ALLTEXT</tt></li>
     * <li><tt>SecondStepStrategy.SELECTEDTEXT</tt></li>
     * <li><tt>SecondStepStrategy.REGULAR_EXP</tt></li>
     * <li><tt>SecondStepStrategy.PARAGRAPH</tt></li>
     * <li><tt>SecondStepStrategy.SENTENCE</tt></li>
     * <li><tt>SecondStepStrategy.WORD</tt></li>
     * <li><tt>SecondStepStrategy.CHAR</tt></li>
     * </ul>
     * <p>Questo metodo è consigliato usarlo solo nel caso il tipo sia
     * identificabile solo attraverso espressioni regolari</p>
     *
     * @param type nome del tipo di textual Object da inserire, preferibilmente
     * singolare
     * @param commontype constante da associare al tipo
     * @param regex espressione regolare personalizzata
     */
    public void addType(String type, int commontype, String regex) {
        if (commontype == SecondStepStrategy.REGULAR_EXP && regex == null) {
            throw new IllegalArgumentException();
        }
        switch (commontype) {
            case SecondStepStrategy.ALLTEXT:
                typeMap.put(type, new Integer(SecondStepStrategy.ALLTEXT));
                break;
            case SecondStepStrategy.SELECTEDTEXT:
                typeMap.put(type, new Integer(SecondStepStrategy.SELECTEDTEXT));
                break;
            case SecondStepStrategy.REGULAR_EXP:
                typeMap.put(type, regex);
                break;
            case SecondStepStrategy.PARAGRAPH:
                // divide in paragrafi
                typeMap.put(type, "(\\p{Digit}{1,}+\\.\\s){0,1}+\\p{Punct}*\\p{Upper}{1}+[^\\.].*(\\n|\\r|\\z)");
                break;
            case SecondStepStrategy.SENTENCE:
                // divide in frasi
                typeMap.put(type, "(\\p{Digit}{1,}+\\.\\s){0,1}+\\p{Punct}*\\p{Upper}{1}+[^.]+([. ]|\\z)");
                break;
            case SecondStepStrategy.WORD:
                // primo livello di selezione, al secondo blocco divide le sequenze \w* da \p{Punct}*
                typeMap.put(type, "\\p{Graph}+"); // in variante a \\p{Punct}*\\w+\\p{Punct}*
                break;
            case SecondStepStrategy.CHAR:
                // divide in caratteri
                typeMap.put(type, "."); // uno qualsiasi
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * <p>Genera e rende visibile un nuovo pannello. Se è possibile ritorna
     * <tt>TRUE</tt> altrimenti <tt>FALSE</tt>. </p>
     *
     * @return <tt>TRUE</tt> se esite
     */
    public synchronized void next() {
        if (!this.isEnd) {
            if (this.process) { // ci stanno dati da elaborare
                // caricamento dati
                TextType pollFirst = this.ttl.pollFirst();
                // genero pannello sucessivo
                printPaneGet(pollFirst);
                this.process = false;
            } else { // genero in nuovo pannello iniziale per il prossimo oggetto
                TextType peekFirst = this.ttl.peekFirst();
                this.printPaneSet(peekFirst); // se peekFirst è null imposta la finiestra per la fine
                CardLayout layout = (CardLayout) cards.getLayout();
                layout.next(cards);
                if (peekFirst == null) {
                    this.isEnd = true;
                }
                this.process = true;
            }
        }
    }
    
    /**
     * <p>Specifica se è disponibile un nuovo oggetto. </p>
     * @return <tt>true</tt> se c'e un nuovo oggetto <tt>false</tt> altrimenti
     */
    public synchronized boolean hasNext() {
        return !this.isEnd;
    }

    /**
     * <p>Lancia i thread che eseguiranno effettivamente le query. Se la
     * procedura di caricamento dati non è ancora terminata (<tt>next()</tt>
     * ritorna <tt>TRUE</tt>) il risultato sarà
     * <tt>null</tt>. </p>
     *
     * @param windows finestra usata per la procedura di caricamento
     */
    public synchronized <JFrame extends PropertyChangeListener> void doLoading(JFrame windows) {
        if (this.isEnd) {
            // lancio il processo che si occuperò di caricare i dati
        }
    }
}
