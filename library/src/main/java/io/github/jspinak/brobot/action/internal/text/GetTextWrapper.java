package io.github.jspinak.brobot.action.internal.text;

import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.config.core.FrameworkSettings;
import io.github.jspinak.brobot.tools.testing.mock.action.MockText;
import io.github.jspinak.brobot.action.ActionResult;

import org.sikuli.script.OCR;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;

/**
 * Provides OCR text extraction functionality from image regions with mock
 * support.
 * <p>
 * This wrapper class abstracts optical character recognition operations,
 * extracting
 * text from {@link Match} objects and their associated image regions. It
 * integrates
 * with Sikuli's OCR capabilities while providing a consistent interface for
 * both
 * real and mocked text extraction.
 * <p>
 * The class handles two main workflows:
 * <ul>
 * <li>Batch processing: Extract text from all matches in an
 * {@link ActionResult}</li>
 * <li>Individual processing: Extract text from a single {@link Match}</li>
 * </ul>
 * <p>
 * In mock mode, text extraction uses probabilistic models based on match
 * history
 * rather than actual OCR, ensuring realistic test behavior without requiring
 * actual image processing.
 * 
 * @see OCR
 * @see Match#setText(String)
 * @see MockText
 * @see FrameworkSettings#mock
 */
@Component
public class GetTextWrapper {

    private final MockText mockText;

    public GetTextWrapper(MockText mockText) {
        this.mockText = mockText;
    }

    /**
     * Extracts text from all matches in the action result and updates both the
     * matches and result.
     * <p>
     * This method processes each match in the provided {@link ActionResult},
     * performing
     * OCR on their associated image regions. Extracted text is stored in two
     * places:
     * <ul>
     * <li>In each individual {@link Match} object via {@link Match#setText}</li>
     * <li>In the {@link ActionResult}'s string collection for aggregate access</li>
     * </ul>
     * <p>
     * In mock mode, text is generated based on match history probabilities. The
     * mock
     * system ensures that repeated calls don't artificially increase success rates,
     * maintaining realistic test behavior.
     * 
     * @param matches The ActionResult containing matches to process. This object
     *                is modified by adding extracted text strings to its
     *                collection.
     * 
     * @implNote In mock mode, existing Match objects don't get new text to prevent
     *           probability manipulation through repeated calls.
     */
    public void getAllText(ActionResult matches) {
        matches.getMatchList().forEach(match -> setText(matches, match));
    }

    private void setText(ActionResult matches, Match match) {
        String str = getTextFromMatch(match);
        if (str.isEmpty())
            return;
        matches.addString(str);
        match.setText(str);
    }

    /**
     * Retrieves text from a match, using either existing text or mock generation.
     * <p>
     * In real mode, returns any text already stored in the match object.
     * In mock mode, generates text based on the match's history and probability
     * models.
     * This method doesn't perform OCR; it retrieves previously extracted text.
     * 
     * @param match The match object to retrieve text from
     * @return The text associated with the match, or mock-generated text in mock
     *         mode
     * 
     * @see Match#getText()
     * @see MockText#getString(Match)
     */
    public String getTextFromMatch(Match match) {
        if (FrameworkSettings.mock)
            return mockText.getString(match);
        return match.getText();
    }

    /**
     * Performs OCR on the match's image region and returns the extracted text.
     * <p>
     * This method directly invokes Sikuli's OCR engine on the BufferedImage
     * contained within the match. Unlike {@link #getTextFromMatch}, this method
     * performs actual text recognition rather than retrieving stored text.
     * <p>
     * If the match doesn't contain a valid image, returns an empty string.
     * 
     * @param match Must contain a valid BufferedImage in its image field
     * @return All recognized text as a single string, or empty string if
     *         no image is available or no text is detected
     * 
     * @see OCR#readText(BufferedImage)
     * @see Match#getImage()
     */
    public String getText(Match match) {
        BufferedImage bi = match.getImage().getBufferedImage();
        if (bi == null)
            return "";
        // OCR.Options myOptions = new OCR.Options().asLine();
        return OCR.readText(bi); // , myOptions);
    }

    /**
     * Performs OCR on the match's image and updates the match with the extracted
     * text.
     * <p>
     * This convenience method combines {@link #getText} with {@link Match#setText},
     * performing OCR and immediately storing the result in the match object.
     * This ensures the match contains the most current text from its image region.
     * 
     * @param match The match to process. Must contain a valid image for OCR.
     *              The match's text field will be updated with OCR results.
     * 
     * @implNote This method modifies the passed match object by setting its text
     *           field.
     */
    public void setText(Match match) {
        String text = getText(match);
        match.setText(text);
    }
}
