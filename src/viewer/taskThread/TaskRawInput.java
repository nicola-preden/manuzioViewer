/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package viewer.taskThread;

import java.io.File;
import javax.swing.SwingWorker;

/**
 * <p>Questa classe carica in una stringa tutto il file in imput
 * in una stringa. <p>
 * @author Nicola Preden, matricola 818578, Facoltà di informatica Ca' Foscari
 * in Venice
 */
public class TaskRawInput extends SwingWorker<String, Void>{

    public TaskRawInput(File f) {
        
    }
    @Override
    protected String doInBackground() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    protected void done(){
        
    }
}
