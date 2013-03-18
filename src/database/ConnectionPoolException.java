/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import java.sql.SQLException;

/**
 * <p>Classe che gestisce tutte le eccezioni sollevate da ConnectionPoolFactory. </p>
 * @author Nicola Preden, matricola 818578, Facoltà di informatica Ca' Foscari
 * in Venice
 */
public class ConnectionPoolException extends SQLException {

    /**
     * Creates a new instance of
     * <code>ConnectionPoolFactoryA</code> without detail message.
     */
    public ConnectionPoolException() {
    }

    /**
     * Constructs an instance of
     * <code>ConnectionPoolFactoryA</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public ConnectionPoolException(String msg) {
        super(msg);
    }
}
