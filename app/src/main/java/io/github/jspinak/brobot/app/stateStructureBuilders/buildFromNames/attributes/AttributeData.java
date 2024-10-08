package io.github.jspinak.brobot.app.stateStructureBuilders.buildFromNames.attributes;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * AttributeData keeps track of the following info:
 * - the Attribute is active on which pages
 * - the results of searching for the Image on each page with respect to this Attribute
 * Here, the term 'page' refers to screenshots that are used to create the initial
 *   State structure.
 * For example, as specified by its filename (..._34...), an Image should appear (Attribute APPEARS)
 *   on page #34. This info will be in the pagesActive field. If the Image is found on page 34,
 *   the entry in pageResults will be (34, true); otherwise, it will be (34, false).
 */
@Getter
@Setter
public class AttributeData {

    private AttributeTypes.Attribute attribute;
    private Set<Integer> pagesActive = new HashSet<>();
    /*
    Some attributes such as MultipleMatches require setting a false flag on pages
    where there is no active MultipleMatches attribute. This is why we need a separate Map
    for the results.
     */
    private Map<Integer, Boolean> pageResults = new HashMap<>();

    public AttributeData(AttributeTypes.Attribute attribute) {
        this.attribute = attribute;
    }

    public void addPage(int page) {
        pagesActive.add(page);
    }

    public boolean isEmpty() {
        return pagesActive.isEmpty();
    }

    public void setPageResult(int page, boolean result) {
        pageResults.put(page, result);
    }

    public boolean hasPage(int page) { return pagesActive.contains(page) || pagesActive.contains(-1); }

    public void merge(AttributeData aData) {
        pagesActive.addAll(aData.pagesActive);
    }
}
