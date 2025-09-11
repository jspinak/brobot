package io.github.jspinak.brobot.tools.ml.dataset.encoding;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.tools.ml.dataset.model.ActionVector;

/**
 * Interface for bidirectional translation between ActionOptions and ActionVectors.
 *
 * <p>This interface defines the contract for converting GUI automation actions into numerical
 * vectors suitable for machine learning, and vice versa. Different implementations can provide
 * various encoding strategies (e.g., one-hot encoding, continuous encoding) based on the specific
 * requirements of the ML model being used.
 *
 * <p>Implementations should ensure that the translation is as lossless as possible, preserving all
 * relevant information needed to reconstruct the original action from its vector representation.
 *
 * @see OneHotActionVectorEncoder
 * @see ActionVector
 * @see ActionOptions
 */
public interface ActionVectorTranslator {

    /**
     * Converts an action result into a numerical vector representation.
     *
     * <p>This method extracts relevant information from the ActionResult, including the action
     * configuration and execution results (such as matched coordinates), and encodes them into a
     * fixed-size numerical vector suitable for ML processing.
     *
     * @param matches The ActionResult containing the action configuration and execution results
     * @return An ActionVector containing the numerical representation of the action
     */
    ActionVector toVector(ActionResult matches);

    /**
     * Reconstructs ActionConfig from a numerical vector representation.
     *
     * <p>This method performs the inverse operation of toVector, reconstructing the action
     * configuration from its numerical representation. Note that some information may be lost in
     * the translation process depending on the encoding strategy used.
     *
     * @param actionVector The numerical vector representation of an action
     * @return ActionConfig reconstructed from the vector
     */
    ActionConfig toActionConfig(ActionVector actionVector);
}
