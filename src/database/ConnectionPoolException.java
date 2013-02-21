/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import java.sql.SQLException;

/**
 * Classe che gestisce tutte le eccezioni sollevate da ConnectionPool
 * @author Nicola Preden, matricola 818578, Facoltà di informatica Ca' Foscari
 * in Venice
 */
public class ConnectionPoolException extends SQLException {
    public ConnectionPoolException() {}
    public ConnectionPoolException(String s) {
        super(s);
    }
}
