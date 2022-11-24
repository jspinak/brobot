package io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles;

import lombok.Getter;
import lombok.Setter;

import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorSchema.ColorValue.*;

@Getter
@Setter
public class ColorSchemaBGR extends ColorSchema {

    public ColorSchemaBGR() {
        super(BLUE, GREEN, RED);
    }
}
