package io.github.jspinak.brobot.buildStateStructure.buildFromNames.attributes;

import io.github.jspinak.brobot.reports.ANSI;
import io.github.jspinak.brobot.reports.Report;
import lombok.Getter;
import lombok.Setter;
import org.sikuli.script.Match;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private List<String> transitionsTo = new ArrayList<>();
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

    public void print() {
        Report.println("State."+stateName+" Image."+imageName, ANSI.BLUE);
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
        for (AttributeTypes.Attribute attribute : AttributeTypes.Attribute.values()) {
            if (screenshots.get(attribute).getPageResults().containsKey(page)) attributes.add(attribute);
        }
        return attributes;
    }

    public boolean isStateRegion() {
        return !screenshots.get(REGION).isEmpty();
    }

    public boolean isStateImage() {
        return screenshots.get(TRANSFER).isEmpty() && screenshots.get(REGION).isEmpty();
    }
}
