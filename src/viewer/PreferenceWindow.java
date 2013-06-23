package viewer;

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import static viewer.ManuzioViewer.shutdownProgram;
import viewer.setting.AbstractTextualLayout;
import viewer.setting.NodeSettingInterface;
import viewer.setting.PreferencesPane;
import viewer.setting.SettingXML;

/**
 * <p>JFrame responsabile della visualizzazione e modifica delle enventuali
 * Preferences dell'applicazione. </p>
 *
 * @author Nicola Preden, matricola 818578, Facoltà di informatica Ca' Foscari
 * in Venice
 */
public class PreferenceWindow extends javax.swing.JFrame {

    private static final ResourceBundle lang = ResourceBundle.getBundle("viewer/language/lang", ManuzioViewer.LANGUAGE);

    private class JPanelLanguages extends JPanel implements PreferencesPane {

        private class ActionListenerComboBox implements ActionListener {

            private final Properties next;
            private final String set;

            private ActionListenerComboBox(Properties next, String set) {
                this.next = next;
                this.set = set;
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                Anonymus selectedItem =(Anonymus) ((JComboBox) e.getSource()).getSelectedItem();
                next.setProperty(set, selectedItem.l.toString());
                isModified = true;
            }
        }

        private class Anonymus {

            Locale l;

            public Anonymus(Locale l) {
                this.l = l;
            }

            @Override
            public String toString() {
                return l.getDisplayLanguage();

            }
        }
        private volatile boolean isModified = false;

        @Override
        public boolean isModified() {
            return isModified;
        }

        @Override
        public void setPreferences(NodeSettingInterface node) {
            try {
                GridBagConstraints c;
                JLabel l = new JLabel(lang.getString("LANGUAGE"));
                JComboBox f;
                
                
                ArrayList<Anonymus> ar = new ArrayList();
                ClassLoader loader = Thread.currentThread().getContextClassLoader();
                Path dir = Paths.get(loader.getResource("viewer/language").toURI());
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "lang_?*.{properties}")) {
                    for (Path entry : stream) {
                        String s = entry.getFileName().toString().replaceAll("^lang(_)?|\\.properties$", "").replaceAll("_", "-");
                        Locale forLanguageTag = Locale.forLanguageTag(s);
                        ar.add(new Anonymus(forLanguageTag));
                    }
                } catch (IOException x) {
                    Logger.getLogger(ManuzioViewer.class.getName()).log(Level.SEVERE, null, x);
                }
                Object[] possibilities = ar.toArray();
                f = new JComboBox(possibilities);
                for (int i = 0; i < possibilities.length; i++) {
                    if (((Anonymus)possibilities[i]).l.equals(ManuzioViewer.LANGUAGE)) {
                        f.setSelectedIndex(i);
                        break;
                    }
                }
                f.addActionListener(new ActionListenerComboBox(node.readProp().next(), "lang"));
                
                c = new GridBagConstraints();
                c.fill = GridBagConstraints.HORIZONTAL;
                c.anchor = GridBagConstraints.PAGE_START;
                c.gridx = 0;
                c.gridwidth = 1;
                c.gridy = 0;
                this.add(l,c);
                c = new GridBagConstraints();
                c.fill = GridBagConstraints.HORIZONTAL;
                c.anchor = GridBagConstraints.PAGE_END;
                c.gridx = 1;
                c.gridwidth = 1;
                c.gridy = 0;
                this.add(f,c);
                
            } catch (URISyntaxException ex) {
                Logger.getLogger(PreferenceWindow.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        @Override
        public void removePreference(String url) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

    private class JPanelTextGraphics extends JPanel implements PreferencesPane {

        private class ActionListenerComboBox implements ActionListener {

            private final Properties next;
            private final String set;

            private ActionListenerComboBox(Properties next, String set) {
                this.next = next;
                this.set = set;
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = ((JComboBox) e.getSource()).getSelectedIndex();
                next.setProperty(set, selectedIndex + "");
                isModified = true;
            }
        }
        private volatile boolean isModified = false;
        private String VOID = "Void";

        @Override
        public void setPreferences(NodeSettingInterface node) {
            jP_SchemaConfig.add(new JPanel(), VOID);
            CardLayout cl = (CardLayout) (jP_SchemaConfig.getLayout());
            cl.show(jP_SchemaConfig, VOID);

            ListIterator<Properties> readProp = node.readProp();
            while (readProp.hasNext()) {
                Properties next = readProp.next(); // Properties corrente

                JPanel panel = new JPanel();
                panel.setLayout(new GridBagLayout());
                GridBagConstraints c;
                JLabel l;
                JComboBox f;


                String url = next.getProperty("schema-url");
                jP_SchemaConfig.add(panel, url);
                if (viewer.setting.AbstractTextualLayout.typeConsistencyCheck(next, viewer.ManuzioViewer.schema)) {
                    // Server Connesso ed tutto ok
                    l = new JLabel(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("viewer/language/lang").getString("DATABASE: {0}"), new Object[]{url}));
                } else {
                    String t = viewer.ManuzioViewer.getJdbcUrl() != null ? viewer.ManuzioViewer.getJdbcUrl() : "";
                    if (t.compareTo(url) != 0) {
                        // Server Disconnesso tutto ok
                        l = new JLabel(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("viewer/language/lang").getString("DATABASE_RED: {0}"), new Object[]{url}));
                    } else {
                        // Server Connesso con dati mancanti
                        l = new JLabel(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("viewer/language/lang").getString("DATABASE: {0}"), new Object[]{url}));
                        next.clear();
                        next.setProperty("schema-url", url);
                        viewer.manuzioParser.Type[] typeSet = viewer.ManuzioViewer.schema.getTypeSet();
                        for (int i = 0; i < typeSet.length; i++) {
                            next.setProperty(typeSet[i].getTypeName(), AbstractTextualLayout.DEFAULT + "");
                        }
                    }
                }
                l.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                c = new GridBagConstraints();
                c.fill = GridBagConstraints.HORIZONTAL;
                c.anchor = GridBagConstraints.PAGE_START;
                c.gridx = 0;
                c.gridwidth = 2;
                c.gridy = 0;
                panel.add(l, c);

                Set<String> spn = next.stringPropertyNames(); // Lista keys
                Iterator<String> iterator = spn.iterator();
                int i = 1;
                while (iterator.hasNext()) { // per ogni keys della Properties correte 
                    String set = iterator.next();
                    if (set.compareTo("schema-url") != 0) {
                        l = new JLabel(set);
                        l.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);

                        c = new GridBagConstraints();
                        c.fill = GridBagConstraints.HORIZONTAL;
                        c.anchor = GridBagConstraints.LINE_START;
                        c.gridx = 0;
                        c.gridwidth = 1;
                        c.gridy = i;
                        panel.add(l, c);

                        f = new JComboBox(viewer.setting.AbstractTextualLayout.LIST_OPERATION);
                        f.setSelectedIndex(Integer.parseInt(next.getProperty(set)));
                        f.setEditable(false);
                        // Il salvataggio dei nuovi dati è immeditato stile OS X
                        f.addActionListener(new ActionListenerComboBox(next, set));

                        c = new GridBagConstraints();
                        c.fill = GridBagConstraints.HORIZONTAL;
                        c.anchor = GridBagConstraints.LINE_END;
                        c.gridx = 1;
                        c.gridwidth = 1;
                        c.gridy = i;
                        panel.add(f, c);

                        i++;
                    }
                }
            }
        }

        @Override
        public void removePreference(String url) {
            NodeSettingInterface setting = viewer.ManuzioViewer.setting.getSetting("SchemaList");
            ListIterator<Properties> readProp = setting.readProp();
            while (readProp.hasNext()) {
                Properties next = readProp.next();
                if (next.getProperty("schema-url").compareTo(url) == 0) {
                    setting.removeProp(next);
                    isModified = true;
                    return;
                }
            }
        }

        @Override
        public boolean isModified() {
            return isModified;
        }
    }

    /**
     * Creates new form PreferenceWindow
     */
    public PreferenceWindow() {
        initComponents();
        NodeSettingInterface SchemaList = ManuzioViewer.setting.getSetting(SettingXML.SCHEMA_LIST);
        NodeSettingInterface ConnectionList = ManuzioViewer.setting.getSetting(SettingXML.CONNECTION_LIST);
        ArrayList<String> al = new ArrayList<String>();
        //<editor-fold defaultstate="collapsed" desc="Inserimento lista schemi preinseriti nel xml">
        if (SchemaList != null) {
            ListIterator<Properties> readProp = SchemaList.readProp();
            while (readProp.hasNext()) {
                Properties next = readProp.next();
                al.add(next.getProperty("schema-url"));
            }
            Collections.sort(al);
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Inserimento lista schemi delle connessioni ricenti (non necessariamente preseti nel xml)">
        if (ConnectionList != null) {
            ListIterator<Properties> readProp = ConnectionList.readProp();
            while (readProp.hasNext()) {
                Properties next = readProp.next();
                String url = next.getProperty("url");
                int binarySearch = Collections.binarySearch(al, url);
                if (binarySearch < 0) {
                    al.add(url);
                    Properties p = new Properties();
                    p.setProperty("schema-url", url);
                    viewer.ManuzioViewer.setting.addSetting(SettingXML.SCHEMA_LIST, p);
                }
            }
        }
        //</editor-fold>
        jL_server.setValueIsAdjusting(true);
        // inserimento valori
        DefaultListModel<String> m = new DefaultListModel();
        for (String s : al) {
            m.addElement(s);
        }
        jL_server.setModel(m);
        JPanelTextGraphics text = (JPanelTextGraphics) jP_TextLayout;
        text.setPreferences(viewer.ManuzioViewer.setting.getSetting(SettingXML.SCHEMA_LIST));
        JPanelLanguages language = (JPanelLanguages)jP_Languages;
        language.setPreferences(viewer.ManuzioViewer.setting.getSetting(SettingXML.LANGUAGE_SELECT));
        jL_server.setValueIsAdjusting(false);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTP_Preferences = new javax.swing.JTabbedPane();
        jP_TextLayout = new JPanelTextGraphics();
        jScrollPane1 = new javax.swing.JScrollPane();
        jL_server = new javax.swing.JList();
        jScrollPane2 = new javax.swing.JScrollPane();
        jP_SchemaConfig = new javax.swing.JPanel();
        jB_remove = new javax.swing.JButton();
        jP_Languages = new JPanelLanguages();
        jB_close = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(lang.getString("PREFERENCES")); // NOI18N

        jL_server.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jL_server.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jL_serverValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(jL_server);

        jP_SchemaConfig.setLayout(new java.awt.CardLayout());
        jScrollPane2.setViewportView(jP_SchemaConfig);

        jB_remove.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/16px/059.png"))); // NOI18N
        jB_remove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jB_removeActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jP_TextLayoutLayout = new org.jdesktop.layout.GroupLayout(jP_TextLayout);
        jP_TextLayout.setLayout(jP_TextLayoutLayout);
        jP_TextLayoutLayout.setHorizontalGroup(
            jP_TextLayoutLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jP_TextLayoutLayout.createSequentialGroup()
                .addContainerGap()
                .add(jP_TextLayoutLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 143, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jB_remove, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 26, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 429, Short.MAX_VALUE)
                .addContainerGap())
        );
        jP_TextLayoutLayout.setVerticalGroup(
            jP_TextLayoutLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jP_TextLayoutLayout.createSequentialGroup()
                .add(jP_TextLayoutLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane2)
                    .add(jP_TextLayoutLayout.createSequentialGroup()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 265, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jB_remove)))
                .addContainerGap())
        );

        jTP_Preferences.addTab(lang.getString("LAYOUT"), jP_TextLayout); // NOI18N

        jP_Languages.setLayout(new java.awt.GridBagLayout());
        jTP_Preferences.addTab(lang.getString("LANGUAGE"), jP_Languages); // NOI18N

        jB_close.setText(lang.getString("EXIT")); // NOI18N
        jB_close.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jB_closeActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jTP_Preferences)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(0, 0, Short.MAX_VALUE)
                        .add(jB_close)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jTP_Preferences)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jB_close)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jB_closeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jB_closeActionPerformed
        this.setVisible(false);
        JPanelLanguages lang = (JPanelLanguages) jP_Languages;
        JPanelTextGraphics text = (JPanelTextGraphics) jP_TextLayout;
        if (lang.isModified()) {
        }
        if (text.isModified()) {
        }
    }//GEN-LAST:event_jB_closeActionPerformed

    private void jL_serverValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jL_serverValueChanged
        if (jL_server.isSelectionEmpty()) {
            return;
        }
        String value = ((String) jL_server.getSelectedValue());
        CardLayout cl = (CardLayout) (jP_SchemaConfig.getLayout());
        cl.show(jP_SchemaConfig, value);
    }//GEN-LAST:event_jL_serverValueChanged

    private void jB_removeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jB_removeActionPerformed
        if (jL_server.isSelectionEmpty()) {
            return;
        }
        jL_server.setValueIsAdjusting(true);
        JPanelTextGraphics text = (JPanelTextGraphics) jP_TextLayout;
        String value = ((String) jL_server.getSelectedValue());
        int index = jL_server.getSelectedIndex();
        text.removePreference(value);
        DefaultListModel m = (DefaultListModel) jL_server.getModel();
        m.remove(index);
        jL_server.setValueIsAdjusting(false);
    }//GEN-LAST:event_jB_removeActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jB_close;
    private javax.swing.JButton jB_remove;
    private javax.swing.JList jL_server;
    private javax.swing.JPanel jP_Languages;
    private javax.swing.JPanel jP_SchemaConfig;
    private javax.swing.JPanel jP_TextLayout;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTP_Preferences;
    // End of variables declaration//GEN-END:variables
}
