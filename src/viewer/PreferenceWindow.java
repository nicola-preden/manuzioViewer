package viewer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.ListIterator;
import java.util.Properties;
import javax.swing.JPanel;
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

    private class JPanelLanguages extends JPanel implements PreferencesPane {

        @Override
        public void savePreferences() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void resetToDefaultPreferences() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isModified() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

    private class JPanelTextGraphics extends JPanel implements PreferencesPane {

        @Override
        public void savePreferences() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void resetToDefaultPreferences() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isModified() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
            while(readProp.hasNext()) {
                Properties next = readProp.next();
                al.add(next.getProperty("schema-url"));
            }
            Collections.sort(al);
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Inserimento lista schemi delle connessioni ricenti (non necessariamente preseti nel xml)">
        if (ConnectionList != null) {
            ListIterator<Properties> readProp = ConnectionList.readProp();
            while(readProp.hasNext()) {
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
        jL_server.setListData(al.toArray(new String[al.size()]));
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
        jB_CloseAndSave = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Preferenze");

        jL_server.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jL_server.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jL_serverValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(jL_server);

        org.jdesktop.layout.GroupLayout jP_SchemaConfigLayout = new org.jdesktop.layout.GroupLayout(jP_SchemaConfig);
        jP_SchemaConfig.setLayout(jP_SchemaConfigLayout);
        jP_SchemaConfigLayout.setHorizontalGroup(
            jP_SchemaConfigLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 425, Short.MAX_VALUE)
        );
        jP_SchemaConfigLayout.setVerticalGroup(
            jP_SchemaConfigLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 295, Short.MAX_VALUE)
        );

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
                .add(jScrollPane2)
                .addContainerGap())
        );
        jP_TextLayoutLayout.setVerticalGroup(
            jP_TextLayoutLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jP_TextLayoutLayout.createSequentialGroup()
                .add(jP_TextLayoutLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane2)
                    .add(jP_TextLayoutLayout.createSequentialGroup()
                        .add(jScrollPane1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jB_remove)))
                .addContainerGap())
        );

        jTP_Preferences.addTab("Layout", jP_TextLayout);

        org.jdesktop.layout.GroupLayout jP_LanguagesLayout = new org.jdesktop.layout.GroupLayout(jP_Languages);
        jP_Languages.setLayout(jP_LanguagesLayout);
        jP_LanguagesLayout.setHorizontalGroup(
            jP_LanguagesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 596, Short.MAX_VALUE)
        );
        jP_LanguagesLayout.setVerticalGroup(
            jP_LanguagesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 305, Short.MAX_VALUE)
        );

        jTP_Preferences.addTab("Lingua", jP_Languages);

        jB_close.setText("Chiudi");
        jB_close.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jB_closeActionPerformed(evt);
            }
        });

        jB_CloseAndSave.setText("Salva");
        jB_CloseAndSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jB_CloseAndSaveActionPerformed(evt);
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
                        .add(jB_CloseAndSave)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jB_close)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jTP_Preferences)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jB_CloseAndSave)
                    .add(jB_close))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jB_closeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jB_closeActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_jB_closeActionPerformed

    private void jB_CloseAndSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jB_CloseAndSaveActionPerformed
        JPanelLanguages lang = (JPanelLanguages) jP_Languages;
        JPanelTextGraphics text = (JPanelTextGraphics) jP_TextLayout;
        if (lang.isModified()) {
            lang.savePreferences();
        }
        if (text.isModified()) {
            text.savePreferences();
        }
    }//GEN-LAST:event_jB_CloseAndSaveActionPerformed

    private void jL_serverValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jL_serverValueChanged
        // TODO add your handling code here:
        
    }//GEN-LAST:event_jL_serverValueChanged

    private void jB_removeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jB_removeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jB_removeActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jB_CloseAndSave;
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
