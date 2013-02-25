/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package viewer;

import database.ConnectionPoolException;
import database.ConnectionPoolFactory;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import viewer.manuzioParser.Schema;
import viewer.setting.SettingXML;

/**
 *
 * @author Nicola Preden, matricola 818578, Facoltà di informatica Ca' Foscari
 * in Venice
 */
public class ConnectWindow extends javax.swing.JFrame implements PropertyChangeListener {

    private MainWindow mainWindow;
    private JFileChooser chooser;
    private File f = null;
    private String text = null;
    private String user;
    private String password;
    private String dbName;
    private String url;

    private class TaskCreate extends SwingWorker<Void, Void> {

        private static final int MAX = 5;

        @Override
        protected Void doInBackground() {
            boolean conn = false;
            try {
                setProgress(0);
                if (jRb_loadFromFile.isSelected()) {
                    Main.schema = Schema.loadFromFile(f);
                }
                if (jRb_loadFromString.isSelected()) {
                    Main.schema = Schema.buildFromSourceCode(text);
                }
                setProgress(100 * 1 / MAX);
                conn = Main.buildManuzioDB(url, dbName, user, password, true);
                setProgress(100 * 2 / MAX);
                Main.schema.saveToDB(url, dbName, user, password);
                setProgress(100 * 3 / MAX);
            } catch (IOException | ParseException ex) {
                Logger.getLogger(ConnectWindow.class.getName()).log(Level.SEVERE, null, ex);
                Main.schema = null;
                JOptionPane.showMessageDialog(Main.cw, "Errore Caricamento Schema", "Errore", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                Logger.getLogger(ConnectWindow.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(Main.cw, "Impossibile Creare il DataBase", "Errore", JOptionPane.ERROR_MESSAGE);
                conn = false;
            } finally {
                if (conn) {
                    try {
                        Main.setConnectionPool(url + "/" + dbName, user, password);
                        setProgress(100 * 4 / MAX);
                        Properties prop = new Properties();
                        prop.setProperty("url", url + "/" + dbName);
                        prop.setProperty("user", user);
                        prop.setProperty("password", password);
                        Main.setting.addSettingAtTop(SettingXML.CONNECTION_LIST, prop);
                        mainWindow.updateMenu();
                        mainWindow.setEnableConnectMenu(false);
                        setProgress(100 * 5 / MAX);
                    } catch (ConnectionPoolException ex) {
                        Logger.getLogger(ConnectWindow.class.getName()).log(Level.SEVERE, null, ex);
                        JOptionPane.showMessageDialog(Main.cw, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
            return null;
        }

        @Override
        protected void done() {
            Toolkit.getDefaultToolkit().beep();
            setCursor(null);
            jb_Connect_new.setEnabled(true);
            this.removePropertyChangeListener(Main.cw);
            if (Main.connectionIsSet()) {
                Main.cw.setVisible(false);
                Main.cw = null;
            } else {
                jPr_create.setVisible(false);
            }
        }
    }

    private class TaskConnect extends SwingWorker<Void, Void> {

        private static final int MAX = 5;

        @Override
        protected Void doInBackground() throws Exception {
            Connection conn = null;
            try {
                setProgress(0);
                conn = ConnectionPoolFactory.getConnection(url, user, password);
                setProgress(100 * 1 / MAX);
                Main.schema = Schema.loadFromDB(conn);
                setProgress(100 * 2 / MAX);
            } catch (SQLException ex) {
                Logger.getLogger(ConnectWindow.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(Main.cw, "URL errato o Server offline", "Errore", JOptionPane.ERROR_MESSAGE);
            } finally {
                if (conn != null) {
                    try {
                        Main.setConnectionPool(url, user, password);
                        setProgress(100 * 3 / MAX);
                        Properties prop = new Properties();
                        prop.setProperty("url", url);
                        prop.setProperty("user", user);
                        prop.setProperty("password", password);
                        Main.setting.addSettingAtTop(SettingXML.CONNECTION_LIST, prop);
                        mainWindow.updateMenu();
                        mainWindow.setEnableConnectMenu(false);
                        setProgress(100 * 4 / MAX);
                    } catch (ConnectionPoolException ex) {
                        Logger.getLogger(ConnectWindow.class.getName()).log(Level.SEVERE, null, ex);
                        JOptionPane.showMessageDialog(Main.cw, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
            return null;
        }

        @Override
        protected void done() {
            Toolkit.getDefaultToolkit().beep();
            setCursor(null);
            jb_Connect.setEnabled(true);
            this.removePropertyChangeListener(Main.cw);
            if (Main.connectionIsSet()) {
                Main.cw.setVisible(false);
                Main.cw = null;
            } else {
                jPr_connect.setVisible(false);
            }
        }
    }

    /**
     * Creates new form ConnectWindow
     */
    public ConnectWindow(MainWindow mainWindow) {
        initComponents();
        this.setVisible(false);
        this.mainWindow = mainWindow;
        chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Plain text file", "txt");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(filter);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jbG_load = new javax.swing.ButtonGroup();
        jt_connect_type = new javax.swing.JTabbedPane();
        jp_connect_DB = new javax.swing.JPanel();
        jlb_addr = new javax.swing.JLabel();
        jlb_port = new javax.swing.JLabel();
        jtf_addr = new javax.swing.JTextField();
        jtf_port = new javax.swing.JTextField();
        jlb_dbName = new javax.swing.JLabel();
        jtf_dbName = new javax.swing.JTextField();
        jlb_usr = new javax.swing.JLabel();
        jtf_usr = new javax.swing.JTextField();
        jtf_passw = new javax.swing.JPasswordField();
        jlb_passw = new javax.swing.JLabel();
        jb_Connect = new javax.swing.JButton();
        jPr_connect = new javax.swing.JProgressBar();
        jp_newDB = new javax.swing.JPanel();
        jlb_addr_new = new javax.swing.JLabel();
        jlb_port_new = new javax.swing.JLabel();
        jtf_port_new = new javax.swing.JTextField();
        jtf_addr_new = new javax.swing.JTextField();
        jlb_dbName_new = new javax.swing.JLabel();
        jtf_dbName_new = new javax.swing.JTextField();
        jtf_usr_new = new javax.swing.JTextField();
        jtf_passw_new = new javax.swing.JPasswordField();
        jlb_passw_new = new javax.swing.JLabel();
        jlb_usr_new = new javax.swing.JLabel();
        jb_Connect_new = new javax.swing.JButton();
        jB_load = new javax.swing.JButton();
        jRb_loadFromFile = new javax.swing.JRadioButton();
        jRb_loadFromString = new javax.swing.JRadioButton();
        jPr_create = new javax.swing.JProgressBar();
        jB_cancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Connetti");
        setAlwaysOnTop(true);
        setResizable(false);

        jlb_addr.setText("Indirizzo Server");

        jlb_port.setText("Porta");

        jtf_addr.setToolTipText("<html> www.server.it <br \\> 192.169.34.2");

        jlb_dbName.setText("Nome Database");

        jlb_usr.setText("Nome Utente");

        jlb_passw.setText("Password");

        jb_Connect.setText("Connetti");
        jb_Connect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jb_ConnectActionPerformed(evt);
            }
        });

        jPr_connect.setStringPainted(true);
        jPr_connect.setVisible(false);
        jPr_connect.setIndeterminate(true);

        org.jdesktop.layout.GroupLayout jp_connect_DBLayout = new org.jdesktop.layout.GroupLayout(jp_connect_DB);
        jp_connect_DB.setLayout(jp_connect_DBLayout);
        jp_connect_DBLayout.setHorizontalGroup(
            jp_connect_DBLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jp_connect_DBLayout.createSequentialGroup()
                .add(24, 24, 24)
                .add(jp_connect_DBLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jtf_dbName)
                    .add(jp_connect_DBLayout.createSequentialGroup()
                        .add(jp_connect_DBLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jlb_addr)
                            .add(jtf_addr, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 262, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jlb_dbName))
                        .add(18, 18, Short.MAX_VALUE)
                        .add(jp_connect_DBLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(jp_connect_DBLayout.createSequentialGroup()
                                .add(jlb_port)
                                .add(48, 48, 48))
                            .add(jtf_port)))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jp_connect_DBLayout.createSequentialGroup()
                        .add(jp_connect_DBLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jtf_usr)
                            .add(jp_connect_DBLayout.createSequentialGroup()
                                .add(jlb_usr)
                                .add(0, 0, Short.MAX_VALUE)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(jp_connect_DBLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jlb_passw)
                            .add(jtf_passw, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 179, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .add(33, 33, 33))
            .add(jp_connect_DBLayout.createSequentialGroup()
                .add(jp_connect_DBLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jp_connect_DBLayout.createSequentialGroup()
                        .add(151, 151, 151)
                        .add(jb_Connect, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 98, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jp_connect_DBLayout.createSequentialGroup()
                        .add(127, 127, 127)
                        .add(jPr_connect, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jp_connect_DBLayout.setVerticalGroup(
            jp_connect_DBLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jp_connect_DBLayout.createSequentialGroup()
                .add(12, 12, 12)
                .add(jp_connect_DBLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jp_connect_DBLayout.createSequentialGroup()
                        .add(jlb_addr)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jtf_addr, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jp_connect_DBLayout.createSequentialGroup()
                        .add(jlb_port)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jtf_port, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jlb_dbName)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jtf_dbName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jp_connect_DBLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jlb_usr)
                    .add(jlb_passw))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jp_connect_DBLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(jtf_usr, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jtf_passw, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 70, Short.MAX_VALUE)
                .add(jPr_connect, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jb_Connect))
        );

        jt_connect_type.addTab("Connetti ...", jp_connect_DB);

        jlb_addr_new.setText("Indirizzo Server");

        jlb_port_new.setText("Porta");

        jtf_addr_new.setToolTipText("<html>\nwww.server.it <br \\>\n192.169.34.2");

        jlb_dbName_new.setText("Nome del nuovo Database");

        jlb_passw_new.setText("Password");

        jlb_usr_new.setText("Nome Utente");

        jb_Connect_new.setText("Connetti");
        jb_Connect_new.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jb_Connect_newActionPerformed(evt);
            }
        });

        jB_load.setText("Carica");
        jB_load.setEnabled(false);
        jB_load.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jB_loadActionPerformed(evt);
            }
        });

        jbG_load.add(jRb_loadFromFile);
        jRb_loadFromFile.setText("Carica Schema da File");
        jRb_loadFromFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRb_loadFromFileActionPerformed(evt);
            }
        });

        jbG_load.add(jRb_loadFromString);
        jRb_loadFromString.setText("Carica Schema da testo");
        jRb_loadFromString.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRb_loadFromStringActionPerformed(evt);
            }
        });

        jPr_create.setStringPainted(true);
        jPr_create.setVisible(false);
        jPr_create.setIndeterminate(true);

        org.jdesktop.layout.GroupLayout jp_newDBLayout = new org.jdesktop.layout.GroupLayout(jp_newDB);
        jp_newDB.setLayout(jp_newDBLayout);
        jp_newDBLayout.setHorizontalGroup(
            jp_newDBLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jp_newDBLayout.createSequentialGroup()
                .add(24, 24, 24)
                .add(jp_newDBLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jp_newDBLayout.createSequentialGroup()
                        .add(jp_newDBLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jtf_dbName_new)
                            .add(jp_newDBLayout.createSequentialGroup()
                                .add(jp_newDBLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jlb_addr_new)
                                    .add(jtf_addr_new, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 262, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(jlb_dbName_new))
                                .add(18, 18, Short.MAX_VALUE)
                                .add(jp_newDBLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                    .add(jp_newDBLayout.createSequentialGroup()
                                        .add(jlb_port_new)
                                        .add(48, 48, 48))
                                    .add(jtf_port_new)))
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jp_newDBLayout.createSequentialGroup()
                                .add(jp_newDBLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jtf_usr_new)
                                    .add(jp_newDBLayout.createSequentialGroup()
                                        .add(jlb_usr_new)
                                        .add(0, 0, Short.MAX_VALUE)))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(jp_newDBLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jlb_passw_new)
                                    .add(jtf_passw_new, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 179, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                            .add(jp_newDBLayout.createSequentialGroup()
                                .add(jp_newDBLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jRb_loadFromFile)
                                    .add(jRb_loadFromString))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(jB_load)))
                        .add(33, 33, 33))
                    .add(jp_newDBLayout.createSequentialGroup()
                        .add(127, 127, 127)
                        .add(jb_Connect_new)
                        .add(0, 0, Short.MAX_VALUE))))
            .add(jp_newDBLayout.createSequentialGroup()
                .add(127, 127, 127)
                .add(jPr_create, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, Short.MAX_VALUE))
        );
        jp_newDBLayout.setVerticalGroup(
            jp_newDBLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jp_newDBLayout.createSequentialGroup()
                .add(12, 12, 12)
                .add(jp_newDBLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jp_newDBLayout.createSequentialGroup()
                        .add(jlb_addr_new)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jtf_addr_new, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jp_newDBLayout.createSequentialGroup()
                        .add(jlb_port_new)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jtf_port_new, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jlb_dbName_new)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jtf_dbName_new, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jp_newDBLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jlb_usr_new)
                    .add(jlb_passw_new))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jp_newDBLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(jtf_usr_new, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jtf_passw_new, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(jp_newDBLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jp_newDBLayout.createSequentialGroup()
                        .add(18, 18, 18)
                        .add(jB_load)
                        .addContainerGap())
                    .add(jp_newDBLayout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(jRb_loadFromFile)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jRb_loadFromString)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPr_create, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(jb_Connect_new))))
        );

        jt_connect_type.addTab("Crea un nuovo Database", jp_newDB);

        jB_cancel.setText("Annulla");
        jB_cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jB_cancelActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jt_connect_type, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 430, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jB_cancel)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jt_connect_type)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jB_cancel)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress".equals(evt.getPropertyName())) {

            if (!jb_Connect_new.isEnabled()) { // barra di progressione pe nuovo server
                int progress = (Integer) evt.getNewValue();
                jPr_create.setValue(progress);
            }
            if (!jb_Connect.isEnabled()) { // modifica barra di progressione per connessione ad un server gia configurato
                int progress = (Integer) evt.getNewValue();
                jPr_create.setValue(progress);
            }
        }

    }

    private void jb_Connect_newActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jb_Connect_newActionPerformed

        user = this.jtf_usr_new.getText();
        password = new String(this.jtf_passw_new.getPassword());
        dbName = this.jtf_dbName_new.getText();
        url = this.jtf_addr_new.getText() + ":" + this.jtf_port_new.getText();

        if (user.isEmpty() || password.isEmpty() || dbName.isEmpty() || url.isEmpty()) {                        // Controlla se tutti i campi non son vuoti
            JOptionPane.showMessageDialog(this, "Campi incompleti", "Attenzione", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (this.jRb_loadFromFile.isSelected() && f == null) {
            JOptionPane.showMessageDialog(this, "Devi scegliere un file", "Attenzione", JOptionPane.WARNING_MESSAGE);
            return;
        }
        jPr_create.setVisible(true);
        jPr_create.setIndeterminate(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        jb_Connect_new.setEnabled(false);
        TaskCreate taskCreate = new TaskCreate();
        taskCreate.addPropertyChangeListener(this);
        taskCreate.execute();

    }//GEN-LAST:event_jb_Connect_newActionPerformed

    private void jb_ConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jb_ConnectActionPerformed
        // TODO add your handling code here:
        Connection conn = null;
        user = this.jtf_usr.getText();
        password = new String(this.jtf_passw.getPassword());
        dbName = this.jtf_dbName.getText();
        url = this.jtf_addr.getText() + ":" + this.jtf_port.getText() + "/" + dbName;

        if (user.isEmpty() || password.isEmpty() || dbName.isEmpty() || url.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Campi incompleti", "Attenzione", JOptionPane.WARNING_MESSAGE);
            return;
        }
        jPr_connect.setVisible(true);
        jPr_connect.setIndeterminate(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        jb_Connect.setEnabled(false);
        TaskConnect taskConnect = new TaskConnect();
        taskConnect.addPropertyChangeListener(this);
        taskConnect.execute();
    }//GEN-LAST:event_jb_ConnectActionPerformed

    private void jB_cancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jB_cancelActionPerformed
        // TODO add your handling code here:
        this.setVisible(false);
        Main.cw = null;
    }//GEN-LAST:event_jB_cancelActionPerformed

    private void jB_loadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jB_loadActionPerformed
        // TODO add your handling code here:
        if (jRb_loadFromFile.isSelected()) { // scelta del file contenente lo schema
            int showOpenDialog = chooser.showOpenDialog(this);
            if (showOpenDialog == JFileChooser.APPROVE_OPTION) {
                f = chooser.getSelectedFile();
            } else {
                jbG_load.clearSelection();
                jB_load.setEnabled(false);
            }
        }
        if (jRb_loadFromString.isSelected()) {
            JPanel jp = new JPanel(new BorderLayout()); // Creo un nuovo pannello
            JTextArea textArea = new JTextArea(30, 100);
            textArea.setFont(new Font("Arial", Font.PLAIN, 16));
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);

            JScrollPane areaScrollPane = new JScrollPane(textArea); // Aggiungo una textarea allo scroller
            areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            areaScrollPane.setPreferredSize(new Dimension(250, 250));

            jp.add(areaScrollPane, BorderLayout.CENTER); // Aggiungo lo scroller centrato al pannello
            int confirm = JOptionPane.showConfirmDialog(this, jp, "Inserisci lo Schema", JOptionPane.OK_CANCEL_OPTION);
            if (confirm == JOptionPane.OK_OPTION) {
                this.text = textArea.getText();
            } else {
                jbG_load.clearSelection();
                jB_load.setEnabled(false);
            }
        }
    }//GEN-LAST:event_jB_loadActionPerformed

    private void jRb_loadFromFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRb_loadFromFileActionPerformed
        // TODO add your handling code here:
        jB_load.setEnabled(true);
    }//GEN-LAST:event_jRb_loadFromFileActionPerformed

    private void jRb_loadFromStringActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRb_loadFromStringActionPerformed
        // TODO add your handling code here:
        jB_load.setEnabled(true);
    }//GEN-LAST:event_jRb_loadFromStringActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jB_cancel;
    private javax.swing.JButton jB_load;
    private javax.swing.JProgressBar jPr_connect;
    private javax.swing.JProgressBar jPr_create;
    private javax.swing.JRadioButton jRb_loadFromFile;
    private javax.swing.JRadioButton jRb_loadFromString;
    private javax.swing.ButtonGroup jbG_load;
    private javax.swing.JButton jb_Connect;
    private javax.swing.JButton jb_Connect_new;
    private javax.swing.JLabel jlb_addr;
    private javax.swing.JLabel jlb_addr_new;
    private javax.swing.JLabel jlb_dbName;
    private javax.swing.JLabel jlb_dbName_new;
    private javax.swing.JLabel jlb_passw;
    private javax.swing.JLabel jlb_passw_new;
    private javax.swing.JLabel jlb_port;
    private javax.swing.JLabel jlb_port_new;
    private javax.swing.JLabel jlb_usr;
    private javax.swing.JLabel jlb_usr_new;
    private javax.swing.JPanel jp_connect_DB;
    private javax.swing.JPanel jp_newDB;
    private javax.swing.JTabbedPane jt_connect_type;
    private javax.swing.JTextField jtf_addr;
    private javax.swing.JTextField jtf_addr_new;
    private javax.swing.JTextField jtf_dbName;
    private javax.swing.JTextField jtf_dbName_new;
    private javax.swing.JPasswordField jtf_passw;
    private javax.swing.JPasswordField jtf_passw_new;
    private javax.swing.JTextField jtf_port;
    private javax.swing.JTextField jtf_port_new;
    private javax.swing.JTextField jtf_usr;
    private javax.swing.JTextField jtf_usr_new;
    // End of variables declaration//GEN-END:variables
}
