package io.github.jspinak.brobot.buildStateStructure.buildFromNames.attributes;

import io.github.jspinak.brobot.database.primitives.image.Image;
import io.github.jspinak.brobot.reports.ANSI;
import io.github.jspinak.brobot.reports.Report;
import lombok.Getter;
import lombok.Setter;
import org.sikuli.script.Match;

import java.util.*;

import static io.github.jspinak.brobot.buildStateStructure.buildFromNames.attributes.AttributeTypes.Attribute.*;

/**
Attributes learned from the image filename and initial screenshots of the environment.
These values determine MatchHistories, SearchRegions, and other variables when
building the initial State structure (States and Transitions) from image filenames and screenshots.
 **/
@Getter
@Setter
public class ImageAttributes {

    private String stateName;
    private String imageName;
    private List<String> filenames = new ArrayList<>();
    private Set<String> transitionsTo = new HashSet<>();
    private Map<Integer, List<Match>> matches = new HashMap<>();

    /*
      The image has these attributes applied to these screenshots.
      A value of -1 means that it applies to all screenshots. For example, a -1 in
      DEFINE will define the region in the first screenshot where the image is found.
     */
    private Map<AttributeTypes.Attribute, AttributeData> screenshots = new HashMap<>();
    {
        addAttribute(APPEARS);
        addAttribute(APPEARS_EXCLUSIVELY);
        addAttribute(DOESNT_APPEAR);
        addAttribute(VARIABLE_LOCATION);
        addAttribute(FIXED_LOCATION);
        addAttribute(MULTIPLE_MATCHES);
        addAttribute(SINGLE_MATCH);
        addAttribute(DEFINE);
        addAttribute(GROUP_DEFINE);
        addAttribute(REGION);
        addAttribute(TRANSFER);
    }
    
    private void addAttribute(AttributeTypes.Attribute attribute) {
        screenshots.put(attribute, new AttributeData(attribute));
    }

    public void addPage(AttributeTypes.Attribute attribute, Integer page) {
        screenshots.get(attribute).addPage(page);
    }

    public void addTransition(String stateName) {
        transitionsTo.add(stateName);
    }

    public void addFilename(String filename) {
        filenames.add(filename);
    }

    public void print() {
        Report.println("State."+stateName+" Image."+imageName, ANSI.BLUE);
        filenames.forEach(f -> {
            Image i = new Image(f);
            Report.print(f+"["+i.getWidth(0)+","+i.getHeight(0)+"] ");
        });
        Report.println();
        if (!transitionsTo.isEmpty()) {
            Report.print("Transitions");
            transitionsTo.forEach(tr -> Report.print("."+tr));
            Report.println();
        }
        screenshots.keySet().forEach(this::printPages);
    }

    private void printPages(AttributeTypes.Attribute attribute) {
        if (screenshots.get(attribute).isEmpty()) return;
        Report.print(attribute.name());
        screenshots.get(attribute).getPagesActive().forEach(page -> Report.print("."+page));
        Report.println();
    }

    public List<AttributeTypes.Attribute> getActiveAttributes(int page) {
        List<AttributeTypes.Attribute> attributes = new ArrayList<>();
        for (Map.Entry<AttributeTypes.Attribute, AttributeData> attData : screenshots.entrySet()) {
            if (attData.getValue().hasPage(page)) attributes.add(attData.getKey());
        }
        return attributes;
    }

    public boolean isStateRegion() {
        return !screenshots.get(REGION).isEmpty();
    }

    public boolean isStateImage() {
        return screenshots.get(TRANSFER).isEmpty() && screenshots.get(REGION).isEmpty();
    }

    public void merge(ImageAttributes imageAttributes) {
        transitionsTo.addAll(imageAttributes.transitionsTo);
        filenames.addAll(imageAttributes.filenames);
        imageAttributes.matches.forEach((key, value) -> matches.get(key).addAll(value));
        screenshots.keySet().forEach(
                att -> screenshots.get(att).merge(imageAttributes.screenshots.get(att)));
    }

    public String getWithImagesLineInBuilder() {
        StringBuilder withImages = new StringBuilder();
        withImages.append("\n.withImages(");
        for (int i=0; i<filenames.size(); i++) {
            withImages.append("$S");
            if (i+1 < filenames.size()) withImages.append(", ");
        }
        withImages.append(")");
        return withImages.toString();
    }

}
