package io.github.jspinak.brobot.tools.ml.dataset.model;

import io.github.jspinak.brobot.tools.ml.dataset.encoding.ActionVectorTranslator;
import io.github.jspinak.brobot.tools.ml.dataset.encoding.OneHotActionVectorEncoder;

import lombok.Getter;

/**
 * Represents an action as a numerical vector for machine learning applications.
 *
 * <p>ActionVector translates {@link io.github.jspinak.brobot.action.ActionConfig} into a
 * fixed-size numeric vector suitable for neural networks and other ML algorithms. The vector uses
 * the short data type (-32,768 to 32,767) to accommodate both categorical and continuous values.
 *
 * <p><strong>Data encoding strategy:</strong>
 *
 * <ul>
 *   <li>Categorical values (e.g., action types) are encoded as discrete integers
 *   <li>Coordinate values (x, y, width, height) are stored directly as shorts
 *   <li>Continuous values (time, similarity scores) are multiplied by 1000 to preserve three
 *       decimal places of precision
 * </ul>
 *
 * <p>The vector size is fixed at 100 elements to provide space for various action parameters while
 * maintaining consistency across different action types.
 *
 * @see ActionVectorTranslator
 * @see OneHotActionVectorEncoder
 */
@Getter
public class ActionVector {

    /**
     * The size of the action vector. This provides sufficient space for encoding various action
     * parameters including action type, coordinates, options, and future expansion.
     */
    public static final int VECTOR_SIZE = 100;

    private short[] vector = new short[VECTOR_SIZE];
}
