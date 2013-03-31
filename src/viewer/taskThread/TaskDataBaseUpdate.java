/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package viewer.taskThread;

import javax.swing.SwingWorker;
import viewer.SecondStepStrategy;

/**
 * <p>Questa classe si occupa di caricare il i dati ed il loro modello nel 
 * databse. </p>
 * @author Nicola Preden, matricola 818578, Facoltà di informatica Ca' Foscari
 * in Venice
 */
public class TaskDataBaseUpdate extends SwingWorker<Void, Void> {

    SecondStepStrategy.TextType maxTypeList;
    int progress = 0;
    int max;
            
    public TaskDataBaseUpdate(SecondStepStrategy.TextType maxTypeList) {
        this.maxTypeList =  maxTypeList;
    }

    @Override
    protected Void doInBackground() {
        this.setProgress(0);
        
        return null;
    }
    
}
