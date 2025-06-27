/**
 * Contains mapper utilities for converting between domain models and DTOs.
 * 
 * <p>This package provides transformation utilities that handle the bidirectional
 * conversion between internal domain models and their external representations.
 * Mappers ensure data integrity and proper null handling during the transformation
 * process while maintaining a clean separation between the domain and API layers.
 * 
 * <h2>Main Components</h2>
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.tools.logging.mapper.LogDataMapper} - 
 *       Handles conversion between {@link io.github.jspinak.brobot.tools.logging.model.LogData}
 *       and {@link io.github.jspinak.brobot.tools.logging.dto.LogDataDTO}</li>
 * </ul>
 * 
 * <h2>Mapping Strategies</h2>
 * <ul>
 *   <li><strong>Null Safety</strong>: All mappers handle null inputs gracefully,
 *       returning null for null inputs to maintain API contracts</li>
 *   <li><strong>Defensive Copying</strong>: Collections and mutable objects are
 *       defensively copied to prevent unintended modifications</li>
 *   <li><strong>Type Conversion</strong>: Enums are converted to strings for DTOs
 *       to ensure serialization compatibility</li>
 *   <li><strong>Nested Object Handling</strong>: Complex nested structures are
 *       properly mapped recursively</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Domain to DTO conversion
 * LogData domainObject = new LogData();
 * domainObject.setType(LogEventType.ACTION);
 * domainObject.setAction("click");
 * domainObject.setTarget("submitButton");
 * 
 * LogDataDTO dto = LogDataMapper.toDTO(domainObject);
 * // dto.getType() returns "ACTION" as String
 * 
 * // DTO to Domain conversion
 * LogDataDTO externalDto = receiveFromAPI();
 * LogData internalModel = LogDataMapper.toDomain(externalDto);
 * // Enum types are properly reconstructed
 * }</pre>
 * 
 * <h2>Design Considerations</h2>
 * <ul>
 *   <li>Mappers are implemented as utility classes with static methods for
 *       simplicity and stateless operation</li>
 *   <li>No external dependencies are required, keeping the mapping layer
 *       lightweight</li>
 *   <li>Validation is minimal - mappers focus on transformation, not business
 *       rules</li>
 *   <li>Performance is optimized for typical logging volumes with minimal
 *       object allocation</li>
 * </ul>
 * 
 * <h2>Extension Guidelines</h2>
 * <p>When adding new mappers:
 * <ul>
 *   <li>Follow the established naming convention: {Entity}Mapper</li>
 *   <li>Provide both toDTO() and toDomain() methods</li>
 *   <li>Handle null cases explicitly</li>
 *   <li>Document any special conversion rules</li>
 *   <li>Consider adding batch conversion methods for collections if needed</li>
 * </ul>
 * 
 * @see io.github.jspinak.brobot.tools.logging.model
 * @see io.github.jspinak.brobot.tools.logging.dto
 * @since 1.0
 */
package io.github.jspinak.brobot.tools.logging.mapper;