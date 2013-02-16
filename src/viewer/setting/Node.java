/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package viewer.setting;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

public class Node implements NodeSettingInterface {

    String desc;
    ArrayList<Properties> ls;

    public Node(String desc) {
        this.desc = desc;
        ls = new ArrayList<Properties>();

    }

    @Override
    public String getDesc() {
        return this.desc;
    }

    @Override
    public boolean addProp(Properties... Prop) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean addAtFistOccProp(Properties pro) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean removeProp(Properties... Prop) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean removeLastProp() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Iterator<Properties> readProp() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int compareTo(NodeSettingInterface o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
