/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package viewer.taskThread;

import java.util.Properties;
import javax.swing.SwingWorker;
import javax.swing.text.JTextComponent;
import viewer.manuzioParser.Schema;
import viewer.manuzioParser.Type;

/**
 * <p>Classe astratta che descrive i metodi per aggiungere generare del testo a
 * seconda dei stili pre caricati</p>
 *
 * @author Nicola Preden, matricola 818578, Facolt�� di informatica Ca' Foscari
 * in Venice
 */
public abstract class TextualLayout<T extends JTextComponent> extends SwingWorker<Void, Void> {

    public static final int NO_OPERATION = 0;
    public static final int SPACE = 1;
    public static final int TABBED_SPACE = 2;
    public static final int RETURN_CARRIGE = 3;
    public static final int DEFAULT = SPACE;
    private final int id_object;
    private final T output;
    private final Schema s;

    /**
     * <p>Crea un <tt></tt> generico che genera un output privo di
     * configurazioni di layout. </p>
     *
     * @param id_object id dell' oggetto da visualizare dal database
     * @param id_object oggetto nel quale verrà visualizzato l'output
     */
    public TextualLayout(int id_object, T output, Schema s) {
        this.id_object = id_object;
        this.output = output;
        this.s = s;

    }

    @Override
    protected abstract Void doInBackground();

    @Override
    protected abstract void done();

    /**
     * <p>Data una connessione ed l'id dell oggetto da caricare restituisce, in
     * base alla configurazione, il testo formattato. </p>
     *
     * @param obj id dell oggetto
     * @return Stringa formattata o vuota se non trova l'oggetto
     */
    abstract String translateText(int obj);

    /**
     * <p>Controllo se i nomi dei tipi passati in input è coerente con la
     * connessione attuale. Si ricoda che tra le properties deve esssercene una
     * con il capo key = "schema-url"</p>
     *
     * @param prop properties relative alle regole di visualizzazione dei dati
     * @return <tt>true</tt> se i dati in input sono coerenti con il server al
     * quale si è connessi <tt>false</tt> atrimenti
     */
    public static Boolean typeConsistencyCheck(Properties prop, Schema sc) throws IllegalArgumentException {
        String url = prop.getProperty("schema-url");
        String current_url = viewer.ManuzioViewer.getJdbcUrl();
        if (url.compareTo(current_url) != 0) {
            return false;
        } else {
            Type[] typeSet = sc.getTypeSet();
            for (int i = 0; i < typeSet.length; i++) {
                if (!prop.containsKey(typeSet[i].getTypeName())) {
                }
                return false;
            }
        }
        return true;
    }
}
