/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import viewer.ManuzioViewer;

/**
 * <p>La classe crea un pool di conessioni a un db PostgreSQL oppure ad un atro
 * qualsiaasi DBMS se viene fornito un oggetto
 * <tt>com.jolbox.bonecp.BoneCPConfig</tt></p>
 *
 * @author Nicola Preden, matricola 818578, Facoltà di informatica Ca' Foscari
 * in Venice
 */
public class ConnectionPoolFactory {

    private BoneCP connectionPool = null;    // l'unica istanza disponibile per factory
    private BoneCPConfig config = null;     // Configurazione
    private static String[] driverName = {"org.postgresql.Driver"};
    private static String[] driverPrefix = {"jdbc:postgresql://"};
    private static final ResourceBundle lang = ResourceBundle.getBundle("viewer/language/lang", ManuzioViewer.LANGUAGE);

    /**
     * Crea una nuova factory a partire da un una configurazione gia pronta
     *
     * @param config cofigurazione per la connessione
     * @throws ConnectionPoolException in caso sia impossibile caricare i driver
     */
    public ConnectionPoolFactory(BoneCPConfig config) throws ConnectionPoolException {
        loadDrivers();
        // Carico configurazione
        this.config = config;
    }

    /**
     * Crea una nuova connessione ad un server PostgreSQL
     *
     * @param url indirizzo DNS o IP del server
     * @param user
     * @param password
     * @throws ConnectionPoolException in caso sia impossibile caricare i driver
     */
    public ConnectionPoolFactory(String url, String user, String password) throws ConnectionPoolException {
        loadDrivers();
        // Carico Configurazione
        loadConfig(url, user, password);
    }

    private static void loadDrivers() throws ConnectionPoolException {
        for (int i = 0; i < driverName.length; i++) {
            try {
                Class.forName(driverName[i]);
            } catch (ClassNotFoundException ex) {
                throw new ConnectionPoolException(lang.getString("DRIVER_NOT_FOUND"));
            }
        }
    }

    private void loadConfig(String url, String user, String password) {
        config = new BoneCPConfig();
        config.setJdbcUrl(driverPrefix[0] + url);
        config.setUsername(user);
        config.setPassword(password);
        config.setMinConnectionsPerPartition(2);
        config.setMaxConnectionsPerPartition(10);
        config.setPartitionCount(2);
        config.setTransactionRecoveryEnabled(false);
        config.setDefaultAutoCommit(true);
        config.sanitize();
    }

    /**
     * <p>Questo metodo aggiunge un nuvo driver all'elenco dei driver
     * disponibili</p> <p>N.B: Il metodo deve essere lanciato prima di esegure
     * uno dei possibli costruttore, in caso contrario il driver non verra
     * caricato.</p>
     *
     * @param driverName nuovo nome della classe *      * e.s <tt>org.postgresql.Driver</tt>
     * @param driverPrefix nuovo prefisso per esegure la *      * connessione <tt>jdbc:postgresql://</tt>
     */
    public static void addDriver(String driverName, String driverPrefix) {
        ConnectionPoolFactory.driverName = Arrays.copyOf(ConnectionPoolFactory.driverName, ConnectionPoolFactory.driverName.length + 1);
        ConnectionPoolFactory.driverPrefix = Arrays.copyOf(ConnectionPoolFactory.driverPrefix, ConnectionPoolFactory.driverPrefix.length + 1);
        ConnectionPoolFactory.driverName[ConnectionPoolFactory.driverName.length - 1] = driverName;
        ConnectionPoolFactory.driverPrefix[ConnectionPoolFactory.driverPrefix.length - 1] = driverPrefix;
    }

    /**
     * Crea un nuovo oggetto
     * <tt>com.jolbox.bonecp.BoneCP</tt> ed in caso ne sia gia generato uno
     * chiude tutte le connessioni esistenti e lo ricrea.
     *
     * @return a pool
     * @throws ConnectionPoolException
     */
    public synchronized BoneCP createConnectionPool() throws ConnectionPoolException {
        Connection conn = null;

        if (connectionPool != null) { // se questa Factory ha gia create un pool
            connectionPool.shutdown();      // Chiudo tutte le vecche connessioni
            try {
                connectionPool = new BoneCP(config);
            } catch (SQLException ex) {
                throw new ConnectionPoolException(ex.getSQLState());
            }

        } else {
            try {
                connectionPool = new BoneCP(config);
            } catch (SQLException ex) {
                throw new ConnectionPoolException(ex.getSQLState());
            }
        }
        try {
            conn = connectionPool.getConnection();
        } catch (SQLException ex) {
            throw new ConnectionPoolException(ex.getSQLState());
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(ConnectionPoolFactory.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        return connectionPool;
    }

    /**
     * <p>Ritorna l'ultimo
     * <tt>com.jolbox.bonecp.BoneCP</tt> creato o se non presente ne crea
     * uno di nuovo
     *
     * @return
     * @throws ConnectionPoolException
     */
    public synchronized BoneCP getConnectionPool() throws ConnectionPoolException {


        if (connectionPool == null) {
            connectionPool = createConnectionPool();
        }
        return connectionPool;

    }

    /**
     * <p>Ritorna l'elenco dei driver supportati. Attenzione!!! Questo metodo
     * accetta solo url postgresql</p>
     *
     * @return An Array of Strings containing the drivers supported
     */
    public String[] getSupportedDrivers() {
        return ConnectionPoolFactory.driverName;
    }

    /**
     * <p>Crea una connesione temporanea non gestite direttamente dal Connection
     * Pool</p>
     *
     * @param url
     * @param user
     * @param password
     * @return
     * @throws SQLException
     */
    public static Connection getConnection(String url, String user, String password) throws SQLException {
        loadDrivers();
        try {
            Connection connection = DriverManager.getConnection(ConnectionPoolFactory.driverPrefix[0] + url, user, password);
            connection.setAutoCommit(true);
            return connection;
        } catch (SQLException ex) {
            throw new SQLException(lang.getString("FATAL ERROR"));
        }

    }
}
