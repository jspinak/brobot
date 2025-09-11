/**
 * Contains Data Transfer Objects (DTOs) for external API communication and serialization.
 *
 * <p>This package provides lightweight, serialization-friendly representations of logging domain
 * models. DTOs in this package are designed to facilitate data exchange between the Brobot logging
 * framework and external systems, such as REST APIs, message queues, or persistence layers.
 *
 * <h2>Key Classes</h2>
 *
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.tools.logging.dto.LogDataDTO} - Simplified representation
 *       of log entries for external consumption
 *   <li>{@link io.github.jspinak.brobot.tools.logging.dto.ExecutionMetricsDTO} - Flattened view of
 *       performance and execution statistics
 *   <li>{@link io.github.jspinak.brobot.tools.logging.dto.StateImageLogDTO} - Serializable
 *       representation of state-related visual logging data
 * </ul>
 *
 * <h2>Design Principles</h2>
 *
 * <ul>
 *   <li><strong>Simplicity</strong>: DTOs use primitive types and simple structures to ensure
 *       compatibility with various serialization frameworks
 *   <li><strong>Decoupling</strong>: DTOs are independent of domain logic and internal
 *       implementation details
 *   <li><strong>Immutability</strong>: DTOs are designed as immutable data carriers to ensure
 *       thread safety and data integrity
 *   <li><strong>Null Safety</strong>: DTOs handle null values gracefully without default values
 *       that might mask data issues
 * </ul>
 *
 * <h2>Usage Example</h2>
 *
 * <pre>{@code
 * // Converting domain model to DTO
 * LogData logData = createLogData();
 * LogDataDTO dto = LogDataMapper.toDTO(logData);
 *
 * // Serializing to JSON
 * String json = objectMapper.writeValueAsString(dto);
 *
 * // Deserializing from JSON
 * LogDataDTO receivedDto = objectMapper.readValue(json, LogDataDTO.class);
 * LogData domainModel = LogDataMapper.toDomain(receivedDto);
 * }</pre>
 *
 * <h2>Integration Notes</h2>
 *
 * <p>These DTOs are designed to work seamlessly with common Java serialization frameworks including
 * Jackson, Gson, and standard Java serialization. They avoid framework-specific annotations to
 * maintain portability.
 *
 * @see io.github.jspinak.brobot.tools.logging.mapper.LogDataMapper
 * @see io.github.jspinak.brobot.tools.logging.model
 * @since 1.0
 */
package io.github.jspinak.brobot.tools.logging.dto;
