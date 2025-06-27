package io.github.jspinak.brobot.runner.json.mixins;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Jackson mixin for JavaCV's Rect class to control JSON serialization.
 * <p>
 * This mixin prevents serialization of native pointer references used by JavaCV
 * for JNI (Java Native Interface) communication with native OpenCV libraries.
 * The pointer property contains memory addresses and native object references
 * that cannot be meaningfully serialized to JSON and would cause errors if
 * attempted.
 * <p>
 * Properties ignored:
 * <ul>
 * <li>pointer - Native memory pointer for JNI operations</li>
 * </ul>
 *
 * @see org.bytedeco.opencv.opencv_core.Rect
 * @see org.bytedeco.javacpp.Pointer
 * @see com.fasterxml.jackson.databind.ObjectMapper#addMixIn(Class, Class)
 */
@JsonIgnoreProperties({"pointer"})
public abstract class JavaCVRectMixin {
}

