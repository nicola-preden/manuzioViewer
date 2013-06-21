package viewer.setting;

/**
 * <p>Questa interfaccia descrive due possibili metodi per salvare le
 * configurazioni dei pannello o per tornare alla configurazione di default.
 * </p>
 *
 * @author Nicola Preden, matricola 818578, Facoltà di informatica Ca' Foscari
 * in Venice
 */
public interface PreferencesPane {

    /**
     * <p>Configura il pannello in base alla configurazione atuale. </p>
     * @param node 
     */
    void setPreferences(NodeSettingInterface node);
    
    /**
     * <p>Rinuove dalle preferenze le configurazioni associate alle striga 
     * passata in input.</p>
     * @param url Stringa che identifica le configuraioni
     */
    void removePreference(String url);

    /**
     * <p>Ritorna lo status del atttuale pannello e se ha subito modifiche. </p>
     *
     * @return <code>TRUE</code> se si sono apportate modifiche
     */
    boolean isModified();
}
