package io.github.jspinak.brobot.app.stateStructureBuilders.buildFromNames.attributes;

import io.github.jspinak.brobot.app.stateStructureBuilders.ExtendedStateImageDTO;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * For determining active Attributes for an Image on a specific page.
 */
@Component
public class GetAttribute {

    /**
     * Retrieves all Attributes active on the given page.
     * @param image The Image of interest
     * @param page The page, or screenshot, to query
     * @return all Attributes active on this page for this Image
     */
    public Set<AttributeTypes.Attribute> getAttributes(ExtendedStateImageDTO image, int page) {
        Set<AttributeTypes.Attribute> attributes = new HashSet<>();
        Map<AttributeTypes.Attribute, AttributeData> screenshots = image.getAttributes().getScreenshots();
        screenshots.keySet().forEach(att -> {
            Set<Integer> pages = screenshots.get(att).getPagesActive();
            if (pages.contains(page) || pages.contains(-1))
                attributes.add(att);
        });
        return attributes;
    }

    /**
     * Determines if an Attribute is active on a specific page for the Image of interest.
     * @param image The Image of interest
     * @param page The page to query
     * @param attribute The Attribute of interest
     * @return true if the Attribute exists for this Image on the given page
     */
    public boolean isPresent(ExtendedStateImageDTO image, int page, AttributeTypes.Attribute attribute) {
        return getAttributes(image, page).contains(attribute);
    }
}
