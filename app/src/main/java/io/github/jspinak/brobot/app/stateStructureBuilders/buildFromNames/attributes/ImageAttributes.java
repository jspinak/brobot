package io.github.jspinak.brobot.app.stateStructureBuilders.buildFromNames.attributes;

import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.reports.ANSI;
import io.github.jspinak.brobot.reports.Report;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

import static io.github.jspinak.brobot.app.stateStructureBuilders.buildFromNames.attributes.AttributeTypes.Attribute.*;

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
        addAttribute(LOCATION);
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
        Report.println("State."+stateName+" Pattern."+imageName, ANSI.BLUE);
        filenames.forEach(f -> {
            Pattern i = new Pattern(f);
            Report.print(f+"["+i.w()+","+i.h()+"] ");
        });
        Report.println();
        if (!transitionsTo.isEmpty()) {
            Report.print("Transitions");
            transitionsTo.forEach(tr -> Report.print("."+tr));
            Report.println();
        }
        printPages();
    }

    /**
     * Print each {attribute, pages} for all attributes with pages.
     * Otherwise, print "no attributes".
     * SINGLE_MATCH is an attribute but since it's a default we don't print it here.
     */
    private void printPages() {
        boolean noAttributes = true;
        for (AttributeTypes.Attribute attribute : screenshots.keySet()) {
            //Report.println(attribute.toString());
            if (attribute != SINGLE_MATCH && !screenshots.get(attribute).isEmpty()) {
                noAttributes = false;
                Report.print(attribute.name());
                screenshots.get(attribute).getPagesActive().forEach(page -> Report.print("." + page));
                Report.println();
            }
        }
        if (noAttributes) Report.println(" no attributes");
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

    public boolean isStateLocation() { return !screenshots.get(LOCATION).isEmpty(); }

    public boolean isStateImage() {
        return screenshots.get(TRANSFER).isEmpty() &&
                screenshots.get(REGION).isEmpty() &&
                screenshots.get(LOCATION).isEmpty();
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