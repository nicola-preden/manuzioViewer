/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package viewer.setting;

import java.util.ListIterator;
import java.util.Properties;

/**
 * <p>Interfaccia che descrive i metodi fondamentali di un nodo della
 * struttura</p> <p>Tutti i dati iseriti vengono restituiti come coppie di
 * valori oppure array di coppie sfruttando la classe
 * <code>java.util.Properties</code></p>
 *
 * @author Nicola Preden, matricola 818578, Facoltà di informatica Ca' Foscari
 * in Venice
 */
public interface NodeSettingInterface extends Comparable<NodeSettingInterface> {

    /**
     * <p>Ritorna il descrittore della classe</p>
     *
     * @return
     */
    String getDesc();

    /**
     * <p>Aggiunge ad un nodo una o più coppie di valori (es. user-->"pippo",
     * password-->"1234").</p>
     *
     * @param Prop <code>java.util.Properties</code> da aggiungere
     * @return
     */
    void addProp(Properties... prop);

    /**
     * <p>Aggiunge una coppia di valori in testa</p>
     *
     * @param prop <code>java.util.Properties</code> da aggingere
     * @return
     */
    void addAtFistOccProp(Properties prop);

    /**
     * <p>Rimuove una coppie di valori</p>
     *
     * @param Prop <code>java.util.Properties</code> da rimuovere
     * @return 
     */
    void removeProp(Properties... prop);

    /**
     * <p>Rimuove l'ultima coppia di valori presente nel nodo</p>
     *
     * @return
     */
    void removeLastProp();

    /**
     * </p>Ritorna un iteratore non modificabile</p>
     *
     * @return <p>Iteratore alla lista</p>
     */
    ListIterator<Properties> readProp();

    /**
     * <p>Ritorna</p>
     * <code>TRUE</code> se il nodo con contiene coppie di valori</p>
     *
     * @return
     */
    boolean isEmpty();

    /**
     * <p>Ritorna il numero di Properties presenti</p>
     *
     * @return
     */
    int size();

    @Override
    String toString();
}
