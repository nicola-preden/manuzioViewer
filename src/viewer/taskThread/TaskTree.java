/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package viewer.taskThread;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Enumeration;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JSlider;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import viewer.Main;
import viewer.manuzioParser.Schema;

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
     * Richiede il refresh
     */
    private static final int REFRESH = -1;
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
     * slider responsabile di gestire il numero di sotto alberi aperti
     */
    private final JSlider slider;
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
    /**
     * indica l'altezza dell'albero
     */
    private int level = 0;

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
     * @param slider lo slider che gestisce il livello di dettaglio visibile del
     * jtree
     */
    public TaskTree(JTree tree, T output, Schema schema, JSlider slider) {
        this.tree = tree;
        this.output = output;
        this.schema = schema;
        this.slider = slider;
        //  this.slider;
        this.root = null;

        super.setName("TaskTree");
        this.attesa = 60;
        this.end = false;
        this.queue = new LinkedTransferQueue<TreeNodeObject>();
    }

    /**
     * Avvia il Thread
     */
    public void startThread() {
        this.start();
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

        /* if nothing is selected */
        if (node == null) {
            return;
        }

        TreeNodeObject data;
        Object userObject = node.getUserObject();
        if (userObject instanceof String) {
        } else {
            data = (TreeNodeObject) node.getUserObject();
            this.sendMessage(data);
        }
        tree.clearSelection();
    }

    @Override
    public void run() {
        thisThread = Thread.currentThread();
        tree.setEditable(false);

        while (!endValue()) {
            String q;
            Connection conn = null;
            PreparedStatement query = null;
            CallableStatement function = null; // da usarsi per le chiamate alle funzioni
            ResultSet res = null;
            try {
                // Carico la Radice dell'albero
                conn = Main.getConnection();
                if (conn != null) {  // Se è ancora disponibile una connessione al database
                    // Creo la Radice
                    String[] temp = conn.getMetaData().getURL().split("://");

                    root = new DefaultMutableTreeNode(temp[1]);
                } else {
                    // Se la connessione è caduta chiudo il thread
                    stopThread();
                    continue;
                }
            } catch (SQLException ex) {
                Logger.getLogger(TaskTree.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    Main.close(conn);
                } catch (SQLException ex) {
                    Logger.getLogger(TaskTree.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            if (root != null) { // Controllo superfluo ma meglio esere sicuri
                // Creo l'albero
                slider.setEnabled(false);
                tree.setEnabled(false);
                tree.removeTreeSelectionListener(this);


                // Caricamento del nuovo modello
                level = 0;
                loadChild();

                // Inserisco il nuovo modello
                DefaultTreeModel treeModel = new DefaultTreeModel(root);
                tree.setModel(treeModel);
                tree.addTreeSelectionListener(this);
                tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
                tree.setRootVisible(true);
                tree.setEnabled(true);

                // Aggiorno lo slider
                slider.setMaximum(level);
                slider.setEnabled(true);
                slider.setValue(1);
            }
            try {
                // Attendo un tempo prefissato per riaggiornare l'albero ed attendo un comando
                TreeNodeObject poll = queue.poll(attesa, TimeUnit.SECONDS);
                if (poll != null) {
                    if (poll.getId() == TaskTree.REFRESH) {
                        continue;
                    }
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
                                + "  attribute_types.editable "
                                + "FROM "
                                + "  public.attribute_types, "
                                + "  public.attribute_values "
                                + "WHERE "
                                + "  attribute_types.id_att_type = attribute_values.id_att_type AND"
                                + "  attribute_values.id_tex_obj = ?;";
                        query = conn.prepareStatement(q);
                        query.setInt(1, poll.getId());
                        res = query.executeQuery();
                        while (res.next()) {
                            text += "id_value= " + res.getInt("id_att_value") + " editable= " + res.getBoolean("editable")
                                    + "\tlabel= " + res.getString("label") + " value=" + res.getString("value") + "\n";
                        }
                        text += "Fine Attributi\n INIZIO TESTO \n";
                        Main.close(res);
                        Main.close(query);
                        // lettura text object

                        Object[] arr = {poll.getId()};
                        q = "{ ? = call get_text_from_id(?) }";
                        Array t = conn.createArrayOf("integer", arr);
                        function = conn.prepareCall(q);
                        function.registerOutParameter(1, Types.ARRAY);
                        function.setArray(2, t);
                        function.execute();
                        Array array = function.getArray(1);
                        // ottendo un resultSet dove la prima colonna è in indice mentre la secondo il valore
                        res = array.getResultSet();

                        while (res.next()) {
                            text += res.getString(2);
                        }

                        // Aggiorno contenuto finestra
                        output.setText(null);
                        output.setText(text);
                    } catch (SQLException ex) {
                        Logger.getLogger(TaskTree.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        try {
                            Main.close(conn);
                            Main.close(function);
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
        DefaultMutableTreeNode node = new javax.swing.tree.DefaultMutableTreeNode("Server Disconnesso");
        tree.setModel(new DefaultTreeModel(node));
        tree.removeTreeSelectionListener(this);
        slider.setMaximum(10);
        slider.setEnabled(false);
        slider.setValue(1);
        output.setText(null);
    }

    /**
     * <p>Popola l'albero inserendo i vari
     * <code>textual object</code> come figli della radiece dell albero,
     * rispettando la loro struttura.</p>
     *
     */
    private void loadChild() {
        String q = "SELECT"
                + "  textual_objects.id_tex_obj,"
                + "  textual_objects.type_name,"
                + "  textual_objects.is_plural"
                + " FROM "
                + "  public.textual_objects"
                + " WHERE "
                + "  textual_objects.type_name = ? "
                + " ORDER BY"
                + "  textual_objects.start ASC;";

        Connection conn = null;
        PreparedStatement query = null;
        ResultSet res = null;

        // Genero il primo livello dell'albero contenente i maxUnit
        try {
            conn = Main.getConnection();
            query = conn.prepareStatement(q);
            query.setString(1, schema.getMaximalUnit().getTypeName());
            res = query.executeQuery();
            while (res.next()) {
                // Creo un nuovo userObject per il DefaultMutableTreeNode
                TreeNodeObject userObject = new TreeNodeObject(res.getInt("id_tex_obj"), res.getString("type_name"), res.getBoolean("is_plural"));
                DefaultMutableTreeNode node = new DefaultMutableTreeNode();
                // inserisco l'userObject nel nodo
                node.setUserObject(userObject);
                // aggiungo il nodo
                root.add(node);
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
        // Ottengo un enumerazione dei figli e per ognuno di loro chiamo
        // una funzione ricorsiva che carica tutti i figli

        Enumeration children = root.children();
        while (children.hasMoreElements()) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
            int x = insert(child);
            if (x > level) {
                level = x;
            }
        }
    }

    /**
     * <p>Inserisce tutti i figli di
     * <code>head</code> fino a raggiungere le foglie ricorsivamente.</p>
     * <p>Il metodo si ferma se si ha raggiunto il minimo dello schema oppure se
     * <code>head</code> non ha figli</p>
     *
     * @param head un nodo dell'albero
     */
    protected int insert(DefaultMutableTreeNode head) {

        if (head == null) {
            return 0;
        }
        String q = "SELECT "
                + "  textual_objects.id_tex_obj, "
                + "  textual_objects.type_name, "
                + "  textual_objects.is_plural "
                + "FROM "
                + "  public.objects_composition, "
                + "  public.textual_objects "
                + "WHERE "
                + "  objects_composition.id_contained = textual_objects.id_tex_obj AND"
                + "  objects_composition.id_container = ? "
                + "ORDER BY "
                + "  objects_composition.pos ASC;";
        Connection conn = null;
        PreparedStatement query = null;
        ResultSet res = null;

        String headType = ((TreeNodeObject) head.getUserObject()).getType();    // tipo del padre
        int headId = ((TreeNodeObject) head.getUserObject()).getId();           // id del padre
        if (schema.getMinimalUnit().getTypeName().compareTo(headType) == 0) {
            return 1;
        }
        try {
            // Eseguo la query per ottenere tutti i figli di head
            conn = Main.getConnection();
            query = conn.prepareStatement(q);
            query.setInt(1, headId);
            res = query.executeQuery();

            while (res.next()) {
                // Creo un nuovo userObject per il DefaultMutableTreeNode
                TreeNodeObject userObject = new TreeNodeObject(res.getInt("id_tex_obj"), res.getString("type_name"), res.getBoolean("is_plural"));
                DefaultMutableTreeNode node = new DefaultMutableTreeNode();
                // inserisco l'userObject nel nodo
                node.setUserObject(userObject);
                // aggiungo il nodo
                head.add(node);
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
        // Ottengo un enumerazione dei figli e per ognuno di loro chiamo
        // una funzione ricorsiva che carica tutti i figli
        int max = 0;
        Enumeration children = head.children();
        while (children.hasMoreElements()) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
            int x = insert(child);
            if (x > max) {
                max = x;
            }
        }
        return max + 1;
    }

    /**
     * <p>Ferma il Thread, deassocia tutti gli eventi aggiunti e azzera il
     * contenuto dell'albero</p>
     * <code>javax.swing.JTree</code> e chiude la connessione al DB</p>
     */
    public synchronized void stopThread() {
        if (!endValue()) {
            end = true;
            refresh(); // per evitare l'attesa di 30 sec nella peggiore delle ipotesi
        }
    }

    protected synchronized boolean endValue() {
        return end;
    }

    /**
     * <p>Richiede un refrash sulla struttura dell albero.</p>
     *
     * @param sec
     */
    public synchronized void refresh() {
        queue.offer(new TreeNodeObject(TaskTree.REFRESH, null, false));
    }

    synchronized boolean sendMessage(TreeNodeObject s) {
        return queue.offer(s);
    }
}
