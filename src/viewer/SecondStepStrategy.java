package viewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JPanel;
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
        private Set<TextType> subType = null;
        /**
         * <p>Eventuale estensione. </p>
         */
        private TextType superType = null;
        /**
         * <p>Padre dell'oggetto. </p>
         */
        private TextType conteiner = null;
        /**
         * <p>Tutto il testo dell oggetto</p>
         */
        private String allText = null;

        public TextType(Type type, TextType conteiner, TextType superType, String allText) {
            this.type = type;
            this.conteiner = conteiner;
            this.superType = superType;
            this.allText = allText;
            this.subType = new HashSet<TextType>();
        }

        public Type getType() {
            return type;
        }

        public TextType[] getSubType() {
            return subType.toArray(new TextType[0]);
        }

        public void addSubType(TextType subType) {
            this.subType.add(subType);
        }

        public TextType getSuperType() {
            return superType;
        }

        public void setSuperType(TextType superType) {
            this.superType = superType;
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
                return al.toArray(new String[0]);
            }
        }
    }
    /**
     * <p>Pannello al quale aggiungere il CardLayout. </p>
     */
    private JPanel cards;
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
     * <p>Varibile di controllo per stabilere la fine della preparazione per
     * inserimento. </p>
     */
    private volatile boolean isEnd;
    /**
     * <p>Nome del tipo dell'oggetto root. </p>
     */
    private String rootTypeName;
    /**
     * <p>Tipo dell'oggetto root. </p>
     */
    private viewer.manuzioParser.Type rootType;
    /**
     * <p>Indice corrente dell'array dei Maxtype. Se descritto come un
     * <tt>SecondStepStrategy.ALLTEXT</tt> la variabile rimarrà <tt>0</tt>.
     * </p>
     */
    private int idxMaxtype = 0;
    /**
     * <p>Indice corrente in un array Maxtype. </p>
     */
    private int idxPostype = 0;
    private LinkedList<TextType> maxTypeList;
    /**
     * <p>Elenco dei tipi inseriti nel oggetto (typeName,Object) per i quali
     * generare delle query. Se Object è un Integer allora parliamo di tutto il
     * testo o selezioni, se String di una espressione regolare. <p>
     */
    private Map<String, Object> typeMap;
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
     * padre un nuovo maxType</p>
     *
     * <p>I parametri relativi a <tt>prev</tt>, <tt>next</tt>, <tt>close</tt>
     * vengo usati sono per semplificare la visualizzazione del layout. </p>
     *
     * @param cards JPanel al quale legare tutti le successive finestre
     * @param idx id del padre
     * @param text il testo da inserire
     * @param prev JButton che esegue <tt>previous()</tt>
     * @param next JButton che esegue <tt>next()</tt>
     * @param close JButton che chiude la finestra
     * @exception NullPointerException
     */
    public SecondStepStrategy(JPanel cards, int idx, String type, ArrayList<String> text, javax.swing.JButton prev, javax.swing.JButton next, javax.swing.JButton close) {
        this.cards = cards;
        this.rootIdx = idx;
        if ((cards == null) || (prev == null) || (next == null) || (close == null)) {
            throw new NullPointerException();
        }
        this.prev = prev;
        this.next = next;
        this.close = close;
        if (idx == AddToServerWizard.COMPLETE_PROCEDURE) {
            this.rootTypeName = ManuzioViewer.schema.getMaximalUnit().getTypeName();
        } else {
            this.rootTypeName = type;
        }
        for (int i = 0; i < text.size(); i++) {
            String get = text.get(i);
            if (!get.endsWith("\n")) {
                get += "\n";
            }
            this.text += text.get(i);
        }
        typeMap = new HashMap<String, Object>();
    }

    /**
     * <p>Prepara la struttura interna a sencoda delle precedenti chiamate ad i
     * vari <tt>addType(...)</tt></p>
     */
    public void start() {
        System.gc();
        maxTypeList = new LinkedList();
        rootType = ManuzioViewer.schema.getType(this.rootTypeName);
        if (!typeMap.containsKey(rootTypeName)) {
            throw new IllegalArgumentException("Missing type");
        }
        Object get = typeMap.get(rootTypeName);
        TextType textType;
        if (get instanceof Integer) {
            // se è intero
            int x = ((Integer) get).intValue();
            if (x == SecondStepStrategy.ALLTEXT) {
                textType = new TextType(rootType, null, null, this.text);
                maxTypeList.add(textType);
            }
        }
        if (get instanceof String) {
            // se stringa (regex automatica)
            // applico le regole regex
            Pattern pattern = Pattern.compile((String) get, Pattern.UNICODE_CHARACTER_CLASS);
            Matcher matcher = pattern.matcher(this.text);
            while (matcher.find()) {
                textType = new TextType(rootType, null, null, matcher.group());
                maxTypeList.add(textType);
            }
        }
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
                typeMap.put(type, "(\\p{Digit}{1,}+\\.\\s){0,1}+\\p{Punct}*\\p{Upper}{1}+[^\\.]*[.]+(\\s+|\\Z)");
                break;
            case SecondStepStrategy.SENTENCE:
                // divide in frasi
                typeMap.put(type, "(\\p{Digit}{1,}+\\.\\s){0,1}+\\p{Punct}*\\p{Upper}{1}+[^\\.]*[.]+(\\s+|\\Z)");
                break;
            case SecondStepStrategy.WORD:
                // primo livello di selezione, al secondo blocco divide le sequenze \w* da \p{Punct}*
                typeMap.put(type, "\\S+"); // in variante a \\p{Punct}*\\w+\\p{Punct}*
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
     * <p>Genera e rende visibile un nuovo pannello. Se è possibile esiste
     * ritorna <tt>TRUE</tt> altrimenti <tt>FALSE</tt>. </p>
     *
     * @return <tt>TRUE</tt> se esite
     */
    public synchronized boolean next() {
        return false;
    }

    /**
     * <p>Genera e rende visibile, se disponibile, il pannello precedente. Se è
     * possibile esiste ritorna <tt>TRUE</tt> altrimenti
     * <tt>FALSE</tt>. </p>
     *
     * @return <tt>TRUE</tt> se esite
     */
    public synchronized boolean previous() {
        return false;
    }

    /**
     * <p>Ritorna l'elenco di query da eseguire per il caricamento nel database.
     * Se la procedura di caricamento dati non è ancora terminata
     * (<tt>next()</tt> ritorna <tt>TRUE</tt>) il risultato sarà
     * <tt>null</tt>. </p>
     *
     * @return un ArrayList di Stringhe
     */
    public synchronized ArrayList<String>[] getResult() {
        return null;
    }
}
