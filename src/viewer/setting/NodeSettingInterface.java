/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package viewer.setting;

import java.util.Iterator;
import java.util.Properties;

/**
 * <p>Interfaccia che descrive i metodi fondamentali di un nodo della struttura</p>
 * <p>Tutti i dati iseriti vengono restituiti come coppie di valori oppure array di coppie
 * sfruttando la classe <code>java.util.Properties</code></p>
 * @author Nicola Preden, matricola 818578, Facoltà di informatica Ca' Foscari
 * in Venice
 */
interface NodeSettingInterface extends Comparable<NodeSettingInterface> {
    /**
     *<p>Ritorna il descrittore della classe</p>
     * @return 
     */
    String getDesc();
    /**
     * <p>Aggiunge ad un nodo una o più coppie di valori (es. user-->"pippo", password-->"1234").</p>
     * @param Prop
     * @return <code>true</code> in caso di successo altrimenti <code>false</code>
     */
    boolean addProp(Properties ... prop);
    /**
     * <p>Aggiunge una coppia di valori in testa</p>
     * @param pro
     * @return 
     */
    boolean addAtFistOccProp(Properties pro);
    /**
     * <p>Rimuove una o più coppie di valori</p>
     * @param Prop
     * @return <code>true</code> in caso di successo altrimenti <code>false</code>
     */
    boolean removeProp(Properties ... prop);
    /**
     * <p>Rimuove l'ultima coppia di valori presente nel nodo</p>
     * @return 
     */
    boolean removeLastProp();
    /**
     * </p>Ritorna un iteratore non modificabile</p>
     * @return <code>Iteratore non modificabile</code>
     */
    Iterator<Properties> readProp();
}
