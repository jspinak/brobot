package io.github.jspinak.brobot.actions.methods.basicactions.captureAndReplay.capture;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.reports.Report;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.util.List;

@Component
public class WriteXmlDomScenes {

    private Document doc;
    private Element rootElement;

    public void initDocument() { // throws ParserConfigurationException {

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
        try {
            docBuilder = docFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }

        // root elements
        doc = docBuilder.newDocument();
        rootElement = doc.createElement("scenes");
        doc.appendChild(rootElement);
    }

    public void addScene(SceneAndObjectsForXML sceneObjects) {
        int sceneNumber = Integer.parseInt(sceneObjects.getSceneName());
        int time = sceneNumber * BrobotSettings.captureFrequency * 1000;
        addSceneAndObjects(sceneNumber, time, sceneObjects.getObjectsNames(), sceneObjects.getObjectsLocations());
    }

    public void addSceneAndObjects(int sceneNumber, int timelapseFromStart, List<String> objectNames, List<Location> objectLocations) {
        if (doc == null || rootElement == null) return;

        Element child = doc.createElement("scene");
        rootElement.appendChild(child);

        // add xml attributes
        child.setAttribute("name", String.valueOf(sceneNumber));
        child.setAttribute("millis", String.valueOf(timelapseFromStart));
        int maxObjects = Math.max(objectNames.size(), objectLocations.size());
        for (int i = 0; i < maxObjects; i++) {
            Element object = doc.createElement("object");
            child.appendChild(object);
            object.setAttribute("name", objectNames.get(i));
            object.setAttribute("x", String.valueOf(objectLocations.get(i).getX()));
            object.setAttribute("y", String.valueOf(objectLocations.get(i).getY()));
        }
    }

    public void writeXmlToFile() {
        try {
            writeXmlToFile("capture\\scenes.xml");
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeXmlToFile(String filename) throws TransformerException { // ParserConfigurationException,
        if (doc == null) return;
        // write dom document to a file
        String path = FileSystems.getDefault().getPath(".") + "\\" + filename;
        Report.println("Writing XML to file: " + path);
        try (FileOutputStream output = new FileOutputStream(path)) {
            writeXml(doc, output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // write doc to output stream
    private static void writeXml(Document doc,
                                 OutputStream output)
            throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes"); // makes it look nice
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(output);
        transformer.transform(source, result);
    }
}
