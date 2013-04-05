/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package viewer.setting;

import java.sql.Connection;
import java.util.Properties;

/**
 * <p>Classe astratta che descrive i metodi per aggiungere generare del testo a
 * seconda dei stili pre caricati</p>
 *
 * @author Nicola Preden, matricola 818578, Facoltà di informatica Ca' Foscari
 * in Venice
 */
public abstract class TextualLayout {
    
    SettingXML setting;

    /**
     * <p>Inizializza l'oggetto in base alla configurazione di sistema. </p>
     * @param setting 
     */
    public TextualLayout(SettingXML setting) {
        this.setting = setting;
    }
   
    /**
     * <p>Data una connessione ed l'id dell oggetto da caricare restituisce,
     *  in base alla configurazione, il testo formattato. </p>
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
    public abstract boolean addStyle(String url, String nameDB, Properties prop);

    /**
     * <p>Data l'identificatvo di un database lo cancella dalla lista. I
     * parametri per identificare il sever devono essere coerenti con quelli
     * delle connessioni. </p>
     *
     * @param url indirizzio del database
     * @param nameDB nome del database
     * @return <tt>true</tt> se ha successo
     */
    public abstract boolean removeStyle(String url, String nameDB);
}
