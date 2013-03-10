/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
     * <p>Salva la configurazione del pannella attuale. </p>
     */
    void savePreferences();

    /**
     * <p>Resetta l'attuale configurazione ai settaggi di default.</p>
     * <p>Questo medodo non si applica in tutti i casi. In alcuni casi un
     * pannello potrebbe essere sprovvisto di settaggi di defaout. </p>
     */
    void resetToDefaultPreferences();

    /**
     * <p>Ritorna lo status del atttuale pannello e se ha subito modifiche. </p>
     *
     * @return <code>TRUE</code> se si sono apportate modifiche
     */
    boolean isModified();
}
