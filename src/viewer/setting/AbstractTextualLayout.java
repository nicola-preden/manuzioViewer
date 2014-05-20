package viewer.setting;

import java.util.Properties;
import java.util.ResourceBundle;
import javax.swing.JEditorPane;
import javax.swing.SwingWorker;
import viewer.ManuzioViewer;
import viewer.manuzioParser.Schema;
import viewer.manuzioParser.Type;

/**
 *
 * @author Nicola Preden, matricola 818578, Facoltà di informatica Ca' Foscari
 * in Venice
 */
public abstract class AbstractTextualLayout<T extends JEditorPane> extends SwingWorker<Void, Void> {

    public static final int NO_OPERATION = 0;
    public static final int SPACE = 1;
    public static final int TABBED_SPACE = 2;
    public static final int RETURN_CARRIGE = 3;
    public static final int DEFAULT = SPACE;
    private static final ResourceBundle lang = ResourceBundle.getBundle("viewer/language/lang", ManuzioViewer.LANGUAGE);
    public static final String[] LIST_OPERATION = {lang.getString("NO_ONE"), lang.getString("SPACE"), lang.getString("TAB"), lang.getString("RETURN_CARRIAGE")};

    /**
     *
     * @param x stringa da scrivere o null per progressione
     * @param b ultimo testo
     */
    protected abstract void updateProgress(Object x, Boolean b);

    /**
     * <p>Dato l'id dell'oggetto da caricare restituisce, in base alla
     * configurazione, la struttura dell'oggetto contenente il testo attraverso
     * una serie di chiamate ricorsive. </p>
     *
     * @param obj id dell'oggetto da caricare
     * @return Stringa formattata o vuota se non trova l'oggetto
     */
    protected abstract String translateText(int obj);

    /**
     * <p>Controllo se i nomi dei tipi passati in input è coerente con la
     * connessione attuale. Si ricorda che tra le properties deve essere presente una
     * con il capo key = "schema-url"</p>
     *
     * @param prop properties relative alle regole di visualizzazione dei dati
     * @return <tt>true</tt> se i dati in input sono coerenti con il server al
     * quale si è connessi <tt>false</tt> altrimenti
     */
    public static Boolean typeConsistencyCheck(Properties prop, Schema sc) throws IllegalArgumentException {
        if (prop == null || sc == null) {
            return false;
        }
        if (!prop.containsKey("schema-url")) {
            throw new IllegalArgumentException("MIssing Data : schema-url" + prop.toString());
        }
        String url = prop.getProperty("schema-url");
        String current_url = ManuzioViewer.getJdbcUrl();
        if (url.compareTo(current_url) != 0) {
            return false;
        } else {
            Type[] typeSet = sc.getTypeSet();
            for (int i = 0; i < typeSet.length; i++) {
                if (!prop.containsKey(typeSet[i].getTypeName())) {
                    return false;
                }
            }
        }
        return true;
    }
}
