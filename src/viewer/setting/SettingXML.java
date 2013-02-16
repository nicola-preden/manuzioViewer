/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package viewer.setting;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * <p>Questa classe rende trasparente qualsiasi operazione sul file di configurazione
 * che contiene l'elenco dei server ed altro </p>
 * @author Nicola Preden, matricola 818578, Facoltà di informatica Ca' Foscari
 * in Venice
 */
public class SettingXML {
    private ArrayList setting;
    private String url;

    public SettingXML(String url) {
        this.url = url;
    }
    
    private void readSettingXml() {
        try {

            File file = new File(this.url);

            if (!file.isFile()) {
                writeSettingXml();
                return;
            }

            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = dBuilder.parse(file);


            if (doc.hasChildNodes()) {
                // Read op
            }

        } catch (ParserConfigurationException | SAXException | IOException e) {
            System.out.println(e.getMessage());
        }

    }

    private void writeSettingXml() {
    }
    
}
