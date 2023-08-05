package io.github.jspinak.brobot.buildStateStructure.buildFromNames.attributes;

import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import io.github.jspinak.brobot.reports.Report;
import io.github.jspinak.brobot.stringUtils.CommonRegex;
import org.springframework.stereotype.Component;

import static io.github.jspinak.brobot.buildStateStructure.buildFromNames.attributes.AttributeTypes.Attribute.APPEARS;
import static io.github.jspinak.brobot.buildStateStructure.buildFromNames.attributes.AttributeTypes.Tag.*;

/**
 * Determines the State and Image names, Transition targets, and Attributes from the filename.
 */
@Component
public class SetAttributes {

    private ImageAttributes attributes;
    private String filename;
    private int pos;
    private AttributeTypes.Tag tag;
    private AttributeTypes.Attribute attribute;

    public void processName(StateImageObject image) {
        attributes = image.getAttributes();
        filename = attributes.getFilenames().get(0); //image.getName();
        pos = 0;
        while (pos < filename.length()) {
            processOneValue();
        }
    }

    /**
     * The tag is the category of value: STATE_NAME, IMAGE_NAME, ATTRIBUTE, or TRANSITION
     */
    private void setTag() {
        //Report.println(filename+" "+pos+" "+filename.charAt(pos));
        if (pos == 0) {
            tag = STATE_NAME;
            return;
        }
        if (filename.charAt(pos) == '-') tag = IMAGE_NAME;
        else if (filename.charAt(pos) == '_') tag = ATTRIBUTE;
        else if (filename.charAt(pos) == '~') tag = TRANSITION;
        pos++;
    }

    /**
     * Find and save the next value
     */
    private void processOneValue() {
        //Report.print("processOneValue");
        setTag(); // find the tag
        //Report.print(tag.name());
        // save the tag name if it's not an ATTRIBUTE
        if (tag != ATTRIBUTE) {
            saveName();
            return;
        }
        // Find and save the next attribute given the position of the pointer (pos)
        if (setAttribute()) {
            saveValues();
        } else {
            pos++;
        }
    }

    private boolean setAttribute() {
        // if we're at the end of the filename, the attribute looks like a single _
        if (pos >= filename.length()) {
            attribute = APPEARS;
            return true;
        }
        // otherwise, if the current char is a digit, save it
        char c = filename.charAt(pos);
        //Report.print("attDig:"+c);
        if (Character.isDigit(c)) {
            attribute = APPEARS;
            return true;
        }
        if (!AttributeTypes.attributes.containsKey(c)) return false; // if the char is not a valid attribute, move the counter
        attribute = AttributeTypes.attributes.get(c); // otherwise, set the next attribute
        pos++;
        return true;
    }

    private void saveValues() {
        String str = getName();
        if (str.length() == 0) { // the attribute is for all pages
            attributes.addPage(attribute, -1);
            return;
        }
        StringBuilder pageBuilder = new StringBuilder();
        for (int i=0; i<str.length(); i++) {
            char c = str.charAt(i);
            if (c == ',') {
                addPage(pageBuilder.toString());
                pageBuilder = new StringBuilder();
            } else pageBuilder.append(c);
        }
        addPage(pageBuilder.toString());
        //Report.print(" new attribute -> "+pageBuilder.toString());
    }

    private void addPage(String page) {
        if (CommonRegex.isNumeric(page)) attributes.addPage(attribute, Integer.valueOf(page));
    }

    private void saveName() {
        String name = getBaseName(getName());
        if (tag == AttributeTypes.Tag.TRANSITION) attributes.addTransition(name);
        if (tag == STATE_NAME) attributes.setStateName(name);
        if (tag == AttributeTypes.Tag.IMAGE_NAME) attributes.setImageName(name);
    }

    private String getName() {
        StringBuilder str = new StringBuilder();
        while (pos < filename.length()) {
            Character c = filename.charAt(pos);
            if (AttributeTypes.tags.containsKey(c)) break;
            str.append(c);
            pos++;
        }
        return str.toString();
    }

    public String getBaseName(String name) {
        int startPos = name.length() - 1;
        int endPos = 0;
        for (int i=startPos; i>=0; i--) {
            if (!CommonRegex.isNumeric(name.substring(i))) {
                endPos = i + 1;
                break;
            }
        }
        return name.substring(0, endPos);
    }

}
