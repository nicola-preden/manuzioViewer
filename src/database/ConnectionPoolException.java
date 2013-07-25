package database;

import java.sql.SQLException;
import java.util.ResourceBundle;
import viewer.ManuzioViewer;

/**
 * <p>Classe che gestisce tutte le eccezioni sollevate da ConnectionPoolFactory. </p>
 * @author Nicola Preden, matricola 818578, Facoltà di informatica Ca' Foscari
 * in Venice
 */
public class ConnectionPoolException extends SQLException {
    private static final ResourceBundle lang = ResourceBundle.getBundle("viewer/language/lang", ManuzioViewer.LANGUAGE);

    /**
     * Creates a new instance of
     * <tt>ConnectionPoolFactoryA</tt> without detail message.
     */
    public ConnectionPoolException() {
    }

    /**
     * Constructs an instance of
     * <tt>ConnectionPoolFactoryA</tt> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public ConnectionPoolException(String msg) {
        super(msg);
    }
}
