package io.github.jspinak.brobot.libraryfeatures.captureAndReplay.replay;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import io.github.jspinak.brobot.libraryfeatures.captureAndReplay.capture.SceneAndObjectsForXML;
import io.github.jspinak.brobot.libraryfeatures.captureAndReplay.capture.SceneObjectCollectionForXML;

// Removed old logging import: import io.github.jspinak.brobot.tools.logging.ConsoleReporter;

@Component
public class ReadXmlScenes {

    public SceneObjectCollectionForXML getSceneAndObjects() {
        DocumentBuilderFactory dbf =
                DocumentBuilderFactory.newInstance(); // Instantiate the Factory
        String path = "capture\\scenes.xml";
        System.out.println(Path.of(path).toAbsolutePath().toString());
        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(path)); // parse XML file
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getElementsByTagName("scenes");
            if (nodeList.item(0).hasChildNodes()) {
                return getScenes(nodeList.item(0).getChildNodes());
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
        return new SceneObjectCollectionForXML();
    }

    private SceneObjectCollectionForXML getScenes(NodeList childNodes) {
        SceneObjectCollectionForXML sceneObjects = new SceneObjectCollectionForXML();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeName().equals("scene")) {
                sceneObjects.addScene(new SceneAndObjectsForXML(childNodes.item(i)));
            }
        }
        return sceneObjects;
    }
}
