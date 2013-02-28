/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package viewer;

import database.ConnectionPoolException;
import database.ConnectionPoolFactory;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ListIterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import viewer.manuzioParser.Schema;
import viewer.setting.NodeSettingInterface;
import viewer.setting.SettingXML;

/**
 *
 * @author Nicola Preden, matricola 818578, Facoltà di informatica Ca' Foscari
 * in Venice
 */
public class MainWindow extends javax.swing.JFrame {

    /**
     *  ActionListener per il menu
     */
    private class MenuConnectActionListener implements java.awt.event.ActionListener {

        String url = null;
        String user = null;
        String passw = null;

        MenuConnectActionListener(Properties prop) {
            url = prop.getProperty("url");
            user = prop.getProperty("user");
            passw = prop.getProperty("password");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Connection conn = null;
            try {
                conn = ConnectionPoolFactory.getConnection(url, user, passw);
                Main.schema = Schema.loadFromDB(conn);
            } catch (ParseException ex) {
                Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(Main.mw, "Impossibile Scaricare lo schema", "Errore", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(Main.mw, "URL errato o Server offline", "Errore", JOptionPane.ERROR_MESSAGE);
            } finally {
                if (conn != null) {
                    try {
                        Main.setConnectionPool(url, user, passw);
                        setEnableConnectStatus(true);
                    } catch (ConnectionPoolException ex) {
                        Logger.getLogger(ConnectWindow.class.getName()).log(Level.SEVERE, null, ex);
                        JOptionPane.showMessageDialog(Main.mw, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }

    /**
     * Creates new form MainWindow
     */
    public MainWindow() {
        initComponents();
        this.updateMenu();
    }

    /**
     * Disabilità o abilità i menu per la connessione/disconnessione, le toolbar e tutti i 
     * JPanel inerenti questa classe.
     * <code>javax.swing.JMenu</code> relativo alle connessioni a un db
     *
     * @param set
     */
    public synchronized void setEnableConnectStatus(boolean set) {
        jM_Connects.setEnabled(!set);
        disconnectMenuItem.setEnabled(set);
    }

    public synchronized void updateMenu() {
        if (jM_Connects.getItemCount() > 2) {
            for (int i = 2; i < jM_Connects.getItemCount(); i++) {
                JMenuItem item = jM_Connects.getItem(i);
                jM_Connects.remove(i);
            }
        }
        NodeSettingInterface setting = Main.setting.getSetting(SettingXML.CONNECTION_LIST);
        if (setting != null) {
            ListIterator<Properties> readProp = setting.readProp();
            while (readProp.hasNext()) {
                Properties next = readProp.next();
                String url = next.getProperty("url");

                JMenuItem x = new javax.swing.JMenuItem();
                x.setText(url.split("/")[0]);
                x.addActionListener(new MenuConnectActionListener(next));
                jM_Connects.add(x);

            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jP_Server = new javax.swing.JPanel();
        toolBarServer = new javax.swing.JToolBar();
        toolBarSx_Disconnect = new javax.swing.JButton();
        toolBarSx_NewType = new javax.swing.JButton();
        toolBarSx_RemoveType = new javax.swing.JButton();
        toolBarSx_RefrashRate = new javax.swing.JComboBox();
        jS_Level = new javax.swing.JSlider();
        jScrollPane1 = new javax.swing.JScrollPane();
        jT_SchemaServer = new javax.swing.JTree();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        jM_Connects = new javax.swing.JMenu();
        connectMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        disconnectMenuItem = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        cutMenuItem = new javax.swing.JMenuItem();
        copyMenuItem = new javax.swing.JMenuItem();
        pasteMenuItem = new javax.swing.JMenuItem();
        deleteMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        contentsMenuItem = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("ManuzioViewer");

        jP_Server.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, new java.awt.Color(153, 153, 153), new java.awt.Color(153, 153, 153)));
        jP_Server.setEnabled(false);

        toolBarServer.setFloatable(false);
        toolBarServer.setRollover(true);

        toolBarSx_Disconnect.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/32px/038.png"))); // NOI18N
        toolBarSx_Disconnect.setBorderPainted(false);
        toolBarSx_Disconnect.setFocusable(false);
        toolBarSx_Disconnect.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        toolBarSx_Disconnect.setPreferredSize(new java.awt.Dimension(40, 40));
        toolBarSx_Disconnect.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBarSx_Disconnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toolBarSx_DisconnectActionPerformed(evt);
            }
        });
        toolBarServer.add(toolBarSx_Disconnect);

        toolBarSx_NewType.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/32px/060.png"))); // NOI18N
        toolBarSx_NewType.setToolTipText("Aggiungi un nuovo tipo alla selezione");
        toolBarSx_NewType.setBorderPainted(false);
        toolBarSx_NewType.setFocusable(false);
        toolBarSx_NewType.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        toolBarSx_NewType.setPreferredSize(new java.awt.Dimension(40, 40));
        toolBarSx_NewType.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBarSx_NewType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toolBarSx_NewTypeActionPerformed(evt);
            }
        });
        toolBarServer.add(toolBarSx_NewType);

        toolBarSx_RemoveType.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/32px/059.png"))); // NOI18N
        toolBarSx_RemoveType.setToolTipText("Rimuovi selezionato");
        toolBarSx_RemoveType.setBorderPainted(false);
        toolBarSx_RemoveType.setFocusable(false);
        toolBarSx_RemoveType.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        toolBarSx_RemoveType.setPreferredSize(new java.awt.Dimension(40, 40));
        toolBarSx_RemoveType.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBarSx_RemoveType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toolBarSx_RemoveTypeActionPerformed(evt);
            }
        });
        toolBarServer.add(toolBarSx_RemoveType);

        toolBarSx_RefrashRate.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "45 s", "30 s", "15 s", "10 s", "5   s" }));
        toolBarSx_RefrashRate.setToolTipText("Tempo di aggirnamento finestra");
        toolBarSx_RefrashRate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toolBarSx_RefrashRateActionPerformed(evt);
            }
        });
        toolBarServer.add(toolBarSx_RefrashRate);

        jS_Level.setPaintLabels(true);
        jS_Level.setPaintTicks(true);
        jS_Level.setToolTipText("Livello di dettaglio");
        jS_Level.setValue(20);
        toolBarServer.add(jS_Level);

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        jT_SchemaServer.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        jScrollPane1.setViewportView(jT_SchemaServer);

        org.jdesktop.layout.GroupLayout jP_ServerLayout = new org.jdesktop.layout.GroupLayout(jP_Server);
        jP_Server.setLayout(jP_ServerLayout);
        jP_ServerLayout.setHorizontalGroup(
            jP_ServerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, toolBarServer, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 326, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jP_ServerLayout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane1)
                .addContainerGap())
        );
        jP_ServerLayout.setVerticalGroup(
            jP_ServerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jP_ServerLayout.createSequentialGroup()
                .add(toolBarServer, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 483, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, new java.awt.Color(153, 153, 153), new java.awt.Color(153, 153, 153)));
        jPanel2.setEnabled(false);

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 543, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 0, Short.MAX_VALUE)
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, new java.awt.Color(153, 153, 153), new java.awt.Color(153, 153, 153)));
        jPanel3.setEnabled(false);

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 33, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 0, Short.MAX_VALUE)
        );

        fileMenu.setMnemonic('f');
        fileMenu.setText("File");

        jM_Connects.setText("Connetti ....");

        connectMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.ALT_MASK));
        connectMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/16px/040.png"))); // NOI18N
        connectMenuItem.setMnemonic('o');
        connectMenuItem.setText("Nuovo ...");
        connectMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectMenuItemActionPerformed(evt);
            }
        });
        jM_Connects.add(connectMenuItem);
        jM_Connects.add(jSeparator1);

        fileMenu.add(jM_Connects);

        disconnectMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/16px/038.png"))); // NOI18N
        disconnectMenuItem.setMnemonic('s');
        disconnectMenuItem.setText("Disconnetti");
        disconnectMenuItem.setEnabled(false);
        disconnectMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                disconnectMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(disconnectMenuItem);

        exitMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.META_MASK));
        exitMenuItem.setMnemonic('x');
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        editMenu.setMnemonic('e');
        editMenu.setText("Edit");

        cutMenuItem.setMnemonic('t');
        cutMenuItem.setText("Cut");
        editMenu.add(cutMenuItem);

        copyMenuItem.setMnemonic('y');
        copyMenuItem.setText("Copy");
        editMenu.add(copyMenuItem);

        pasteMenuItem.setMnemonic('p');
        pasteMenuItem.setText("Paste");
        editMenu.add(pasteMenuItem);

        deleteMenuItem.setMnemonic('d');
        deleteMenuItem.setText("Delete");
        editMenu.add(deleteMenuItem);

        menuBar.add(editMenu);

        helpMenu.setMnemonic('h');
        helpMenu.setText("Help");

        contentsMenuItem.setMnemonic('c');
        contentsMenuItem.setText("Contents");
        helpMenu.add(contentsMenuItem);

        aboutMenuItem.setMnemonic('a');
        aboutMenuItem.setText("About");
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jP_Server, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(6, 6, 6)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(6, 6, 6)
                .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jP_Server, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        Main.shutdownProgram();
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void connectMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectMenuItemActionPerformed
        // TODO add your handling code here:
        Main.cw = new ConnectWindow(this);
        Main.cw.setVisible(true);
    }//GEN-LAST:event_connectMenuItemActionPerformed

    private void disconnectMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disconnectMenuItemActionPerformed
        // TODO add your handling code here:
        if (Main.shutdownConnectionPool()) {
            this.setEnableConnectStatus(false);
        }
    }//GEN-LAST:event_disconnectMenuItemActionPerformed

    private void toolBarSx_DisconnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toolBarSx_DisconnectActionPerformed
        // TODO add your handling code here:
        if (Main.shutdownConnectionPool()) {
            this.setEnableConnectStatus(false);
        }
    }//GEN-LAST:event_toolBarSx_DisconnectActionPerformed

    private void toolBarSx_NewTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toolBarSx_NewTypeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_toolBarSx_NewTypeActionPerformed

    private void toolBarSx_RemoveTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toolBarSx_RemoveTypeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_toolBarSx_RemoveTypeActionPerformed

    private void toolBarSx_RefrashRateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toolBarSx_RefrashRateActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_toolBarSx_RefrashRateActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JMenuItem connectMenuItem;
    private javax.swing.JMenuItem contentsMenuItem;
    private javax.swing.JMenuItem copyMenuItem;
    private javax.swing.JMenuItem cutMenuItem;
    private javax.swing.JMenuItem deleteMenuItem;
    private javax.swing.JMenuItem disconnectMenuItem;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenu jM_Connects;
    private javax.swing.JPanel jP_Server;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSlider jS_Level;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JTree jT_SchemaServer;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem pasteMenuItem;
    private javax.swing.JToolBar toolBarServer;
    private javax.swing.JButton toolBarSx_Disconnect;
    private javax.swing.JButton toolBarSx_NewType;
    private javax.swing.JComboBox toolBarSx_RefrashRate;
    private javax.swing.JButton toolBarSx_RemoveType;
    // End of variables declaration//GEN-END:variables
}
