/**
 * @author Nicola Preden, matricola 818578, Facoltà di informatica Ca' Foscari
 * in Venice
 */
package viewer;

import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p> Classe contenete il main e tutti i metodi per operare su file xml </p>
 *
 * @author Nicola Preden, matricola 818578, Facoltà di informatica Ca' Foscari
 * in Venice
 */
public class Main {
    private static class Timer extends Thread {
        @Override
        public void run() {
                while (true) {
                    try {
                        Thread.sleep(30000);
                        System.gc();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
    }

    static Connection conn = null;
    static MainWindow mw = null;
    private static String urlXml = "setting.xml";
    private static Timer tm = new Timer();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here

        mw = new MainWindow();
        mw.setVisible(true);
        tm.start();
        

    }
}