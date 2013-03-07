/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package viewer;

import database.ConnectionPoolException;
import database.ConnectionPoolFactory;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ListIterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import viewer.manuzioParser.Schema;
import viewer.setting.NodeSettingInterface;
import viewer.setting.SettingXML;
import viewer.taskThread.TaskTree;

/**
 *
 * @author Nicola Preden, matricola 818578, Facoltà di informatica Ca' Foscari
 * in Venice
 */
public class MainWindow extends javax.swing.JFrame {

    /**
     * ActionListener per il menu
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
                        startTreeThread();
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
     * <p>Disabilità o abilità i menu per la connessione/disconnessione, le
     * toolbar e tutti i JPanel inerenti questa classe.</p>
     * <code>javax.swing.JMenu</code> relativo alle connessioni a un db
     *
     * @param set
     */
    public synchronized void setEnableConnectStatus(boolean set) {
        jM_Connects.setEnabled(!set);
        disconnectMenuItem.setEnabled(set);
        toolBarServer.setEnabled(set);
        jE_output.setEnabled(set);
        toolBarGeneral.setEnabled(set);
    }

    public synchronized void updateMenu() {
        if (jM_Connects.getItemCount() > 2) {
            for (int i = 2; i < jM_Connects.getItemCount(); i++) {
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
     * <p>Avvia i thread relativi all'aggiornamento del
     * <code>javax.swing.JTree</code></p>
     */
    public void startTreeThread() {
        if (Main.connectionIsSet()) {
            TaskTree tree;
            tree = new TaskTree(jT_SchemaServer, jE_output, Main.schema, jS_Level);
            tree.startThread();
            Main.setTaskTree(tree);
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
        toolBarSx_Refrash = new javax.swing.JButton();
        toolBarSx_NewType = new javax.swing.JButton();
        toolBarSx_RemoveType = new javax.swing.JButton();
        jS_Level = new javax.swing.JSlider();
        jScrollPane1 = new javax.swing.JScrollPane();
        jT_SchemaServer = new javax.swing.JTree();
        jT_SchemaServer.setToggleClickCount(2);
        jP_Output = new javax.swing.JPanel();
        toolBarGeneral = new javax.swing.JToolBar();
        jButton1 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jE_output = new javax.swing.JEditorPane();
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
        aboutMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("ManuzioViewer");
        setLocationByPlatform(true);
        setResizable(false);

        jP_Server.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        toolBarServer.setFloatable(false);
        toolBarServer.setRollover(true);

        toolBarSx_Disconnect.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/32px/038.png"))); // NOI18N
        toolBarSx_Disconnect.setToolTipText("Disconnetti");
        toolBarSx_Disconnect.setBorderPainted(false);
        toolBarSx_Disconnect.setFocusable(false);
        toolBarSx_Disconnect.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        toolBarSx_Disconnect.setMaximumSize(new java.awt.Dimension(40, 40));
        toolBarSx_Disconnect.setMinimumSize(new java.awt.Dimension(30, 30));
        toolBarSx_Disconnect.setPreferredSize(new java.awt.Dimension(40, 40));
        toolBarSx_Disconnect.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBarSx_Disconnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toolBarSx_DisconnectActionPerformed(evt);
            }
        });
        toolBarServer.add(toolBarSx_Disconnect);

        toolBarSx_Refrash.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/32px/033.png"))); // NOI18N
        toolBarSx_Refrash.setToolTipText("Aggiorna");
        toolBarSx_Refrash.setBorderPainted(false);
        toolBarSx_Refrash.setFocusable(false);
        toolBarSx_Refrash.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        toolBarSx_Refrash.setMaximumSize(new java.awt.Dimension(40, 40));
        toolBarSx_Refrash.setMinimumSize(new java.awt.Dimension(30, 30));
        toolBarSx_Refrash.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBarSx_Refrash.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toolBarSx_RefrashActionPerformed(evt);
            }
        });
        toolBarServer.add(toolBarSx_Refrash);

        toolBarSx_NewType.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/32px/060.png"))); // NOI18N
        toolBarSx_NewType.setToolTipText("Aggiungi un nuovo tipo alla selezione");
        toolBarSx_NewType.setBorderPainted(false);
        toolBarSx_NewType.setFocusable(false);
        toolBarSx_NewType.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        toolBarSx_NewType.setMaximumSize(new java.awt.Dimension(40, 40));
        toolBarSx_NewType.setMinimumSize(new java.awt.Dimension(30, 30));
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
        toolBarSx_RemoveType.setMaximumSize(new java.awt.Dimension(40, 40));
        toolBarSx_RemoveType.setMinimumSize(new java.awt.Dimension(30, 30));
        toolBarSx_RemoveType.setPreferredSize(new java.awt.Dimension(40, 40));
        toolBarSx_RemoveType.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBarSx_RemoveType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toolBarSx_RemoveTypeActionPerformed(evt);
            }
        });
        toolBarServer.add(toolBarSx_RemoveType);

        jS_Level.setMajorTickSpacing(1);
        jS_Level.setMaximum(10);
        jS_Level.setPaintLabels(true);
        jS_Level.setPaintTicks(true);
        jS_Level.setSnapToTicks(true);
        jS_Level.setToolTipText("Livello di dettaglio");
        jS_Level.setValue(1);
        jS_Level.setEnabled(false);
        toolBarServer.add(jS_Level);

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("Server Disconnesso");
        jT_SchemaServer.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        jScrollPane1.setViewportView(jT_SchemaServer);

        org.jdesktop.layout.GroupLayout jP_ServerLayout = new org.jdesktop.layout.GroupLayout(jP_Server);
        jP_Server.setLayout(jP_ServerLayout);
        jP_ServerLayout.setHorizontalGroup(
            jP_ServerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jP_ServerLayout.createSequentialGroup()
                .add(jP_ServerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jP_ServerLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(jScrollPane1))
                    .add(toolBarServer, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 383, Short.MAX_VALUE))
                .addContainerGap())
        );
        jP_ServerLayout.setVerticalGroup(
            jP_ServerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jP_ServerLayout.createSequentialGroup()
                .add(toolBarServer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(jScrollPane1)
                .addContainerGap())
        );

        jP_Output.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        toolBarGeneral.setFloatable(false);
        toolBarGeneral.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/32px/035.png"))); // NOI18N
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setMaximumSize(new java.awt.Dimension(40, 40));
        jButton1.setMinimumSize(new java.awt.Dimension(30, 30));
        jButton1.setPreferredSize(new java.awt.Dimension(40, 40));
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBarGeneral.add(jButton1);

        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane2.setViewportBorder(javax.swing.BorderFactory.createTitledBorder("OutPut Area"));
        jScrollPane2.setMinimumSize(new java.awt.Dimension(500, 300));
        jScrollPane2.setPreferredSize(new java.awt.Dimension(400, 300));

        jE_output.setEditable(false);
        jE_output.setEnabled(false);
        jScrollPane2.setViewportView(jE_output);

        org.jdesktop.layout.GroupLayout jP_OutputLayout = new org.jdesktop.layout.GroupLayout(jP_Output);
        jP_Output.setLayout(jP_OutputLayout);
        jP_OutputLayout.setHorizontalGroup(
            jP_OutputLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jP_OutputLayout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 579, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(toolBarGeneral, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 46, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        jP_OutputLayout.setVerticalGroup(
            jP_OutputLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jP_OutputLayout.createSequentialGroup()
                .add(toolBarGeneral, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 495, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 15, Short.MAX_VALUE))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jP_OutputLayout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 358, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        fileMenu.setMnemonic('f');
        fileMenu.setText("File");

        jM_Connects.setText("Connetti ....");

        connectMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
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

    disconnectMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
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

    exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
exitMenuItem.setMnemonic('x');
exitMenuItem.setText("Exit");
exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        exitMenuItemActionPerformed(evt);
    }
    });
    if (!Main.isOSX()){
        fileMenu.add(exitMenuItem);
    }

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
            .add(jP_Server, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            .add(jP_Output, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 647, Short.MAX_VALUE)
            .addContainerGap())
    );
    layout.setVerticalGroup(
        layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
        .add(layout.createSequentialGroup()
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(org.jdesktop.layout.GroupLayout.TRAILING, jP_Output, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 514, Short.MAX_VALUE)
                .add(jP_Server, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addContainerGap())
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

    private void toolBarSx_RefrashActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toolBarSx_RefrashActionPerformed
        // TODO add your handling code here:
        if (Main.taskTree != null) {
            Main.taskTree.refresh();
        }
    }//GEN-LAST:event_toolBarSx_RefrashActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JMenuItem connectMenuItem;
    private javax.swing.JMenuItem copyMenuItem;
    private javax.swing.JMenuItem cutMenuItem;
    private javax.swing.JMenuItem deleteMenuItem;
    private javax.swing.JMenuItem disconnectMenuItem;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JButton jButton1;
    private javax.swing.JEditorPane jE_output;
    private javax.swing.JMenu jM_Connects;
    private javax.swing.JPanel jP_Output;
    private javax.swing.JPanel jP_Server;
    private javax.swing.JSlider jS_Level;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JTree jT_SchemaServer;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem pasteMenuItem;
    private javax.swing.JToolBar toolBarGeneral;
    private javax.swing.JToolBar toolBarServer;
    private javax.swing.JButton toolBarSx_Disconnect;
    private javax.swing.JButton toolBarSx_NewType;
    private javax.swing.JButton toolBarSx_Refrash;
    private javax.swing.JButton toolBarSx_RemoveType;
    // End of variables declaration//GEN-END:variables
}
