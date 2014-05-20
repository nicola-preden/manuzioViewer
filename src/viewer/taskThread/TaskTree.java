package viewer.taskThread;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.ListIterator;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.JSlider;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import viewer.ManuzioViewer;
import viewer.manuzioParser.Schema;
import viewer.setting.NodeSettingInterface;
import viewer.setting.SettingXML;

/**
 * <p> Si occupa di aggiornare e gestire un <tt>javax.swing.JTree</tt>
 * ed eseguire l'output in un <tt>T extends JTextComponent</tt> delle query
 * automatiche eseguite attraverso gli eventi associati. </p>
 * <p>Il Thread termina automaticamente in caso che la Connessione al DB
 * termini.</p>
 * <p>Si ricorda che per eseguire questo Thread è necessario invocare il metodo
 * <tt>start()</tt></p>
 *
 * @author Nicola Preden, matricola 818578, Facoltà di informatica Ca' Foscari
 * in Venice
 */
public class TaskTree<T extends JEditorPane> extends Thread implements TreeSelectionListener {

    /**
     * Richiede il refresh
     */
    private static final int REFRESH = -1;
    private static final ResourceBundle lang = ResourceBundle.getBundle("viewer/language/lang", ManuzioViewer.LANGUAGE);
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
    private final SettingXML setting;

    /**
     * <p>Crea un oggetto contenenti i dati per identificare univocamente un
     * text object nel database. Il quale ha un metodo <tt>toString()</tt>
     * oppurtunamente modificato per poter essere usato nel <tt>JTree</tt>
     * come etichetta dei nodi.</p>
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
    public TaskTree(JTree tree, T output, Schema schema, JSlider slider, SettingXML setting) {
        this.tree = tree;
        this.output = output;
        this.schema = schema;
        this.slider = slider;
        this.setting = setting;
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
        tree.setEditable(false);

        while (!endValue()) {
            String q;
            Connection conn = null;
            PreparedStatement query = null;
            CallableStatement function = null; // da usarsi per le chiamate alle funzioni
            ResultSet res = null;
            try {
                // Carico la Radice dell'albero
                conn = ManuzioViewer.getConnection();
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
                    ManuzioViewer.close(conn);
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
                    NodeSettingInterface set = setting.getSetting("SchemaList");
                    String jdbcUrl = ManuzioViewer.getJdbcUrl();
                    Properties t = null;
                    if (set != null) {
                        ListIterator<Properties> readProp = set.readProp();
                        while (readProp.hasNext()) {
                            Properties next = readProp.next();
                            String property = next.getProperty("schema-url");
                            if (property.compareTo(jdbcUrl) == 0) {
                                t = next;
                                break;
                            }
                        }
                        if (t == null) {
                            t = new Properties();
                            t.setProperty("schema-url", ManuzioViewer.getJdbcUrl());
                            setting.addSetting("SchemaList", t);
                        }
                    } else {
                        t = new Properties();
                        t.setProperty("schema-url", ManuzioViewer.getJdbcUrl());
                        setting.addSetting("SchemaList", t);
                    }
                    TextualLayout.createTextualLayout(poll.getId(), output, schema, t).execute();
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(TaskTree.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        // Chiudo il processo
        DefaultMutableTreeNode node = new javax.swing.tree.DefaultMutableTreeNode(lang.getString("SERVER_OFFLINE"));
        tree.setModel(new DefaultTreeModel(node));
        tree.removeTreeSelectionListener(this);
        slider.setMaximum(10);
        slider.setEnabled(false);
        slider.setValue(1);
        output.setText(null);
    }

    /**
     * <p>Popola l'albero inserendo i vari <tt>textual object</tt> come figli
     * della radiece dell albero, rispettando la loro struttura. </p>
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
            conn = ManuzioViewer.getConnection();
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
                ManuzioViewer.close(res);
                ManuzioViewer.close(query);
                ManuzioViewer.close(conn);
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
     * <p>Inserisce tutti i figli di <tt>head</tt> fino a raggiungere le foglie
     * ricorsivamente. </p> <p>Il metodo si ferma se si ha raggiunto il minimo
     * dello schema oppure se <tt>head</tt> non ha figli</p>
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
            conn = ManuzioViewer.getConnection();
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
                ManuzioViewer.close(res);
                ManuzioViewer.close(query);
                ManuzioViewer.close(conn);
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
     * contenuto dell'albero <tt>javax.swing.JTree</tt> e chiude la connessione
     * al DB. </p>
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
     */
    public synchronized void refresh() {
        queue.offer(new TreeNodeObject(TaskTree.REFRESH, null, false));
    }

    private synchronized boolean sendMessage(TreeNodeObject s) {
        return queue.offer(s);
    }
}
