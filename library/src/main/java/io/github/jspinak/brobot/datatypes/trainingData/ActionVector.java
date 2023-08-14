package io.github.jspinak.brobot.datatypes.trainingData;

import lombok.Getter;

/**
 * An ActionVector is the translation of the ActionOptions variable into a vector of numbers,
 * to be used for machine learning. The data type short (-32768 to 32767) is used since some of the values
 * are categories. Byte would have been used if all values were categories. The values with a range,
 * for example the x and y locations of a click, cannot have decimal values.
 * Other ranged values such as time and similarity scores, which are saved as doubles in the ActionOptions variable,
 * are multiplied by 1000 and stored as short data types in the vector representation.
 */
@Getter
public class ActionVector {

    private short[] vector = new short[100];

}
