/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package viewer.taskThread;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
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
    int progress;
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
        progress = 0;
        max = maxTypeList.getAllText().length;

    }

    private void updateProgress(Object x) {
        if (x == null) {
            progress++;
            try {
                int intValue = this.getProgress();
                int k = 100 * progress / max;
                if (intValue == k) return;
                Document document = jTA.getDocument();
                Element rootElem = document.getDefaultRootElement();
                int numLines = rootElem.getElementCount();
                Element lineElem = rootElem.getElement(numLines - 2);
                int lineStart = lineElem.getStartOffset();
                int lineEnd = lineElem.getEndOffset();
                document.remove(lineStart, lineEnd - lineStart);
                this.jTA.append("In corso ... " + k + "%\n");
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
            setSavepoint = conn.setSavepoint();

            conn.commit();
            updateProgress("Inizializzazione Caricamento ... ");
            int allTextLeng = this.maxTypeList.getAllText().length;
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
                end_idx = start_idx + allTextLeng;
            } else {
                start_idx = 1;
                end_idx = start_idx + allTextLeng;
            }
            conn.commit();
            resultSet.close();
            conn.close();
            updateProgress("Caricamento dati ... ");
            int id = load(this.maxTypeList,start_idx,end_idx);
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
        Connection conn = null;
        PreparedStatement prepareStatement = null;
        ResultSet res = null;
        int x = -1;
        if (tx == null) {
            return -1;
        }
        if (tx.getType().isMinimalUnit()) {
            //<editor-fold defaultstate="collapsed" desc="minimo">
            try {
                String qry = "SELECT * FROM create_or_return_sto(?, ?, ?, ?, ?, ?)";
                conn = ManuzioViewer.getConnection();
                prepareStatement = conn.prepareStatement(qry);
                prepareStatement.setString(1, tx.getAllText()[0]);
                prepareStatement.setInt(2, tx.getAllText()[0].length());
                prepareStatement.setInt(3, start);
                prepareStatement.setInt(4, end);
                prepareStatement.setString(5, tx.getType().getTypeName());
                prepareStatement.setBoolean(6, false);
                res = prepareStatement.executeQuery();
                while (res.next()) {
                    x = res.getInt(1);
                }
                updateProgress(null);

            } catch (SQLException ex) {
                Logger.getLogger(TaskDataBaseUpdate.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    if (res != null) {
                        res.close();
                    }
                    if (prepareStatement != null) {
                        prepareStatement.close();
                    }
                    if (conn != null) {
                        conn.close();
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(TaskDataBaseUpdate.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            //</editor-fold>
        } else {
            try {
                SecondStepStrategy.TextType[] subType = tx.getSubType();
                Object[] arr = new Integer[subType.length];
                int startT = start;
                int stopT = start;
                for (int i = 0; i < subType.length; i++) {
                    stopT += subType[i].getAllText().length;
                    int t = load(subType[i], startT, stopT);
                    arr[i] = t;
                    startT = stopT;
                }
                String qry = "SELECT * FROM create_or_return_rto(?, ?, ?, ?)";
                // query caricamento
                conn = ManuzioViewer.getConnection();
                Array arrSql = conn.createArrayOf("integer", arr);
                prepareStatement = conn.prepareStatement(qry);
                prepareStatement.setArray(1, arrSql);
                prepareStatement.setString(2, tx.getType().getTypeName());
                prepareStatement.setBoolean(3, true);
                prepareStatement.setString(4, "");
                res = prepareStatement.executeQuery();
                while (res.next()) {
                    x = res.getInt(1);
                }
            } catch (SQLException ex) {
                Logger.getLogger(TaskDataBaseUpdate.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    if (res != null) {
                        res.close();
                    }
                    if (prepareStatement != null) {
                        prepareStatement.close();
                    }
                    if (conn != null) {
                        conn.close();
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(TaskDataBaseUpdate.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return x;
    }
}
