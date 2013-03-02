/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package viewer.taskThread;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import viewer.Main;
import viewer.manuzioParser.Schema;
import viewer.manuzioParser.Type;
import org.postgresql.*;

/**
 * <p> Si occupa di aggiornare e gestire un
 * <code>javax.swing.JTree</code> ed eseguire l'output in un
 * <code>T extends JTextComponent</code> delle query automatiche eseguite
 * attraverso gli eventi associati</p>
 * <p>Il Thread termina automaticamente in caso che la Connessione al DB
 * termini.</p>
 * <p>Si ricorda che per eseguire questo Thread è necessario invocare il metodo
 * <code>start()</code></p>
 *
 * @author Nicola Preden, matricola 818578, Facoltà di informatica Ca' Foscari
 * in Venice
 */
public class TaskTree<T extends JTextComponent> extends Thread implements TreeSelectionListener {

    /**
     * Thread corrente
     */
    private volatile Thread thisThread;
    /**
     * Albero swing ed nodo root
     */
    private JTree tree;
    private DefaultMutableTreeNode root;
    /**
     * JTextComponent che si occupa di visualizzare l'output delle query
     * generate dai eventi associati
     */
    private T output;
    private Schema schema;
    /**
     * tempo di attesa da usare con le invocazioni di wait
     */
    private int attesa;
    /**
     * Variabile per attivare l'arresto del thread
     */
    private volatile boolean end;
    /**
     * Coda ThreadSafe per trasferire le richieste attraverso i nodi selezionati
     * da aprire
     */
    private LinkedTransferQueue<TreeNodeObject> queue;

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

        /* if nothing is selected */
        if (node == null) {
            return;
        }
        TreeNodeObject data = (TreeNodeObject) node.getUserObject();
        this.sendMessage(data);

    }

    /**
     * <p>Crea un oggetto contenenti i dati per identificare univocamente un
     * text object nel database. Il quale ha un metodo
     * <code>toString()</code> oppurtunamente modificato per poter essere usato
     * nel
     * <code>JTree</code> come etichetta dei nodi.</p>
     */
    protected class TreeNodeObject {

        private int id;
        private String type;
        private boolean isPlural;

        TreeNodeObject(int id, String type, boolean isPlural) {
            this.id = id;
            this.type = type;
            this.isPlural = isPlural;
        }

        int getId() {
            return id;
        }

        String getType() {
            return type;
        }

        boolean isIsPlural() {
            return isPlural;
        }

        @Override
        public String toString() {
            return "id=" + id + ", type=" + type;
        }
    }

    /**
     * <p>Costruttore del Task.</p>
     *
     * @param tree Albero swing nel quale sarà vosualizzata la struttura del
     * database
     * @param output oggetto che estendedo un JTextComponent visualizza
     * l'aoutput delle query
     * @param schema lo schema del database manuzio
     */
    public TaskTree(JTree tree, T output, Schema schema) {
        this.tree = tree;
        this.output = output;
        this.schema = schema;
        this.root = null;

        super.setName("TaskTree");
        this.attesa = 30;
        this.end = false;
        this.queue = new LinkedTransferQueue<TreeNodeObject>();
    }

    @Override
    public void run() {
        thisThread = Thread.currentThread();
        tree.setEditable(false);

        while (!end) {
            String q;
            Type maximalUnit = schema.getMaximalUnit();
            String typeNameMaxUnit = maximalUnit.getTypeName();
            Connection conn = null;
            PreparedStatement query = null;
            CallableStatement function = null; // da usarsi per le chiamate alle funzioni
            ResultSet res = null;
            try {
                // Carico la Radice dell'albero
                conn = Main.getConnection();
                if (conn != null) {  // Se è ancora disponibile una connessione al database
                    q = "SELECT"
                            + "  textual_objects.id_tex_obj,"
                            + "  textual_objects.type_name,"
                            + "  textual_objects.is_plural"
                            + "FROM"
                            + "  public.textual_objects"
                            + "WHERE"
                            + "  textual_objects.type_name = ?;";
                    query = conn.prepareStatement(q);
                    query.setString(1, typeNameMaxUnit);
                    res = query.getResultSet();
                    if (res.next()) {
                        // Il database ha almeno un elemento
                        root = new DefaultMutableTreeNode(new TreeNodeObject(res.getInt("id_tex_obj"), res.getString("type_name"), res.getBoolean("is_plural")));
                    } else {
                        // Il databse è completameto vuoto
                        root = null;
                    }
                } else {
                    // Se non che il database
                    end = true;
                    continue;
                }
            } catch (SQLException ex) {
                Logger.getLogger(TaskTree.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    Main.close(res);
                    Main.close(query);
                    Main.close(conn);
                } catch (SQLException ex) {
                    Logger.getLogger(TaskTree.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            if (root != null) {
                // Creo l'albero
                tree.setEnabled(false);
                tree.removeTreeSelectionListener(this);

                // pulisce il JTree
                tree.removeAll();

                // Caricamento del nuovo modello
                loadChild(root);

                DefaultTreeModel treeModel = new DefaultTreeModel(root);
                tree.setModel(treeModel);
                tree.addTreeSelectionListener(this);
                tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
                tree.setRootVisible(true);
                tree.setEnabled(true);

            } else {
                // Inerisco un segnaposto
                // Database Vuoto
            }
            try {
                // Attendo un tempo prefissato per riaggiornare l'albero ed attendo un comando
                TreeNodeObject poll = queue.poll(getWaitTime(), TimeUnit.SECONDS);
                if (poll != null) {
                    try {
                        // aggiorno contenuto finestra di output
                        conn = Main.getConnection();
                        
                        // lettura attributi
                        String text = "id= " + poll.getId() + " type= " + poll.getType() + "\n"
                                + "Attributi Textual Object: \n";
                        q = "SELECT "
                                + "  attribute_values.id_att_value, "
                                + "  attribute_types.label, "
                                + "  attribute_values.content AS value, "
                                + "  attribute_types.editable"
                                + "FROM "
                                + "  public.attribute_types, "
                                + "  public.attribute_values"
                                + "WHERE "
                                + "  attribute_types.id_att_type = attribute_values.id_att_type AND"
                                + "  attribute_values.id_tex_obj = ?;";
                        query = conn.prepareStatement(q);
                        query.setInt(poll.getId(), 1);
                        res = query.executeQuery();
                        while (res.next()) {
                            text+= "id_value= " + res.getInt("id_att_value") + " editable= " + res.getBoolean("editable")
                                    + "\tlabel= " + res.getString("label") + " value=" + res.getString("value") + "\n";
                        }
                        text+= "Fine Attributi\n INIZIO TESTO \n";
                        Main.close(res);
                        Main.close(query);
                        // lettura text object
                        
                        int[] arr = {poll.getId()};
                        q = "{ ? = call get_text_from_id(?) }";
                        function = conn.prepareCall(q);
                        function.registerOutParameter(1, Types.ARRAY);
                        function.setInt(2, arr);
                        
                        
                        

                    } catch (SQLException ex) {
                        Logger.getLogger(TaskTree.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        try {
                            Main.close(conn);
                            Main.close(query);
                            Main.close(res);
                        } catch (SQLException ex) {
                            Logger.getLogger(TaskTree.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(TaskTree.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        // Chiudo il processo

    }

    /**
     * <p>Popola l'albero a partire da
     * <code>id_tex_obj</code> che identifica il primo text object presente sul
     * database.</p>
     *
     * @param root la testa dell'albero
     * @return un albero importabile in un JTree
     */
    protected DefaultMutableTreeNode loadChild(DefaultMutableTreeNode root) {
        return null;
    }

    protected boolean insert() {
        return false;

    }

    /**
     * <p>Ferma il Thread, deassocia tutti gli eventi aggiunti e azzera il
     * contenuto dell'albero</p>
     * <code>javax.swing.JTree</code> e chiude la connessione al DB</p>
     */
    public synchronized void stopThread() {
        if (!end) {
            end = true;
        }
    }

    /**
     * <p>Metodo ThreadSafe per accedere al attuale tempo d'atesa impostato.</p>
     *
     * @return
     */
    protected synchronized int getWaitTime() {
        return this.attesa;
    }

    /**
     * <p>Specifica il tempo di Refrash sulla struttura dell albero.</p>
     *
     * @param sec
     */
    public synchronized void waitTime(int sec) {
        this.attesa = sec;
    }

    synchronized boolean sendMessage(TreeNodeObject s) {
        return queue.offer(s);
    }
}
