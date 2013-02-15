/**
 * @author Nicola Preden, matricola 818578, Facoltà di informatica Ca' Foscari
 * in Venice
 */
package viewer;

import java.io.File;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Document;


/**
 * <p> Classe contenete il main e tutti i metodi per operare su file xml </p>
 * @author Nicola Preden, matricola 818578, Facoltà di informatica Ca' Foscari
 * in Venice
 */
public class Main {
    
     static Connection conn = null;
     static MainWindow mw =null;
     static Document xmlDoc = null;

     void readSettingXml() { 
         DocumetBuilder builder;
      Document document = builder.build(new File("foo.xml")); 
      
     }
    
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