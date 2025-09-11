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

import io.github.jspinak.brobot.tools.logging.ConsoleReporter;

@Component
public class GetXmlActions {

    private ReplayAction replayAction;
    private ReplayCollectionOrganizer replayCollectionOrganizer;

    public GetXmlActions(
            ReplayAction replayAction, ReplayCollectionOrganizer replayCollectionOrganizer) {
        this.replayAction = replayAction;
        this.replayCollectionOrganizer = replayCollectionOrganizer;
    }

    public ReplayCollection getActionsBetweenTimes(double startTime, double endTime) {
        ReplayCollection replayCollection = getAllActions();
        ConsoleReporter.println(
                "ReplayCollection size: " + replayCollection.getReplayObjects().size());
        return replayCollectionOrganizer.getActionsBetweenTimes(
                replayCollection, startTime * 1000, endTime * 1000);
    }

    public ReplayCollection getActionsAfterTime(double startTime) {
        ReplayCollection replayCollection = getAllActions();
        return replayCollectionOrganizer.getActionsAfterTime(replayCollection, startTime);
    }

    public ReplayCollection getAllActions() {
        DocumentBuilderFactory dbf =
                DocumentBuilderFactory.newInstance(); // Instantiate the Factory
        String path = "capture\\input-history.xml";
        ConsoleReporter.println(Path.of(path).toAbsolutePath().toString());
        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(path)); // parse XML file
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getElementsByTagName("recording");
            if (nodeList.item(0).hasChildNodes()) {
                return replayCollectionOrganizer.annotate(nodeList.item(0).getChildNodes());
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
        return new ReplayCollection();
    }
}
