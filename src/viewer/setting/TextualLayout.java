/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package viewer.setting;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * <p>Classe astratta che descrive i metodi per aggiungere generare del testo a
 * seconda dei stili pre caricati</p>
 *
 * @author Nicola Preden, matricola 818578, Facoltà di informatica Ca' Foscari
 * in Venice
 */
public abstract class TextualLayout {

    Map<String,Integer> setting;
    public static final int SPACE = 1;
    public static final int TABBED_SPACE = 2;
    public static final int RETURN_CARRIGE = 3;
    public static final int DEFAULT = SPACE;
    private static final String DEFAULT_STRING = "default";

    /**
     * <p>Crea un <tt></tt> generico che genera un output privo di
     * configurazioni di layout. </p>
     */
    private TextualLayout() {
        setting = new HashMap<String,Integer>();
        setting.put(DEFAULT_STRING, DEFAULT);
        
    }

    /**
     * <p>Inizializza l'oggetto in base alla configurazione di sistema. </p>
     *
     * @param setting
     */
    private TextualLayout(Properties ... props) {
        for (Properties prop : props) {
           
        }
    }

    /**
     * <p>Crea un nuovo oggetto. </p>
     * @param prop se <tt>null</tt> ritorna il costruttore di default
     * @return 
     */
    public TextualLayout createTextualLayout(Properties prop) {
        return new TextualLayout() {

            @Override
            public String translateText(Connection conn, int obj) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public boolean changeStyle(String url, String nameDB, Properties prop) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public boolean removeStyle(String url, String nameDB) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
    }

    /**
     * <p>Data una connessione ed l'id dell oggetto da caricare restituisce, in
     * base alla configurazione, il testo formattato. </p>
     *
     * @param conn connesione al DB
     * @param obj id dell oggetto
     * @return Stringa formattata o vuota se non trova l'oggetto
     */
    public abstract String translateText(Connection conn, int obj);

    /**
     * <p>Data una descrizione dei vincoli di visualizzazione la aggiunge alle
     * impostazioni. I parametri per identificare il sever devono essere
     * coerenti con quelli delle connessioni. </p>
     *
     * @param url indirizzio del database
     * @param nameDB nome del database
     * @param prop parametri di configurazione
     * @return <tt>true</tt> se ha successo
     */
    public abstract boolean changeStyle(String url, String nameDB, Properties prop);

    /**
     * <p>Data l'identificatvo di un database lo cancella dalla lista. I
     * parametri per identificare il sever devono essere coerenti con quelli
     * delle connessioni. NON ANCORA IMPLEMENTATA</p>
     *
     * @param url indirizzio del database
     * @param nameDB nome del database
     * @return <tt>true</tt> se ha successo
     */
    public abstract boolean removeStyle(String url, String nameDB);
}
