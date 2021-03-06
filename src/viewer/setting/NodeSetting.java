package viewer.setting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ListIterator;
import java.util.Properties;

/**
 * Implementazione dell'interfaccia
 * <tt>viewer.setting.NodeSettingInterface</tt>
 *
 * @author Nicola Preden, matricola 818578, Facoltà di informatica Ca' Foscari
 * in Venice
 */
class NodeSetting implements NodeSettingInterface {

    private String desc;
    private ArrayList<Properties> ls;

    NodeSetting(String desc, Properties... prop) {
        this.desc = desc;
        if (prop != null) {
            ls = new ArrayList<Properties>();
            for (Properties p : prop) {
                ls.add(p);
            }
        }

    }

    @Override
    public String getDesc() {
        return this.desc;
    }

    @Override
    synchronized public void addProp(Properties... prop) {
        for (Properties p : prop) { // Si ipotizza che i prop confrontati contengano dati omogenei, quindi abbiano lo stesso numero di campi
            boolean test;
            ArrayList ar = Collections.list(p.propertyNames());
            ListIterator<Properties> li = ls.listIterator();
            while (li.hasNext()) {
                Properties get = li.next();
                test = true;
                for (int j = 0; j < ar.size() && test; j++) {
                    String s = ar.get(j).toString();
                    String k = get.getProperty(s);
                    if (k == null) {    // Se NULL allora non è da cancellare, controllo superfluo ma necessario
                        test = false;                                         // perchè essendo oggetti omogenei dovrebbero avere anche gli stessi campi ma non si sa mai!!!
                    } else {
                        if (k.compareTo(p.getProperty(s)) != 0) { // Se i due compi value sono  uguali test non vien modificato
                            test = false;
                        }
                    }
                }
                if (test) {             // se test è true allora la Properties eseste già
                    return;
                }
            }
            ls.add(p);
        }
        /*
         for (Properties p : prop) {
         ls.add(p);
         }*/
    }

    @Override
    synchronized public void addAtFistOccProp(Properties pro) {
        boolean test;
        ArrayList ar = Collections.list(pro.propertyNames());
        ListIterator<Properties> li = ls.listIterator();
        while (li.hasNext()) {
            Properties get = li.next();
            test = true;
            for (int j = 0; j < ar.size() && test; j++) {
                String s = ar.get(j).toString();
                String k = get.getProperty(s);
                if (k == null) {    // Se NULL allora non è da cancellare, controllo superfluo ma necessario
                    test = false;                                         // perchè essendo oggetti omogenei dovrebbero avere anche gli stessi campi ma non si sa mai!!!
                } else {
                    if (k.compareTo(pro.getProperty(s)) != 0) { // Se i due compi value sono  uguali test non vien modificato
                        test = false;
                    }
                }
            }
            if (test) {             // se test è true allora la Properties eseste già
                return;
            }
        }
        ls.add(0, pro);
    }

    @Override
    synchronized public void removeProp(Properties... prop) {
        for (Properties p : prop) { // Si ipotizza che i prop confrontati contengano dati omogenei, quindi abbiano lo stesso numero di campi
            boolean test;
            ArrayList ar = Collections.list(p.propertyNames());
            ListIterator<Properties> li = ls.listIterator();
            while (li.hasNext()) {
                Properties get = li.next();
                test = true;
                for (int j = 0; j < ar.size() && test; j++) {
                    String s = ar.get(j).toString();
                    String k = get.getProperty(s);
                    if (k == null) {    // Se NULL allora non è da cancellare, controllo superfluo ma necessario
                        test = false;                                         // perchè essendo oggetti omogenei dovrebbero avere anche gli stessi campi ma non si sa mai!!!
                    } else {
                        if (k.compareTo(p.getProperty(s)) != 0) { // Se i due compi value sono  uguali test non vien modificato
                            test = false;
                        }
                    }
                }
                if (test) {
                    li.remove();
                    return;
                }
            }
        }
    }

    @Override
    synchronized public void removeLastProp() {
        ls.remove(ls.size() - 1);
    }

    @Override
    synchronized public ListIterator<Properties> readProp() {
        return Collections.unmodifiableList(ls).listIterator();

    }

    @Override
    public int compareTo(NodeSettingInterface o) {
        return desc.compareTo(o.getDesc());
    }

    @Override
    public boolean isEmpty() {
        return this.ls.isEmpty();
    }

    @Override
    public int size() {
        return ls.size();
    }

    @Override
    public String toString() {
        return this.getDesc();
    }
}
