package io.github.jspinak.brobot.buildStateStructure.buildFromNames.attributes;

import io.github.jspinak.brobot.database.state.stateObject.stateImageObject.StateImageObject;
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
        filename = image.getName();
        pos = 0;
        while (pos < filename.length()) {
            processOneValue();
        }
    }

    private void setTag() {
        if (pos == 0) {
            tag = STATE_NAME;
            return;
        }
        if (filename.charAt(pos) == '-') tag = IMAGE_NAME;
        else if (filename.charAt(pos) == '_') tag = ATTRIBUTE;
        else if (filename.charAt(pos) == '~') tag = TRANSITION;
        pos++;
    }

    private void processOneValue() {
        setTag();
        if (tag != ATTRIBUTE) {
            saveName();
            return;
        }
        if (setAttribute()) {
            saveValues();
        } else {
            pos++;
        }
    }

    private boolean setAttribute() {
        char c = filename.charAt(pos);
        if (Character.isDigit(c)) {
            attribute = APPEARS;
            return true;
        }
        if (!AttributeTypes.attributes.containsKey(c)) return false;
        attribute = AttributeTypes.attributes.get(c);
        pos++;
        return true;
    }

    private void saveValues() {
        String str = getName();
        if (str.length() == 0) {
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
    }

    private void addPage(String page) {
        if (isNumeric(page)) attributes.addPage(attribute, Integer.valueOf(page));
    }

    private static boolean isNumeric(String str){
        return str != null && str.matches("[0-9.]+");
    }

    private void saveName() {
        String name = getName();
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


}
