/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package viewer.setting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.ListIterator;
import java.util.Properties;
import java.util.Set;

public class NodeSetting implements NodeSettingInterface {

    private String desc;
    private ArrayList<Properties> ls;

    public NodeSetting(String desc, Properties... prop) {
        this.desc = desc;
        ls = new ArrayList<Properties>();
        for (Properties p : prop) {
            ls.add(p);
        }

    }

    @Override
    public String getDesc() {
        return this.desc;
    }

    @Override
    synchronized public void addProp(Properties... prop) {
        for (Properties p : prop) {
            ls.add(p);
        }
    }

    @Override
    synchronized public void addAtFistOccProp(Properties pro) {
        ls.add(0, pro);
    }

    @Override
    synchronized public void removeProp(Properties... prop) {
        throw new UnsupportedOperationException("Not supported yet.");
        for (Properties p : prop) {
            Enumeration<Object> keys = p.keys();
            for (int i = 0; i < ls.size(); i++) {
                while (keys.hasMoreElements()) {
                }
            }

            ls.remove(p);
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
}
