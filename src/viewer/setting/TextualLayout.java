/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package viewer.setting;

import java.sql.Connection;
import javax.swing.text.JTextComponent;

/**
 * <p>Classe astratta che descrive i metodi per aggiungere generare del testo a
 * seconda dei stili pre caricati</p>
 *
 * @author Nicola Preden, matricola 818578, Facolt�� di informatica Ca' Foscari
 * in Venice
 */
public abstract class TextualLayout<T extends JTextComponent> extends Thread {

    public static final int NO_OPERATION = 0;
    public static final int SPACE = 1;
    public static final int TABBED_SPACE = 2;
    public static final int RETURN_CARRIGE = 3;
    public static final int DEFAULT = SPACE;
    private final int id_object;
    private final T output;

    /**
     * <p>Crea un <tt></tt> generico che genera un output privo di
     * configurazioni di layout. </p>
     * @param id_object id dell' oggetto da visualizare dal database
     * @param id_object oggetto nel quale verrà visualizzato l'output
     */
    private TextualLayout(int id_object, T output) {
        this.id_object = id_object;
        this.output = output;
    }
    
    @Override
    public abstract void run();
    
    /**
     * <p>Data una connessione ed l'id dell oggetto da caricare restituisce, in
     * base alla configurazione, il testo formattato. </p>
     *
     * @param conn connesione al DB
     * @param obj id dell oggetto
     * @return Stringa formattata o vuota se non trova l'oggetto
     */
    public abstract String translateText(Connection conn, int obj);
}
