/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package viewer.taskThread;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import viewer.AddToServerWizard;
import viewer.ManuzioViewer;
import viewer.SecondStepStrategy;
import viewer.manuzioParser.Schema;

/**
 * <p>Questa classe si occupa di caricare il i dati ed il loro modello nel
 * databse. </p>
 *
 * @author Nicola Preden, matricola 818578, Facoltà di informatica Ca' Foscari
 * in Venice
 */
public class TaskDataBaseUpdate extends SwingWorker<Void, Void> {

    AddToServerWizard windows;
    SecondStepStrategy.TextType maxTypeList;
    JTextArea jTA;
    Schema s;
    int progress = -1;
    int max;

    /**
     * <p>Crea un nuovo oggetto. </p>
     *
     * @param s
     * @param windows
     * @param label
     * @param maxTypeList
     */
    public TaskDataBaseUpdate(Schema s, AddToServerWizard windows, JTextArea label, SecondStepStrategy.TextType maxTypeList) {
        this.maxTypeList = maxTypeList;
        this.jTA = label;
        this.jTA.setText("");
        this.jTA.setEditable(false);
        this.windows = windows;
        this.s = s;

    }

    private void updateProgress(Object x) {
        if (x == null) {
            return;
        }
        if (x instanceof Integer) {
            try {
                int intValue = ((Integer) x).intValue();
                if (intValue == progress) {
                    return;
                } else {
                    progress = intValue;
                }
                Document document = jTA.getDocument();
                Element rootElem = document.getDefaultRootElement();
                int numLines = rootElem.getElementCount();
                Element lineElem = rootElem.getElement(numLines - 2);
                int lineStart = lineElem.getStartOffset();
                int lineEnd = lineElem.getEndOffset();
                document.remove(lineStart, lineEnd - lineStart);
                this.jTA.append("In corso ... " + progress + "%\n");
                setProgress(intValue);
            } catch (BadLocationException ex) {
                Logger.getLogger(TaskDataBaseUpdate.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (x instanceof String) {
            this.jTA.append((String) x + "\n");
            this.jTA.append("In corso ... \n");
            setProgress(0);
        }
    }

    @Override
    protected Void doInBackground() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet resultSet = null;
        CallableStatement function = null;
        Savepoint setSavepoint = null;
        try {
            int start_idx = 0;
            int end_idx = 1;
            conn = ManuzioViewer.getConnection();
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            setSavepoint = conn.setSavepoint();

            conn.commit();
            updateProgress("Inizializzazione Caricamento ... ");
            String[] allText = this.maxTypeList.getAllText();
            // calcolo lo start
            stmt = conn.createStatement();
            stmt.execute("SELECT "
                    + "  max(\"end\")"
                    + "FROM "
                    + "  public.text_occurence");
            resultSet = stmt.getResultSet();

            while (resultSet.next()) {
                start_idx = resultSet.getInt(1);
            }
            if (start_idx != 0) {
                end_idx = start_idx;
            } else {
                start_idx = 1;
            }
            ArrayList<Integer> l = new ArrayList<Integer>();
            String qry = "{? = call create_or_return_sto(?, ?, ?, ?, ?, ?) }";
            function = conn.prepareCall(qry);
            String m = s.getMinimalUnit().getTypeName();
            for (int i = 0; i < allText.length; i++) {
                function.registerOutParameter(1, Types.OTHER);
                function.setString(2, allText[i]);
                function.setInt(3, allText[i].length());
                function.setInt(4, end_idx++);
                function.setInt(5, end_idx);
                function.setString(6, s.getMinimalUnit().getTypeName());
                function.setBoolean(7, false);
                function.execute();
                resultSet = (ResultSet) function.getObject(1);
                while (resultSet.next()) {
                    int aInt = resultSet.getInt(1);
                    l.add(aInt);
                }
                updateProgress((Integer) (100 * i / allText.length));
            }
            function.close();
            updateProgress(100);
            conn.commit();
            updateProgress("Creazione MaximalUnit ...");
            updateProgress(0);

            qry = "{? = call create_or_return_rto(?, obj_type character varying, is_plu boolean, obj_label text) }";

            updateProgress(100);
            function.close();


            conn.commit();
        } catch (SQLException ex) {
            if (setSavepoint != null) {
                try {
                    conn.rollback(setSavepoint);
                } catch (SQLException ex1) {
                    Logger.getLogger(TaskDataBaseUpdate.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
            Logger.getLogger(TaskDataBaseUpdate.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (function != null) {
                    function.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(TaskDataBaseUpdate.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    @Override
    public void done() {
        this.windows.setVisible(false);
    }

    private int load(SecondStepStrategy.TextType tx, int start, int end) {
        if (tx.getType().isMinimalUnit()) {}
        return 0;
    }
}
