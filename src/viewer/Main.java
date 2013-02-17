/**
 * @author Nicola Preden, matricola 818578, Facoltà di informatica Ca' Foscari
 * in Venice
 */
package viewer;

import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;
import viewer.setting.SettingXML;

/**
 * <p> Classe contenete il main</p>
 *
 * @author Nicola Preden, matricola 818578, Facoltà di informatica Ca' Foscari
 * in Venice
 */
 class Main {
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
    static SettingXML setting = null;
    private static final String urlXml = "settings.xml";
    private static Timer tm = new Timer();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        setting = new SettingXML(urlXml);        
        mw = new MainWindow();
        mw.setVisible(true);
        tm.start();
        

    }
}