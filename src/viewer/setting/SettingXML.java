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
import java.util.Set;
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
 * @author Nicola Preden, matricola 818578, Facolt�� di informatica Ca' Foscari
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
    public static final String SCHEMA_LIST = "SchemaList";
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
     * <p>Aggiunge un nuovo valore di configurazione in coda agli altri gi��
     * presenti</p>
     *
     * @param desc descrittore
     * @param prop parametri di configurazione
     */
    public synchronized boolean addSetting(String desc, Properties prop) {
        int find;
        if (desc == null || desc.isEmpty() || prop == null) {
            return false;
        }

        find = Collections.binarySearch(setting, new NodeSetting(desc), new NodeSettingComparator());
        if (find >= 0) {
            setting.get(find).addProp(prop);
        } else {
            setting.add(new NodeSetting(desc, prop));
            Collections.sort(setting, new NodeSettingComparator());
        }
        return true;
    }

    /**
     * <p>Aggiunge un nuovo valore di configurazione in testa agli altri gi��
     * presenti</p>
     *
     * @param desc descrittore
     * @param prop parametri di configurazione
     */
    public synchronized boolean addSettingAtTop(String desc, Properties prop) {
        int find;
        if (desc == null || desc.isEmpty() || prop == null) {
            return false;
        }

        find = Collections.binarySearch(setting, new NodeSetting(desc), new NodeSettingComparator());
        if (find >= 0) {
            setting.get(find).addAtFistOccProp(prop);
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
     * @param prop se <tt>NULL</tt> rimuove tutto il blocco
     */
    public synchronized boolean removeSetting(String desc, Properties prop) {
        if (desc == null || desc.isEmpty()) {
            return false;
        }
        int find = Collections.binarySearch(setting, new NodeSetting(desc), new NodeSettingComparator());
        if (prop == null) {
            setting.remove(find);
        } else {
            NodeSetting get = setting.get(find);
            get.removeProp(prop);
            if (get.isEmpty()) {
                setting.remove(find);
            }
        }
        return false;
    }

    /**
     * <p>Ritona un <tt>viewer.setting.NodeSettingInterface</tt>
     * se trova l'oggetto altrimenti <tt>NULL</tt>. </p>
     *
     * @param desc
     * @return
     */
    public synchronized NodeSettingInterface getSetting(String desc) {
        int find = Collections.binarySearch(setting, new NodeSetting(desc), new NodeSettingComparator());
        if (find >= 0) {
            return setting.get(find);
        } else {
            return null;
        }

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
            NodeList nList;

            nList = doc.getElementsByTagName(SettingXML.CONNECTION_LIST);

            for (int i = 0; i < nList.getLength(); i++) {
                Node n = nList.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) n;
                    Properties p = new Properties();

                    p.setProperty("url", e.getAttribute("url"));
                    p.setProperty("user", e.getAttribute("user"));
                    p.setProperty("password", e.getAttribute("password"));
                    this.addSetting(SettingXML.CONNECTION_LIST, p);
                }

            }
            nList = doc.getElementsByTagName(SettingXML.SCHEMA_LIST);

            for (int i = 0; i < nList.getLength(); i++) {
                Node n = nList.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) n;
                    NodeList childNodes = e.getChildNodes();
                    Properties p = new Properties();

                    for (int j = 0; j < childNodes.getLength(); j++) {
                        if (childNodes.item(j).getNodeType() == Node.ELEMENT_NODE) {
                            Element item = (Element) childNodes.item(j);
                            p.setProperty(item.getTagName(), item.getTextContent());
                        }
                    }
                    this.addSetting(SettingXML.SCHEMA_LIST, p);
                }

            }

            nList = doc.getElementsByTagName(SettingXML.LANGUAGE_SELECT);

            for (int i = 0; i < nList.getLength(); i++) {
                Node n = nList.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) n;
                    Properties p = new Properties();

                    p.setProperty("lang", e.getAttribute("lang"));
                    this.addSetting(SettingXML.LANGUAGE_SELECT, p);
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

            // Lista connessioni
            NodeSetting ns_c = (NodeSetting) this.getSetting(SettingXML.CONNECTION_LIST);
            if (ns_c != null) {
                ListIterator<Properties> readProp = ns_c.readProp();
                while (readProp.hasNext()) {
                    Properties next;
                    Element ConnectionsList;
                    next = readProp.next();
                    ConnectionsList = doc.createElement(SettingXML.CONNECTION_LIST);
                    ConnectionsList.setAttribute("url", next.getProperty("url"));
                    ConnectionsList.setAttribute("user", next.getProperty("user"));
                    ConnectionsList.setAttribute("password", next.getProperty("password"));
                    rootElement.appendChild(ConnectionsList);
                }
            }
            // Elenco layout
            NodeSetting ns_s = (NodeSetting) this.getSetting(SettingXML.SCHEMA_LIST);
            if (ns_s != null) {
                ListIterator<Properties> readProp = ns_s.readProp();
                while (readProp.hasNext()) {
                    Properties next;
                    Element SchemaList;

                    next = readProp.next();
                    Set<String> spn = next.stringPropertyNames();
                    Iterator<String> iterSnp = spn.iterator();

                    SchemaList = doc.createElement(SettingXML.SCHEMA_LIST);
                    rootElement.appendChild(SchemaList);

                    while (iterSnp.hasNext()) {
                        String s = iterSnp.next();
                        Element type = doc.createElement(s);
                        type.appendChild(doc.createTextNode(next.getProperty(s)));
                        SchemaList.appendChild(type);
                    }
                }
            }
            // riscrivere per salvataggio lingua
            NodeSetting ns_l = (NodeSetting) this.getSetting(SettingXML.LANGUAGE_SELECT);
            if (ns_l != null) {
                ListIterator<Properties> readProp = ns_l.readProp();
                while (readProp.hasNext()) {
                    Properties next;
                    Element ConnectionsList;

                    next = readProp.next();
                    ConnectionsList = doc.createElement(SettingXML.LANGUAGE_SELECT);
                    ConnectionsList.setAttribute("lang", next.getProperty("lang"));
                    rootElement.appendChild(ConnectionsList);
                }
            }

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
