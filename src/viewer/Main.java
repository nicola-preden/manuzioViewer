/**
 * @author Nicola Preden, matricola 818578, Facoltà di informatica Ca' Foscari
 * in Venice
 */
package viewer;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * <p> Classe contenete il main e tutti i metodi per operare su file xml </p>
 *
 * @author Nicola Preden, matricola 818578, Facoltà di informatica Ca' Foscari
 * in Venice
 */
public class Main {

    static Connection conn = null;
    static MainWindow mw = null;
    private static String urlXml = "setting.xml";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here

        mw = new MainWindow();
        mw.setVisible(true);

        new Runnable() {    // forzatura esecuzione garbage collector
            @Override
            public void run() {
                while (true) {
                    try {
                        System.gc();
                        Thread.sleep(30000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        };

    }
}