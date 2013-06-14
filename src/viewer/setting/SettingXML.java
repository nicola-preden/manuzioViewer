package viewer.setting;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * <p>Questa classe rende trasparente qualsiasi operazione sul file di
 * configurazione che contiene l'elenco dei server ed altro </p>
 *
 * @author Nicola Preden, matricola 818578, Facoltà di informatica Ca' Foscari
 * in Venice
 */
public class SettingXML {

    /**
     * <p>markup xml lista connessioni recenti. </p>
     */
    public static final String CONNECTION_LIST = "ConnectionsList";
    /**
     * <p>markup xml lingua selezionata. </p>
     */
    public static final String LANGUAGE_SELECT = "Language";
    public static final String SCHEMA_OBJ = "SchemaList";
    private ArrayList<NodeSetting> setting;
    private String url;

    /**
     * <p>Crea un nuovo oggetti contenente tutte le configurazione dell'app in
     * base</p>
     *
     * @param url indirizzo del file xml di configurazione
     */
    public SettingXML(String url) {
        this.url = url;
        this.setting = new ArrayList<NodeSetting>();
        readSettingXml();
    }

    /**
     * <p>Aggiunge un nuovo valore di configurazione in coda agli altri già
     * presenti</p>
     *
     * @param desc descrittore
     * @param prop parametri di configurazione
     */
    public synchronized boolean addSetting(String desc, Properties... prop) {
        int find;
        if (desc == null || desc.isEmpty() || prop == null) {
            return false;
        }

        find = Collections.binarySearch(setting, new NodeSetting(desc), new NodeSettingComparator());
        int i = 0;
        while (i < setting.size()) {
            NodeSetting get = setting.get(i);
            if (get.getDesc().compareTo(desc) != 0) {
                i++;
            } else {
                setting.remove(i);
            }

        }
        if (find != -1) {
            setting.get(find).addProp(prop);
        } else {
            setting.add(new NodeSetting(desc, prop));
            Collections.sort(setting, new NodeSettingComparator());
        }
        return true;
    }

    /**
     * <p>Rimuove un singola configurazione o tutto un blocco</p>
     *
     * @param desc descrittore
     * @param prop se <tt>NULL</tt> rimuove tutti il bloccchi con sescrittore
     * desc
     */
    public synchronized boolean removeSetting(String desc, Properties... prop) {
        Boolean test = false;
        if (desc == null || desc.isEmpty()) {
            return false;
        }

        if (prop == null) {
            int i = 0;
            while (i < setting.size()) {
                NodeSetting get = setting.get(i);
                if (get.getDesc().compareTo(desc) != 0) {
                    i++;
                } else {
                    setting.remove(i);
                    test = true;
                }

            }
        } else {
            int i = 0;
            while (i < setting.size()) {
                NodeSetting get = setting.get(i);
                if (get.getDesc().compareTo(desc) != 0) {
                    i++;
                } else {
                    get.removeProp(prop);
                    test = true;
                    if (get.size() < 1) {
                        setting.remove(i);
                    }
                }
            }
        }
        return test;
    }

    /**
     * <p>Ritona un <tt>viewer.setting.NodeSettingInterface</tt>
     * se trova l'oggetto altrimenti <tt>NULL</tt>. </p>
     *
     * @param desc
     * @return
     */
    public synchronized NodeSettingInterface[] getSetting(String desc) {
        Iterator<NodeSetting> iterator = setting.iterator();
        ArrayList<NodeSettingInterface> al = new ArrayList<NodeSettingInterface>();
        while (iterator.hasNext()) {
            NodeSetting next = iterator.next();
            if (next.getDesc().compareTo(desc) == 0) {
                al.add(next);
            }
        }
        return al.toArray(new NodeSettingInterface[al.size()]);
    }

    /**
     * Salva il contenuto della struttura su file
     */
    public synchronized void saveOnFile() {
        this.writeSettingXml();
    }

    private void readSettingXml() {
        try {
            File file = new File(this.url);

            if (!file.isFile()) {
                return;
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName(SettingXML.CONNECTION_LIST);

            for (int i = 0; i < nList.getLength(); i++) {
                Node n = nList.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) n;
                    Properties p = new Properties();

                    p.setProperty("url", e.getElementsByTagName("url").item(0).getTextContent());
                    p.setProperty("user", e.getElementsByTagName("user").item(0).getTextContent());
                    p.setProperty("password", e.getElementsByTagName("password").item(0).getTextContent());
                    this.addSetting("ConnectionsList", p);
                }

            }

        } catch (SAXException | IOException | ParserConfigurationException ex) {
            Logger.getLogger(SettingXML.class.getName()).log(Level.SEVERE, null, ex);
        }


    }

    private void writeSettingXml() {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();


            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("Setting");
            //rootElement.setAttribute("test", "1");
            doc.appendChild(rootElement);


            NodeSetting ns_c = (NodeSetting) this.getSetting(SettingXML.CONNECTION_LIST);
            if (ns_c != null) {
                ListIterator<Properties> readProp = ns_c.readProp();
                while (readProp.hasNext()) {
                    Properties next;
                    Element ConnectionsList;
                    Element db_url;
                    Element user;
                    Element password;

                    next = readProp.next();
                    ConnectionsList = doc.createElement(SettingXML.CONNECTION_LIST);
                    rootElement.appendChild(ConnectionsList);

                    db_url = doc.createElement("url");
                    db_url.appendChild(doc.createTextNode(next.getProperty("url")));
                    ConnectionsList.appendChild(db_url);

                    user = doc.createElement("user");
                    user.appendChild(doc.createTextNode(next.getProperty("user")));
                    ConnectionsList.appendChild(user);

                    password = doc.createElement("password");
                    password.appendChild(doc.createTextNode(next.getProperty("password")));
                    ConnectionsList.appendChild(password);
                }
            }
            // riscrivere per salvataggio lingua, e interpretazione schemi

            // SCRITTURA FILE
            OutputFormat format = new OutputFormat(doc);
            format.setIndenting(true);
            XMLSerializer serializer = new XMLSerializer(new FileOutputStream(new File(this.url)), format);
            serializer.serialize(doc);

        } catch (ParserConfigurationException | IOException ex) {
            Logger.getLogger(SettingXML.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
