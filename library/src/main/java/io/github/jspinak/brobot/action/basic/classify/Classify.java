package io.github.jspinak.brobot.action.basic.classify;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.FindColor;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Provides scene classification functionality through the Brobot action interface.
 * 
 * <p>Classify implements the ActionInterface to enable pixel-level scene
 * classification using color-based analysis. It serves as a thin wrapper
 * around FindColor, exposing classification capabilities through the
 * standard Brobot action framework.</p>
 * 
 * <p>Classification differs from standard finding operations in that it:</p>
 * <ul>
 *   <li>Classifies every pixel in the scene</li>
 *   <li>Returns regions sorted by size (largest first)</li>
 *   <li>Can operate without specific target images</li>
 *   <li>Provides complete scene segmentation</li>
 * </ul>
 * 
 * <p>Use classification when you need to:</p>
 * <ul>
 *   <li>Segment an entire scene into distinct regions</li>
 *   <li>Identify all instances of multiple patterns</li>
 *   <li>Analyze scene composition</li>
 *   <li>Detect state changes through scene analysis</li>
 * </ul>
 * 
 * @see ActionInterface
 * @see FindColor
 * @see ObjectCollection
 */
@Component
public class Classify implements ActionInterface {

    private final FindColor findColor;

    public Classify(FindColor findColor) {
        this.findColor = findColor;
    }

    @Override
    public ActionInterface.Type getActionType() {
        return ActionInterface.Type.CLASSIFY;
    }

    /**
     * Performs scene classification using color-based analysis.
     * 
     * <p>Delegates to FindColor which handles the actual classification
     * logic. The ActionResult will contain classified regions sorted
     * by size when using CLASSIFY action type.</p>
     * 
     * <p>ObjectCollections follow the standard pattern:</p>
     * <ul>
     *   <li>First: Target images (optional for pure classification)</li>
     *   <li>Second: Context images for classification</li>
     *   <li>Third+: Scenes to classify</li>
     * </ul>
     * 
     * @param matches accumulates classification results
     * @param objectCollections configures targets, context, and scenes
     */
    public void perform(ActionResult matches, ObjectCollection... objectCollections) {
        findColor.find(matches, List.of(objectCollections));
    }
}
