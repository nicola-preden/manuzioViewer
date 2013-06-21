/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package viewer.taskThread;

import viewer.setting.AbstractTextualLayout;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import viewer.ManuzioViewer;
import viewer.manuzioParser.Schema;
import viewer.manuzioParser.Type;

/**
 * <p>Classe astratta che descrive i metodi per aggiungere generare del testo a
 * seconda dei stili pre caricati</p>
 *
 * @author Nicola Preden, matricola 818578, Facolt�� di informatica Ca' Foscari
 * in Venice
 */
public class TextualLayout<T extends JEditorPane> extends AbstractTextualLayout {

    private int id_object;
    private T output;
    private Schema s;
    private Properties prop;
    private volatile int progress;
    private volatile int max;
    private Boolean consistency;

    /**
     * <p>Costruttore</p>
     *
     * @param id_object
     * @param output
     * @param s
     * @param prop
     */
    private TextualLayout(int id_object, T output, Schema s, Properties prop) {
        if (id_object <= 0) {
            throw new IllegalArgumentException("MIssing Data : schema-url" + prop.toString());
        }
        this.id_object = id_object;
        this.output = output;
        this.s = s;
        this.prop = prop;
        this.progress = 0;
        this.max = 0;
        if (AbstractTextualLayout.typeConsistencyCheck(this.prop, this.s)) {
            consistency = true;
        } else {
            consistency = false;
        }
    }

    public static <T extends JEditorPane> TextualLayout<T> createTextualLayout(int id_object, T output, Schema s, Properties prop) {
        return new TextualLayout<T>(id_object, output, s, prop);
    }

    synchronized private void setMax(int max) {
        this.max = max;
    }

    synchronized private void addMax(int add) {
        this.max += add;
    }

    @Override
    synchronized protected void updateProgress(Object x, Boolean b) {
        try {
            if (x == null) {
                progress++;
                int intValue = this.getProgress();
                int k = 100 * progress / max;
                if (intValue != k) {
                    setProgress(k);
                }
                Document document = output.getDocument();
                Element rootElem = document.getDefaultRootElement();
                int numLines = rootElem.getElementCount();
                Element lineElem = rootElem.getElement(numLines - 2);
                int lineStart = lineElem.getStartOffset();
                int lineEnd = lineElem.getEndOffset();
                document.remove(lineStart, lineEnd - lineStart);
                document.insertString(document.getLength(), "In corso ... " + progress + " / " + max + "\n", null);
            }
            if (x instanceof String) {
                progress = 0;
                Document document = output.getDocument();
                document.insertString(document.getLength(), (String) x + "\n", null);
                if (!b) {
                    document.insertString(document.getLength(), "In corso ... \n", null);
                }
                setProgress(0);
            }
        } catch (BadLocationException ex) {
            Logger.getLogger(TextualLayout.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected Void doInBackground() {
        setMax(1);
        Connection conn;
        PreparedStatement stmt;
        ResultSet resultSet;
        CallableStatement function;
        try {
            output.setText(null);
            updateProgress("Controllo coerenza layout ...", false);
            if (consistency) {
                updateProgress(null, false);
            } else {
                Type[] typeSet = s.getTypeSet();
                setMax(typeSet.length);
                this.prop = new Properties();
                for (int i = 0; i < typeSet.length; i++) {
                    this.prop.setProperty(typeSet[i].getTypeName(), AbstractTextualLayout.DEFAULT + "");
                    updateProgress(null, false);
                }
            }
            updateProgress("Interrogazione Database ...", false);
            conn = ManuzioViewer.getConnection();
            stmt = conn.prepareStatement("SELECT "
                    + "textual_objects.id_tex_obj, "
                    + "textual_objects.type_name "
                    + "FROM "
                    + "  public.textual_objects "
                    + "WHERE "
                    + "  textual_objects.id_tex_obj = ?;");
            stmt.setInt(1, this.id_object);
            resultSet = stmt.executeQuery();
            String q;
            if (!resultSet.next()) {
                updateProgress("Errore Caricamento dati inesistenti", true);
                return null;
            } else {
                q = resultSet.getString(2);
            }
            ManuzioViewer.close(resultSet);
            ManuzioViewer.close(stmt);
            ManuzioViewer.close(conn);
            // Caricamento ricorsivo dei dati
            setMax(1); // c'e almeno un oggetto
            String translateText = translateText(this.id_object);

            // Caricamento degli attributi
            updateProgress("Caricamento Attributi ...", true);
            String k = "-------------\n";
            conn = ManuzioViewer.getConnection();
            k += "ID : " + this.id_object + " TYPE: " + q + "\n\n";
            q = "SELECT "
                    + "  attribute_values.id_att_value, "
                    + "  attribute_types.label, "
                    + "  attribute_values.content AS value "
                    + "FROM "
                    + "  public.attribute_types, "
                    + "  public.attribute_values "
                    + "WHERE "
                    + "  attribute_types.id_att_type = attribute_values.id_att_type AND"
                    + "  attribute_values.id_tex_obj = ? "
                    + "ORDER BY "
                    + "  attribute_types.label ASC;";
            stmt = conn.prepareStatement(q);
            stmt.setInt(1, this.id_object);
            resultSet = stmt.executeQuery();
            String z = "";
            while (resultSet.next()) {
                z += "ID_ATTRIBUTE :  " + resultSet.getInt("id_att_value") + "\tLABEL : " + resultSet.getString("label") + " VALUE : " + resultSet.getString("value") + "\n";
            }
            k += z.length() == 0 ? "" : "Attributi Textual Object: \n" + z;
            k += (consistency) ? "-------------\n" : "Layout non configurato, scegliere Preferenze --> Layout \n" + "-------------\n";
            ManuzioViewer.close(resultSet);
            ManuzioViewer.close(stmt);
            ManuzioViewer.close(conn);
            k += "\n\n" + translateText;
            updateProgress("Visualizzazione", true);

            // Visualizzazione dati
            Document document = output.getDocument();
            document.insertString(document.getLength(), "\n\n" + k, null);

        } catch (SQLException | BadLocationException ex) {
            Logger.getLogger(TextualLayout.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    protected String translateText(int obj) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        String q = "";
        String k = "";
        ArrayList<Integer> ar = new ArrayList<Integer>();
        // Controllo eventuali sottotipi
        try {
            conn = ManuzioViewer.getConnection();
            q += "SELECT "
                    + "  objects_composition.id_contained "
                    + "FROM \n"
                    + "  public.objects_composition "
                    + "WHERE "
                    + "  objects_composition.id_container = ? "
                    + "ORDER BY "
                    + "  objects_composition.pos ASC;";
            stmt = conn.prepareStatement(q);
            stmt.setInt(1, obj);
            resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                ar.add(resultSet.getInt(1));
            }
        } catch (SQLException ex) {
            Logger.getLogger(TextualLayout.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (resultSet != null) {
                try {
                    ManuzioViewer.close(resultSet);
                } catch (SQLException ex) {
                    Logger.getLogger(TextualLayout.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (stmt != null) {
                try {
                    ManuzioViewer.close(stmt);
                } catch (SQLException ex) {
                    Logger.getLogger(TextualLayout.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (conn != null) {
                try {
                    ManuzioViewer.close(conn);
                } catch (SQLException ex) {
                    Logger.getLogger(TextualLayout.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        conn = null;
        stmt = null;
        resultSet = null;
        // Chiamate ricorsive su cutti i sotto-tipo
        addMax(ar.size());
        if (!ar.isEmpty()) {
            for (int i = 0; i < ar.size(); i++) {
                k += translateText(ar.get(i));
            }
        }
        // Aggiunta proprio testo
        try {
            q = "SELECT "
                    + "  texts.cached_text "
                    + "FROM "
                    + "  public.elements, "
                    + "  public.text_occurence, "
                    + "  public.texts "
                    + "WHERE  "
                    + "  elements.id_text_occurence = text_occurence.id_text_occurence AND "
                    + "  text_occurence.id_text = texts.id_text AND "
                    + "  elements.id_tex_obj = ?;";
            conn = ManuzioViewer.getConnection();
            stmt = conn.prepareStatement(q);
            stmt.setInt(1, obj);
            resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                k += resultSet.getString(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(TextualLayout.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (resultSet != null) {
                try {
                    ManuzioViewer.close(resultSet);
                } catch (SQLException ex) {
                    Logger.getLogger(TextualLayout.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (stmt != null) {
                try {
                    ManuzioViewer.close(stmt);
                } catch (SQLException ex) {
                    Logger.getLogger(TextualLayout.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (conn != null) {
                try {
                    ManuzioViewer.close(conn);
                } catch (SQLException ex) {
                    Logger.getLogger(TextualLayout.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        // Stesura Layout
        conn = null;
        stmt = null;
        resultSet = null;
        try {
            conn = ManuzioViewer.getConnection();
            q = "SELECT "
                    + "  textual_objects.type_name "
                    + "FROM "
                    + "  public.textual_objects "
                    + "WHERE "
                    + "  textual_objects.id_tex_obj = ?;";
            stmt = conn.prepareStatement(q);
            stmt.setInt(1, obj);
            resultSet = stmt.executeQuery();
            String type = "";
            while (resultSet.next()) {
                type = resultSet.getString(1);
            }
            try {
                int temp = Integer.parseInt(prop.getProperty(type));
                switch (temp) {
                    case AbstractTextualLayout.SPACE:
                        k += " ";
                        break;
                    case AbstractTextualLayout.TABBED_SPACE:
                        k += "\t";
                        break;
                    case AbstractTextualLayout.RETURN_CARRIGE:
                        k += "\n";
                        break;
                    case AbstractTextualLayout.NO_OPERATION:
                    default:
                        break;
                }
            } catch (NumberFormatException ex) {
                Logger.getLogger(TextualLayout.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (SQLException ex) {
            Logger.getLogger(TextualLayout.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (resultSet != null) {
                try {
                    ManuzioViewer.close(resultSet);
                } catch (SQLException ex) {
                    Logger.getLogger(TextualLayout.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (stmt != null) {
                try {
                    ManuzioViewer.close(stmt);
                } catch (SQLException ex) {
                    Logger.getLogger(TextualLayout.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (conn != null) {
                try {
                    ManuzioViewer.close(conn);
                } catch (SQLException ex) {
                    Logger.getLogger(TextualLayout.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        updateProgress(null, false);
        return k;
    }
}
