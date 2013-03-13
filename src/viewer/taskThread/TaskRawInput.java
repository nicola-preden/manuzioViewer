/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package viewer.taskThread;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

/**
 * <p>Questa classe carica in un ArrayList tutto il file in imput in una
 * stringa. Se il file non esiste oppure che un qualsiasi errore il valore di
 * ritorno sarà una stringa vuota. <p>
 * <p><b>QUESTA CLASSE USA LE NUOVE API I/O PER JAVA 1.7</b></p>
 *
 * @author Nicola Preden, matricola 818578, Facoltà di informatica Ca' Foscari
 * in Venice
 */
public class TaskRawInput extends SwingWorker<ArrayList<String>, Void> {

    // Le classi usate per le operazioni su disco sfruttano le nuove api per Java 1.7
    private Path p;
    private long size;
    private long current;

    public TaskRawInput(String f) {
        if (f == null) {
            throw new IllegalArgumentException();
        }
        if (f.compareTo("") == 0) {
            throw new IllegalArgumentException();
        }
        p = Paths.get(f);
        if (Files.notExists(p)) { // API 1.7 testa se il percorso non esiste
            throw new IllegalArgumentException();
        }
    }

    @Override
    protected ArrayList<String> doInBackground() {
        ArrayList fun = new ArrayList();
        try {
            String s;

            size = Files.size(p); // dimensione totale del file
            current = 0;
            // uso classi java 1.7
            BufferedReader nbr = Files.newBufferedReader(p, StandardCharsets.UTF_8);

            while ((s = nbr.readLine()) != null && !this.isCancelled()) {
                current += s.getBytes(StandardCharsets.UTF_8).length;
                fun.add(s);
                int perc = (int) (100 * current / size);
                setProgress(perc);
            }
        } catch (IOException ex) {
            Logger.getLogger(TaskRawInput.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            setProgress(100);
            return fun;
        }

    }
}
