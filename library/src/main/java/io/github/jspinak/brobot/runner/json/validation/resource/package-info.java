/**
 * Validators for external resource availability and integrity.
 *
 * <p>This package contains validators that verify external resources referenced in configurations
 * are available, accessible, and valid. This includes image files, data files, and other external
 * dependencies that the automation project requires at runtime.
 *
 * <h2>Validators</h2>
 *
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.runner.json.validation.resource.ImageResourceValidator} -
 *       Validates image file resources
 * </ul>
 *
 * <h2>Resource Types</h2>
 *
 * <h3>Image Resources</h3>
 *
 * <ul>
 *   <li>Pattern image files (.png, .jpg, .gif)
 *   <li>Screenshot references
 *   <li>Icon files
 *   <li>Dynamic image templates
 * </ul>
 *
 * <h3>File Resources</h3>
 *
 * <ul>
 *   <li>Configuration files
 *   <li>Data files
 *   <li>Script files
 *   <li>Template files
 * </ul>
 *
 * <h2>Validation Checks</h2>
 *
 * <h3>Existence Validation</h3>
 *
 * <pre>{@code
 * // Check if file exists
 * if (!imageFile.exists()) {
 *     result.addError(new ValidationError(
 *         ERROR,
 *         "Image file not found: " + imagePath,
 *         "$.states[0].images[0].path"
 *     ));
 * }
 * }</pre>
 *
 * <h3>Accessibility Validation</h3>
 *
 * <pre>{@code
 * // Check read permissions
 * if (!imageFile.canRead()) {
 *     result.addError(new ValidationError(
 *         ERROR,
 *         "Cannot read image file: " + imagePath,
 *         "$.states[0].images[0].path"
 *     ));
 * }
 * }</pre>
 *
 * <h3>Format Validation</h3>
 *
 * <pre>{@code
 * // Validate image format
 * try {
 *     BufferedImage img = ImageIO.read(imageFile);
 *     if (img == null) {
 *         result.addError(new ValidationError(
 *             ERROR,
 *             "Invalid image format: " + imagePath,
 *             jsonPath
 *         ));
 *     }
 * } catch (IOException e) {
 *     result.addError(new ValidationError(
 *         ERROR,
 *         "Cannot load image: " + e.getMessage(),
 *         jsonPath
 *     ));
 * }
 * }</pre>
 *
 * <h3>Size Validation</h3>
 *
 * <pre>{@code
 * // Check image dimensions
 * if (img.getWidth() > MAX_WIDTH || img.getHeight() > MAX_HEIGHT) {
 *     result.addWarning(new ValidationError(
 *         WARNING,
 *         String.format("Image too large: %dx%d (max %dx%d)",
 *             img.getWidth(), img.getHeight(),
 *             MAX_WIDTH, MAX_HEIGHT),
 *         jsonPath
 *     ));
 * }
 * }</pre>
 *
 * <h2>Path Resolution</h2>
 *
 * <p>Resource paths are resolved in order:
 *
 * <ol>
 *   <li>Absolute paths used as-is
 *   <li>Relative to project root
 *   <li>Relative to configured resource directories
 *   <li>Classpath resources
 * </ol>
 *
 * <pre>{@code
 * // Path resolution example
 * Path resolved = resourceResolver.resolve(
 *     "images/button.png",
 *     projectRoot,
 *     resourceDirs
 * );
 * }</pre>
 *
 * <h2>Batch Validation</h2>
 *
 * <pre>{@code
 * // Validate all images in parallel
 * List<CompletableFuture<ValidationResult>> futures =
 *     images.stream()
 *         .map(img -> CompletableFuture.supplyAsync(
 *             () -> validateImage(img)
 *         ))
 *         .collect(Collectors.toList());
 *
 * // Combine results
 * ValidationResult combined = futures.stream()
 *     .map(CompletableFuture::join)
 *     .reduce(new ValidationResult(), ValidationResult::merge);
 * }</pre>
 *
 * <h2>Caching</h2>
 *
 * <p>Resource validation can be expensive, so caching is recommended:
 *
 * <pre>{@code
 * private final Map<String, ValidationResult> cache =
 *     new ConcurrentHashMap<>();
 *
 * public ValidationResult validate(String resourcePath) {
 *     return cache.computeIfAbsent(resourcePath, path -> {
 *         // Perform actual validation
 *         return doValidate(path);
 *     });
 * }
 * }</pre>
 *
 * <h2>Error Recovery</h2>
 *
 * <pre>{@code
 * // Suggest alternatives for missing resources
 * if (!file.exists()) {
 *     List<String> similar = findSimilarFiles(
 *         file.getParent(),
 *         file.getName()
 *     );
 *
 *     if (!similar.isEmpty()) {
 *         error.setSuggestion(
 *             "Did you mean: " + similar.get(0) + "?"
 *         );
 *     }
 * }
 * }</pre>
 *
 * <h2>Best Practices</h2>
 *
 * <ul>
 *   <li>Validate resources early in startup
 *   <li>Cache validation results
 *   <li>Provide helpful path resolution hints
 *   <li>Support both file system and classpath resources
 *   <li>Validate in parallel for large projects
 *   <li>Include file size/format in error messages
 * </ul>
 *
 * @since 1.0
 * @see io.github.jspinak.brobot.runner.json.validation
 * @see java.nio.file.Path
 * @see javax.imageio.ImageIO
 */
package io.github.jspinak.brobot.runner.json.validation.resource;
