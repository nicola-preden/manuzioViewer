/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package viewer.setting;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
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

    private ArrayList<NodeSetting> setting;
    private String url;

    public SettingXML(String url) {
        this.url = url;
        this.setting = new ArrayList<NodeSetting>();
        readSettingXml();
    }

    /**
     * <p>Aggiunge un nuovo valore di configurazione</p>
     *
     * @param desc descrittore
     * @param prop parametri di configurazione
     */
    synchronized boolean addSetting(String desc, Properties prop) {
        if (desc == null || desc.isEmpty() || prop == null) {
            return false;
        }
        for (NodeSetting ns : setting) {
            if (ns.getDesc().compareTo(desc) == 0) {
                ns.addProp(prop);
                return true;
            }
        }
        setting.add(new NodeSetting(desc, prop));
        return true;
    }

    /**
     * <p>Rimuove un singola configurazione o tutto un blocco</p>
     *
     * @param desc descrittore
     * @param prop se <code>NULL</code> rimuove tutto il blocco
     */
    synchronized boolean removeSetting(String desc, Properties prop) {
        if (desc == null || desc.isEmpty()) {
            return false;
        }
        if (prop == null) {
            for (int i = 0; i < setting.size(); i++) {
                if (setting.get(i).getDesc().compareTo(desc) == 0) {
                    setting.remove(i);
                    return true;
                }
            }
        } else {
            Iterator<NodeSetting> iterator = setting.iterator();
            while (iterator.hasNext()) {
                NodeSetting next = iterator.next();
                if (next.getDesc().compareTo(desc) == 0) {
                    next.removeProp(prop);
                    if (next.isEmpty()) {
                        iterator.remove();
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private void readSettingXml() {
        try {
            File file = new File(this.url);

            if (!file.isFile()) {
                writeSettingXml();
                return;
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();


            NodeList nList = doc.getElementsByTagName("connection");

            for (int i = 0; i < nList.getLength(); i++) {
                Node n = nList.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) n;
                    Properties p = new Properties();

                    p.setProperty("url", e.getElementsByTagName("url").item(0).getTextContent());
                    p.setProperty("user", e.getElementsByTagName("user").item(0).getTextContent());
                    p.setProperty("password", e.getElementsByTagName("password").item(0).getTextContent());
                    this.addSetting("connection", p);
                }

            }
        } catch (SAXException | IOException | ParserConfigurationException ex) {
            Logger.getLogger(SettingXML.class.getName()).log(Level.SEVERE, null, ex);
        }


    }

    private void writeSettingXml() {
    }
}
