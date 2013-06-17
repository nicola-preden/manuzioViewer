/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package viewer.taskThread;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.SwingWorker;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import viewer.manuzioParser.Schema;
import viewer.manuzioParser.Type;

/**
 * <p>Classe astratta che descrive i metodi per aggiungere generare del testo a
 * seconda dei stili pre caricati</p>
 *
 * @author Nicola Preden, matricola 818578, Facolt�� di informatica Ca' Foscari
 * in Venice
 */
public class TextualLayout<T extends JEditorPane> extends SwingWorker<Void, Void> {

    /**
     * <p>Classe ausiliaria contenete il testo letto dal database. </p>
     */
    private class DataObject {

        int key;
        int operation;
        DataObject[] keys;
        String text;

        public DataObject(int key, int operation, String text) {
            this.key = key;
            this.operation = operation;
            this.text = text;
            if (text == null) {
                this.text = "";
            }
        }

        void setKeys(DataObject[] keys) {
            this.keys = keys;
        }

        int getKey() {
            return key;
        }

        DataObject[] getKeys() {
            return keys;
        }

        @Override
        public String toString() {
            String k = "";
            // Crea il testo ricorsivamente in postordine
            if (keys != null) {
                for (int i = 0; i < keys.length; i++) {
                    k = k + keys[i].toString();
                }
            }
            switch (key) {
                case NO_OPERATION:
                    k += this.text;
                    break;
                case SPACE:
                    k += this.text + " ";
                    break;
                case TABBED_SPACE:
                    k += this.text + "\t";
                    break;
                case RETURN_CARRIGE:
                    k += this.text + "\n";
                    break;
                default:
                    break;
            }
            return k;
        }
    }
    public static final int NO_OPERATION = 0;
    public static final int SPACE = 1;
    public static final int TABBED_SPACE = 2;
    public static final int RETURN_CARRIGE = 3;
    public static final int DEFAULT = SPACE;
    private int id_object;
    private T output;
    private Schema s;
    private Properties prop;
    private int progress;
    private int max;

    /**
     * <p>Costruttore</p>
     *
     * @param id_object
     * @param output
     * @param s
     * @param prop
     */
    protected TextualLayout(int id_object, T output, Schema s, Properties prop) {
        this.id_object = id_object;
        this.output = output;
        this.s = s;
        this.prop = prop;
        this.progress = 0;
        this.max = 0;
    }

    protected void updateProgress(Object x) {
        try {
            if (x == null) {
                progress++;
                int intValue = this.getProgress();
                int k = 100 * progress / max;
                if (intValue != k) {
                    setProgress(k);
                }
                Document document = output.getDocument();
                Element rootElem = document.getDefaultRootElement();
                int numLines = rootElem.getElementCount();
                Element lineElem = rootElem.getElement(numLines - 2);
                int lineStart = lineElem.getStartOffset();
                int lineEnd = lineElem.getEndOffset();
                document.remove(lineStart, lineEnd - lineStart);
                document.insertString(document.getLength(), "In corso ... " + progress + " su " + max + "\n", null);
            }
            if (x instanceof String) {
                Document document = output.getDocument();
                document.insertString(document.getLength(), (String) x + "\n", null);
                document.insertString(document.getLength(), "In corso ... \n", null);
                setProgress(0);
            }
        } catch (BadLocationException ex) {
            Logger.getLogger(TextualLayout.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected Void doInBackground() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet resultSet = null;
        CallableStatement function = null;
        
        return null;

    }

    @Override
    protected void done() {
    }

    /**
     * <p>Data una connessione ed l'id dell oggetto da caricare restituisce, in
     * base alla configurazione, il testo formattato. </p>
     *
     * @param obj id dell oggetto
     * @return Stringa formattata o vuota se non trova l'oggetto
     */
    protected String translateText(int obj) {
        return null;

    }

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
        if (!prop.containsKey("schema-url")) {
            throw new IllegalArgumentException("MIssing Data : schema-url" + prop.toString());
        }
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
