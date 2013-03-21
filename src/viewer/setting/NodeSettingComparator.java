package viewer.setting;

import java.util.Comparator;

/**
 * Implemetazione di un coparatore per eseguire ricerche ed ordinamenti
 * @author Nicola Preden, matricola 818578, Facoltà di informatica Ca' Foscari
 * in Venice
 */
 class NodeSettingComparator implements Comparator<NodeSettingInterface>{

    @Override
    public int compare(NodeSettingInterface o1, NodeSettingInterface o2) {
        return o1.compareTo(o2);
    }
    
}
