/**
 * @author Nicola Preden, matricola 818578, Facoltà di informatica Ca' Foscari
 * in Venice
 */
package viewer;

import com.apple.eawt.AboutHandler;
import com.apple.eawt.AppEvent;
import com.apple.eawt.Application;
import com.apple.eawt.PreferencesHandler;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;
import com.jolbox.bonecp.BoneCP;
import database.ConnectionPoolException;
import database.ConnectionPoolFactory;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import viewer.manuzioParser.Schema;
import viewer.setting.SettingXML;
import viewer.taskThread.TaskTree;

/**
 * <p>lasse contenete il main e i metodi per inizializzare il database</p>
 *
 * @author Nicola Preden, matricola 818578, Facoltà di informatica Ca' Foscari
 * in Venice
 */
public class ManuzioViewer {

    private static class Timer extends Thread {

        @Override
        public void run() {
            super.setName("Timer");
            while (true) {
                try {
                    Thread.sleep(10000);
                    System.gc();
                } catch (InterruptedException ex) {
                    Logger.getLogger(ManuzioViewer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    private static volatile boolean isConnect = false;
    private static BoneCP connPool = null;                              // Pool Connessione al DB
    static MainWindow mw = null;                                // Finestra Principale
    static ConnectWindow cw = null;                             // Finestra di login
    static SettingXML setting = null;                           // Struttura configurazione
    static Schema schema = null;
    static TaskTree taskTree = null;
    private static final String urlXml = "settings.xml";        // File di Configurazione
    private static Timer tm = new Timer();
    private static final double VERSION_Manuzio = 3.2;
    private static final String APP_NAME = "ManuzioViewer";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //<editor-fold defaultstate="collapsed" desc="regexTest">
        /*
        String w = "\\p{Graph}+";
        Pattern p = Pattern.compile(w, Pattern.UNICODE_CHARACTER_CLASS);
        Pattern z = Pattern.compile("(\\p{Punct})|(\\p{Alnum}+)|(\\p{Punct})", Pattern.UNICODE_CHARACTER_CLASS);

        Matcher m = p.matcher("1. Evocatio\n"
                + "Scendeva la sera mentre i Frati dell’Ordine della Spada si disponevano "
                + "per la parata annuale della Vigilia di Ognissanti. I cavalli "
                + "sbuffavano attendendo che i cavalieri li guidassero per le "
                + "vie della città; i palafrenieri e i novizi avrebbero "
                + "seguito il corteo a piedi.\n"
                + "«Compieta».\n"
                + "Eloise Weiss. Annuì senza badare eccessivamente alla tangibile "
                + "nota d’inquietudine nella voce di Christabel Von Sayn, "
                + "sottile e gentile come un raggio d’argento, alla luce fioca "
                + "delle candele che illuminavano la stanza.\n");
        Matcher r;
        while (m.find()) {
            String t = m.group();
            System.out.println("Gruppo : " + t);
            r = z.matcher(t);
            while (r.find()) {
                String k = r.group();
                System.out.println("\tSub : " + k + " > " + k.length());
            }
        }
        System.exit(0);
        */
        //</editor-fold>
        try {
            if (isOSX()) {  // Se siamo su Mac
                // Per risolvere il bug relativo alla prima property è stato 
                // semplicemente cambiato il nome della classe
                System.setProperty("com.apple.mrj.application.apple.menu.about.name", APP_NAME);
                System.setProperty("apple.awt.graphics.EnableQ2DX", "true");
                System.setProperty("apple.laf.useScreenMenuBar", "true");


                Application macApp = Application.getApplication();
                // Questi eventi vengono usati solo su os x
                // vengono usati per spostare gli eventi di 
                // chiusura, about e preference sulla Menubar di Os X
                macApp.setAboutHandler(new AboutHandler() {
                    @Override
                    public void handleAbout(AppEvent.AboutEvent ae) {
                        AboutWindow aboutWindow = new viewer.AboutWindow();
                        aboutWindow.setVisible(true);
                    }
                });
                macApp.setPreferencesHandler(new PreferencesHandler() {
                    @Override
                    public void handlePreferences(AppEvent.PreferencesEvent pe) {
                        PreferenceWindow preferenceWindow = new viewer.PreferenceWindow();
                        preferenceWindow.setVisible(true);
                    }
                });
                macApp.setQuitHandler(new QuitHandler() {
                    @Override
                    public void handleQuitRequestWith(AppEvent.QuitEvent qe, QuitResponse qr) {
                        int showConfirmDialog = JOptionPane.showConfirmDialog(mw, "Vuoi davvero chiudere il programma?", "Sei Sicuro?", JOptionPane.YES_NO_OPTION);
                        switch (showConfirmDialog) {
                            case JOptionPane.YES_OPTION:
                                ManuzioViewer.shutdownProgram();
                                break;
                            case JOptionPane.NO_OPTION:
                                qr.cancelQuit();
                                break;
                            default:
                                qr.cancelQuit();
                                break;
                        }
                    }
                });
            } else {
                for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        // Cambio Look & Feel
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(ManuzioViewer.class.getName()).log(Level.SEVERE, null, ex);
        }
        setting = new SettingXML(urlXml);
        tm.start();
        mw = new MainWindow();
        mw.setVisible(true);


    }

    static boolean isOSX() {
        String osName = System.getProperty("os.name");
        return osName.contains("OS X");
    }

    /**
     * <p>Setta il il puntatore al thread riguardante l'aggiornamento della
     * rappresentazione grafica del server</p>
     *
     * @param tree
     */
    static synchronized void setTaskTree(TaskTree tree) {
        taskTree = tree;
    }

    /**
     * <p>Fornisce indicazione se è disponibile la connessione ad un server</p>
     *
     * @return <tt>TRUE</tt> se connesso altrimenti <tt>FALSE</tt>
     */
    static synchronized boolean connectionIsSet() {
        return isConnect;
    }

    /**
     * <p>Imposta un nuovo ConnectionPool per generare un nuovo ConnectionPool è
     * necessario chiamare il metodo
     * <tt>viewer.ManuzioViewer.shutdownConnectionPool</tt></p>
     *
     * @param url Indirizzo al server secondo la * * * * * * * * * * struttura
     * <tt>jdbc:postgresql://IP:PORT/DB_NAME</tt>
     * @param user
     * @param password
     * @throws ConnectionPoolException
     */
    static synchronized void setConnectionPool(String url, String user, String password) throws ConnectionPoolException {
        if (!isConnect) {
            ConnectionPoolFactory cpf = new ConnectionPoolFactory(url, user, password);
            connPool = cpf.createConnectionPool();
            isConnect = true;
        }
    }

    /**
     * <p>Ritorna una connessione dal connection pool se disponibile</p>
     *
     * @return
     * @throws SQLException
     */
    static public synchronized Connection getConnection() throws SQLException {
        if (isConnect) {
            return connPool.getConnection();
        }
        return null;
    }

    /**
     * <p>Chiude il connectionPool e chiude tutte le connessioni aperte al
     * momento della chimata</p>
     *
     * @return <tt>TRUE</tt> se l'operazione ha successo
     */
    static synchronized boolean shutdownConnectionPool() {
        if (isConnect) {
            if (taskTree != null) {
                taskTree.stopThread();
                try {
                    taskTree.join();
                    taskTree = null;
                } catch (InterruptedException ex) {
                    Logger.getLogger(ManuzioViewer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            connPool.shutdown();
            isConnect = false;
            connPool = null;
            return true;
        } else {
            return false;
        }
    }

    /**
     * <p>Chiude il programma. </p>
     */
    static void shutdownProgram() {
        shutdownConnectionPool();
        setting.saveOnFile();
        System.exit(0);
    }

    /**
     * <p>Gets the version of the Manuzio Language and the relative database</p>
     *
     * @return - a double representing the version
     */
    public static double getVersion() {
        return (ManuzioViewer.VERSION_Manuzio);
    }

    /**
     * <p>Builds an empty Manuzio database version 3.1 on the server
     * <tt>url</tt> with the given name
     * <tt>dbName</tt></p> <p>If
     * <tt>override = true</tt> and already exists a database with the given
     * name, then tries to delete and substitute it with a new database</p>
     *
     * @param url the server path -either of the * * * * * * * * * * * * * form
     * <tt>jdbc:subprotocol:serverPath</tt>, or only the serverPath itself
     * @param dbName the name given to the new database
     * @param user the username to log in the server
     * @param password - the password used to log in the server using the
     * account of <tt>user</tt>
     * @param override specifies if a database already existing with the given
     * has to be overridden or not.
     * @return a Connection Object with the database just created
     * @throws SQLException if a server error occurs.
     */
    public static boolean buildManuzioDB(String url, String dbName, String user, String password, boolean override) throws SQLException {
        final String MARKER = "-----"; //to delimiter a query in the file
        String file = "functions";	//filename = functions_ + subprotocol
        Scanner scan = null;
        Statement query = null; //query
        SQLException err = null;	//to catch an error during the drop of the old db
        Connection conn = null;


        //builds the db. If it already exists and override=true, then deletes it and builds a new one 
        try {
            if (override) {
                try {
                    deleteManuzioDB(url, dbName, user, password);
                } catch (SQLException e) {
                    err = e;
                } //probably the db does not exist
            }
            conn = ConnectionPoolFactory.getConnection(url, user, password); //connects to the server
            query = conn.createStatement();
            query.executeUpdate("CREATE DATABASE \"" + dbName + '"');

            //builds the name of the database's specific functions file
            String[] url_temp = conn.getMetaData().getURL().split(":");
            file += "_" + url_temp[1];

        } catch (SQLException e) {
            if (override && err != null) {
                throw err;	//there was an exception during the db drop
            } else {
                throw e;
            }
        } finally {
            close(query);
            close(conn);
        }

        final List<String> q = new ArrayList<String>();	//memorizes the query

        //query to build sequences
        q.add("CREATE SEQUENCE id_att_val_seq INCREMENT 1 MINVALUE 1  START 1 CACHE 1");
        q.add("CREATE SEQUENCE id_tex_obj_seq INCREMENT 1 MINVALUE 1  START 1 CACHE 1");
        q.add("CREATE SEQUENCE id_text_occ_seq INCREMENT 1 MINVALUE 1  START 1 CACHE 1");
        q.add("CREATE SEQUENCE id_text_seq INCREMENT 1 MINVALUE 1  START 1 CACHE 1");
        q.add("CREATE SEQUENCE id_user_seq INCREMENT 1 MINVALUE 1  START 1 CACHE 1");
        q.add("CREATE SEQUENCE id_att_type_seq INCREMENT 1 MINVALUE 1  START 1 CACHE 1");
        q.add("CREATE SEQUENCE id_method_seq INCREMENT 1 MINVALUE 1 START 1 CACHE 1");

        //query to build tables
        q.add("CREATE TABLE types "
                + "(type_name character varying NOT NULL, plural_name character varying NOT NULL, extends character varying, description text, "
                + "CONSTRAINT \"types PK\" PRIMARY KEY (type_name), "
                + "CONSTRAINT \"types FK\" FOREIGN KEY (extends) REFERENCES types (type_name), "
                + "CONSTRAINT \"types U\" UNIQUE (plural_name), "
                + "CONSTRAINT \"types_not_empty CHK\" CHECK (type_name <> ''::bpchar AND plural_name <> ''::bpchar))");
        q.add("CREATE TABLE schema "
                + "(schema_name character varying NOT NULL, max_unit character varying NOT NULL, min_unit character varying NOT NULL, description text, "
                + "db_version numeric NOT NULL, \"timestamp\" timestamp(6) without time zone NOT NULL DEFAULT now(), "
                + "CONSTRAINT \"schema PK\" PRIMARY KEY (schema_name), "
                + "CONSTRAINT \"schema_max_unit FK\" FOREIGN KEY (max_unit) REFERENCES types (type_name), "
                + "CONSTRAINT \"schema_min_unit FK\" FOREIGN KEY (min_unit) REFERENCES types (type_name), "
                + "CONSTRAINT \"schema CHK\" CHECK (schema_name <> ''::bpchar))");
        q.add("CREATE TABLE type_composition "
                + "(container_type character varying NOT NULL, contained_type character varying NOT NULL, component_name character varying NOT NULL, "
                + "optional boolean NOT NULL DEFAULT false, is_plural boolean NOT NULL, "
                + "CONSTRAINT \"type_composition PK\" PRIMARY KEY (container_type, component_name ), "
                + "CONSTRAINT \"type_contained FK\" FOREIGN KEY (contained_type) REFERENCES types (type_name), "
                + "CONSTRAINT \"type_container FK\" FOREIGN KEY (container_type) REFERENCES types (type_name), "
                + "CONSTRAINT \"type_composition CHK\" CHECK (component_name <> ''::bpchar))");
        q.add("CREATE TABLE attribute_types "
                + "(id_att_type integer NOT NULL DEFAULT nextval('id_att_type_seq'::regclass), type_name character varying NOT NULL, label character varying NOT NULL, "
                + "type character varying NOT NULL, is_plural boolean NOT NULL DEFAULT false, editable boolean NOT NULL, "
                + "CONSTRAINT \"att_types PK\" PRIMARY KEY (id_att_type), "
                + "CONSTRAINT \"att_types U\" UNIQUE (type_name, label ), "
                + "CONSTRAINT \"att_types FK\" FOREIGN KEY (type_name) REFERENCES types (type_name), "
                + "CONSTRAINT \"attribute_types CHK\" CHECK (label <> ''::bpchar AND type <> ''::bpchar))");
        q.add("CREATE TABLE methods "
                + "(id_method integer NOT NULL DEFAULT nextval('id_method_seq'::regclass), type_name character varying NOT NULL, label character varying NOT NULL, "
                + "syntax text NOT NULL, code bytea, is_plural boolean NOT NULL DEFAULT false, "
                + "CONSTRAINT \"methods PK\" PRIMARY KEY (id_method), "
                + "CONSTRAINT \"methods U\" UNIQUE (type_name, label), "
                + "CONSTRAINT \"methods FK\" FOREIGN KEY (type_name) REFERENCES types (type_name), "
                + "CONSTRAINT \"methods CHK\" CHECK (label <> ''::bpchar AND syntax <> ''::text))");
        q.add("CREATE TABLE textual_objects "
                + "(id_tex_obj integer NOT NULL DEFAULT nextval('id_tex_obj_seq'::regclass), type_name character varying NOT NULL, "
                + "is_plural boolean NOT NULL, start integer NOT NULL, label character varying, "
                + "CONSTRAINT \"tex_obj PK\" PRIMARY KEY (id_tex_obj), "
                + "CONSTRAINT \"tex_obj FK\" FOREIGN KEY (type_name) REFERENCES types (type_name))");
        q.add("CREATE TABLE objects_composition "
                + "(id_container integer NOT NULL, id_contained integer NOT NULL, pos integer NOT NULL, ty_of_relation boolean NOT NULL, "
                + "CONSTRAINT \"obj_comp PK\" PRIMARY KEY (id_container , id_contained ), "
                + "CONSTRAINT \"obj_comp_contained FK\" FOREIGN KEY (id_contained) REFERENCES textual_objects (id_tex_obj), "
                + "CONSTRAINT \"obj_comp_container FK\" FOREIGN KEY (id_container) REFERENCES textual_objects (id_tex_obj))");
        q.add("CREATE TABLE users "
                + "(id_user integer NOT NULL DEFAULT nextval('id_user_seq'::regclass), lastname character varying NOT NULL, "
                + "firstname character varying NOT NULL, nickname character(10) NOT NULL, "
                + "CONSTRAINT \"users PK\" PRIMARY KEY (id_user), "
                + "CONSTRAINT \"users U\" UNIQUE (nickname))");
        q.add("CREATE TABLE attribute_values "
                + "(id_att_value integer NOT NULL DEFAULT nextval('id_att_val_seq'::regclass),content text NOT NULL, "
                + "\"timestamp\" timestamp(6) without time zone NOT NULL DEFAULT NULL::timestamp without time zone, id_user integer, "
                + "id_tex_obj integer NOT NULL, id_att_type integer NOT NULL,"
                + "CONSTRAINT \"att_val PK\" PRIMARY KEY (id_att_value), "
                + "CONSTRAINT \"att_val_att FK\" FOREIGN KEY (id_att_type) REFERENCES attribute_types (id_att_type), "
                + "CONSTRAINT \"att_val_tex_obj FK\" FOREIGN KEY (id_tex_obj) REFERENCES textual_objects (id_tex_obj), "
                + "CONSTRAINT \"att_val_user FK\" FOREIGN KEY (id_user) REFERENCES users (id_user))");
        q.add("CREATE TABLE texts "
                + "(id_text integer NOT NULL DEFAULT nextval('id_text_seq'::regclass), cached_text text NOT NULL, lenght integer NOT NULL, "
                + "CONSTRAINT \"text PK\" PRIMARY KEY (id_text), "
                + "CONSTRAINT \"text U\" UNIQUE (cached_text))");
        q.add("CREATE TABLE text_occurence "
                + "(id_text_occurence integer NOT NULL DEFAULT nextval('id_text_occ_seq'::regclass), id_text integer NOT NULL, "
                + "start integer NOT NULL, \"end\" integer NOT NULL, "
                + "CONSTRAINT \"text_occ PK\" PRIMARY KEY (id_text_occurence ), "
                + "CONSTRAINT \"text_occ FK\" FOREIGN KEY (id_text) REFERENCES texts (id_text))");
        q.add("CREATE TABLE elements "
                + "(id_tex_obj integer NOT NULL, id_text_occurence integer NOT NULL, "
                + "CONSTRAINT \"elements PK\" PRIMARY KEY (id_tex_obj , id_text_occurence ), "
                + "CONSTRAINT \"elements _tex_obj FK\" FOREIGN KEY (id_tex_obj) REFERENCES textual_objects (id_tex_obj), "
                + "CONSTRAINT \"elements_occ FK\" FOREIGN KEY (id_text_occurence) REFERENCES text_occurence (id_text_occurence))");
        q.add("INSERT INTO users (id_user, lastname, firstname, nickname) VALUES (0, 'Admin', 'Admin', 'Admin')");


        //loads database specific functions
        try {
            scan = new Scanner(new java.io.File(file));
        } catch (FileNotFoundException e1) {
            deleteManuzioDB(url, "man_DB " + dbName, user, password);
            throw new SQLException("Cannot find system file '" + file + "'");
        }
        String fun = "";
        while (scan.hasNext()) {
            String s = scan.nextLine();
            if (s.equals(MARKER)) {
                q.add(fun);
                fun = "";
                continue;
            }
            fun += s + "\n";
        }
        scan.close();

        try {	//builds the db
            conn = ConnectionPoolFactory.getConnection(url + "/" + dbName, user, password); //connects to db just created
            query = conn.createStatement();

            //if the db support batch updates, then it is more efficient doing a batch update instead of doing the querys one at time
            if (conn.getMetaData().supportsBatchUpdates()) {
                for (int i = 0; i < q.size(); i++) {
                    query.addBatch((q.get(i)));
                }
                int[] executeBatch = query.executeBatch();
            } else {
                for (int i = 0; i < q.size(); i++) {
                    query.executeUpdate(q.get(i));
                }
            }
        } catch (SQLException e) {
            //closes resources and deletes the bad-formed db
            close(query);
            close(conn);
            deleteManuzioDB(url, dbName, user, password);
            throw e;
        } finally {
            close(query);
            close(conn);
        }
        return true;
    }

    /**
     * <p>Deletes the database with the given name
     * <tt>dbName</tt> from the server.</p> <p>Despite to this method's name,
     * this method could be used to delete any database, not only a Manuzio
     * one.</p>
     *
     * @param url the server path -either of the * * * * * * * * * * * * * form
     * <tt>jdbc:subprotocol:serverPath</tt>, or only the serverPath itself
     * @param dbName - the name of the database to delete
     * @param user the username to connect to the server
     * @param password the password related to the <tt>user</tt> to connect to
     * the server
     * @throws SQLException if the database couldn't be deleted for any reason
     */
    public static void deleteManuzioDB(String url, String dbName, String user, String password) throws SQLException {
        Connection conn = ConnectionPoolFactory.getConnection(url, user, password); //connects to the server
        try {
            conn.createStatement().executeUpdate("DROP DATABASE \"" + dbName + "\";");
        } catch (SQLException e) {
            throw new SQLException("FATAL " + e.getMessage() + "\nWARNING: The database just created may be in a incosistent status.");
        } finally {
            close(conn);
        }
    }

    /**
     * <p>Empties the given Manuzio database.</p> <p>NOTE: the database must
     * have a Manuzio DB version 3.1 structure</p>
     *
     * @param url - the location and the name of the DB to empty
     * @param user the username to log in the server
     * @param password - the password used to log in the server using the
     * account of <tt>user</tt>
     * @throws SQLException - can't connect to the given DB. Maybe the path, the
     * user or the password are wrong
     * @throws ParseException - the given DB hasn't a Manuzio 3.1 structure
     */
    public static void emptyDB(String url, String user, String password) throws SQLException, ParseException {
        Connection conn = ConnectionPoolFactory.getConnection(url, user, password); //connects to the server
        Statement query = null; //query
        try {
            query = conn.createStatement();
            query.executeUpdate("DELETE FROM schema");
            query.executeUpdate("DELETE FROM type_composition");
            query.executeUpdate("DELETE FROM attribute_values");
            query.executeUpdate("DELETE FROM attribute_types");
            query.executeUpdate("DELETE FROM users");
            query.executeUpdate("DELETE FROM objects_composition");
            query.executeUpdate("DELETE FROM elements");
            query.executeUpdate("DELETE FROM text_occurence");
            query.executeUpdate("DELETE FROM texts");
            query.executeUpdate("DELETE FROM types");
            query.executeUpdate("DELETE FROM textual_objects");
            query.executeUpdate("DELETE FROM methods");
            query.executeUpdate("ALTER SEQUENCE id_att_type_seq RESTART");
            query.executeUpdate("ALTER SEQUENCE id_method_seq RESTART");
            query.executeUpdate("ALTER SEQUENCE id_att_val_seq RESTART");
            query.executeUpdate("ALTER SEQUENCE id_att_val_seq RESTART");
            query.executeUpdate("ALTER SEQUENCE id_att_val_seq RESTART");
            query.executeUpdate("ALTER SEQUENCE id_tex_ob_seq RESTART");
            query.executeUpdate("ALTER SEQUENCE id_text_occ_seq RESTART");
            query.executeUpdate("ALTER SEQUENCE id_text_seq RESTART");
            query.executeUpdate("ALTER SEQUENCE id_user_seq RESTART");
        } catch (SQLException e) {
            throw new ParseException("Invalid DB format", -1);
        } finally {
            //closes resources
            close(query);
            close(conn);
        }
    }

    /**
     * <p>Closes any open
     * <tt>ResultSet</tt>.</p>
     *
     * @param resultSets the ResultSets to close
     * @throws SQLException if a database error occurs
     */
    public static void close(ResultSet... resultSets) throws SQLException {
        if (resultSets == null) {
            return;
        }
        for (ResultSet resultSet : resultSets) {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    throw new SQLException("Fatal error. Try restart the program.");
                }
            }
        }
    }

    /**
     * <p>Closes any open
     * <tt>Statement</tt>.</p>
     *
     * @param statements the Statement to close
     * @throws SQLException if a database error occurs
     */
    public static void close(Statement... statements) throws SQLException {
        if (statements == null) {
            return;
        }
        for (Statement statement : statements) {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    throw new SQLException("Fatal error. Try restart the program.");
                }
            }
        }
    }

    /**
     * <p>Closes any open
     * <tt>Connection</tt>.</p>
     *
     * @param connections the Connection to close
     * @throws SQLException if a database error occurs
     */
    public static void close(Connection... connections) throws SQLException {
        if (connections == null) {
            return;
        }
        for (Connection connection : connections) {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    throw new SQLException("Fatal error. Try restart the program.");
                }
            }
        }
    }
}