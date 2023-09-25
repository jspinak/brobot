package io.github.jspinak.brobot.actions.methods.basicactions.captureAndReplay.capture;

import io.github.jspinak.brobot.reports.Report;
import org.sikuli.script.Mouse;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class WriteXmlDomActions {

    private Document doc;
    private Element rootElement;
    private LocalDateTime startTime;

    public void initDocument() { // throws ParserConfigurationException {

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
        try {
            docBuilder = docFactory.newDocumentBuilder();
            startTime = LocalDateTime.now();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }

        // root elements
        doc = docBuilder.newDocument();
        rootElement = doc.createElement("recording");
        doc.appendChild(rootElement);
    }

    public void addElement(String name, String action, String id, String key, String value) {
        if (doc == null || rootElement == null) return;

        Element child = doc.createElement(name);
        rootElement.appendChild(child);

        // add xml attributes
        child.setAttribute("action", action);
        child.setAttribute("x", Mouse.at().x + "");
        child.setAttribute("y", Mouse.at().y + "");
        child.setAttribute("key", key);
        child.setAttribute("millis", String.valueOf(Duration.between(startTime, LocalDateTime.now()).toMillis()));
        child.setAttribute(id, value);
    }

    public void writeXmlToFile() {
        try {
            writeXmlToFile("capture\\input-history.xml");
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
