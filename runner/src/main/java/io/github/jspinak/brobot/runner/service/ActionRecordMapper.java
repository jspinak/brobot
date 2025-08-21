package io.github.jspinak.brobot.runner.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.basic.vanish.VanishOptions;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.runner.persistence.entity.ActionRecordEntity;
import io.github.jspinak.brobot.runner.persistence.entity.MatchEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between ActionRecord (library) and ActionRecordEntity (persistence).
 * Handles serialization of ActionConfig objects and Match conversion.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ActionRecordMapper {
    
    private final ObjectMapper objectMapper;
    
    /**
     * Convert ActionRecord from library to persistence entity
     */
    public ActionRecordEntity toEntity(ActionRecord record) {
        ActionRecordEntity entity = new ActionRecordEntity();
        
        // Map ActionConfig
        if (record.getActionConfig() != null) {
            entity.setActionConfigType(record.getActionConfig().getClass().getSimpleName());
            try {
                entity.setActionConfigJson(objectMapper.writeValueAsString(record.getActionConfig()));
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize ActionConfig: {}", e.getMessage());
            }
        }
        
        // Map basic fields
        entity.setActionSuccess(record.isActionSuccess());
        entity.setDuration((long)(record.getDuration() * 1000)); // Convert seconds to milliseconds
        entity.setText(record.getText());
        // entity.setStateId(record.getStateName()); // TODO: Convert stateName to stateId
        
        // Map matches
        if (record.getMatchList() != null && !record.getMatchList().isEmpty()) {
            List<MatchEntity> matchEntities = record.getMatchList().stream()
                .map(this::toMatchEntity)
                .collect(Collectors.toList());
            
            // Set bidirectional relationship
            matchEntities.forEach(entity::addMatch);
        }
        
        return entity;
    }
    
    /**
     * Convert persistence entity to ActionRecord for library use
     */
    public ActionRecord fromEntity(ActionRecordEntity entity) {
        ActionRecord.Builder builder = new ActionRecord.Builder();
        
        // Deserialize ActionConfig
        if (entity.getActionConfigJson() != null) {
            try {
                ActionConfig config = deserializeActionConfig(
                    entity.getActionConfigType(), 
                    entity.getActionConfigJson()
                );
                builder.setActionConfig(config);
            } catch (Exception e) {
                log.error("Failed to deserialize ActionConfig: {}", e.getMessage());
            }
        }
        
        // Map basic fields
        builder.setActionSuccess(entity.isActionSuccess());
        builder.setDuration(entity.getDuration() / 1000.0); // Convert milliseconds to seconds
        builder.setText(entity.getText());
        // builder.setStateName(entity.getStateId()); // TODO: Convert stateId to stateName
        
        // Map matches
        if (entity.getMatches() != null && !entity.getMatches().isEmpty()) {
            List<Match> matches = entity.getMatches().stream()
                .map(this::fromMatchEntity)
                .collect(Collectors.toList());
            matches.forEach(builder::addMatch);
        }
        
        return builder.build();
    }
    
    /**
     * Convert Match to MatchEntity
     */
    private MatchEntity toMatchEntity(Match match) {
        MatchEntity entity = new MatchEntity();
        
        Region region = match.getRegion();
        entity.setX(region.getX());
        entity.setY(region.getY());
        entity.setWidth(region.getW());
        entity.setHeight(region.getH());
        entity.setSimScore(match.getScore());
        
        // Set anchor points from target location
        if (match.getTarget() != null) {
            entity.setAnchorX(match.getTarget().getX());
            entity.setAnchorY(match.getTarget().getY());
        }
        
        // Set pattern name if available
        if (match.getName() != null) {
            entity.setPatternName(match.getName());
        }
        
        // Screenshot path not directly available in Match
        // entity.setScreenshotPath(...);
        
        return entity;
    }
    
    /**
     * Convert MatchEntity to Match
     */
    private Match fromMatchEntity(MatchEntity entity) {
        Match.Builder builder = new Match.Builder();
        
        // Set region
        builder.setRegion(entity.getX(), entity.getY(), 
                         entity.getWidth(), entity.getHeight());
        
        // Set similarity score
        builder.setSimScore(entity.getSimScore());
        
        // Set target location as anchor point
        builder.setOffset(new Location(entity.getAnchorX(), entity.getAnchorY()));
        
        // Set name if available
        if (entity.getPatternName() != null) {
            builder.setName(entity.getPatternName());
        }
        
        return builder.build();
    }
    
    /**
     * Deserialize ActionConfig from JSON based on type
     */
    private ActionConfig deserializeActionConfig(String type, String json) 
            throws JsonProcessingException {
        
        if (type == null || json == null) {
            return null;
        }
        
        // Map simple class names to full qualified names
        Class<?> configClass = getActionConfigClass(type);
        
        if (configClass == null) {
            log.warn("Unknown ActionConfig type: {}", type);
            return new PatternFindOptions.Builder().build(); // Default
        }
        
        return (ActionConfig) objectMapper.readValue(json, configClass);
    }
    
    /**
     * Get ActionConfig class from simple name
     */
    private Class<?> getActionConfigClass(String simpleName) {
        switch (simpleName) {
            case "PatternFindOptions":
                return PatternFindOptions.class;
            case "ClickOptions":
                return ClickOptions.class;
            case "TypeOptions":
                return TypeOptions.class;
            case "VanishOptions":
                return VanishOptions.class;
            case "ColorFindOptions":
                try {
                    return Class.forName("io.github.jspinak.brobot.action.basic.find.ColorFindOptions");
                } catch (ClassNotFoundException e) {
                    log.warn("ColorFindOptions class not found");
                    return null;
                }
            case "MotionFindOptions":
                try {
                    return Class.forName("io.github.jspinak.brobot.action.basic.find.MotionFindOptions");
                } catch (ClassNotFoundException e) {
                    log.warn("MotionFindOptions class not found");
                    return null;
                }
            case "DragOptions":
                try {
                    return Class.forName("io.github.jspinak.brobot.action.composite.drag.DragOptions");
                } catch (ClassNotFoundException e) {
                    log.warn("DragOptions class not found");
                    return null;
                }
            case "MouseMoveOptions":
                try {
                    return Class.forName("io.github.jspinak.brobot.action.basic.mouse.MouseMoveOptions");
                } catch (ClassNotFoundException e) {
                    log.warn("MouseMoveOptions class not found");
                    return null;
                }
            case "DefineRegionOptions":
                try {
                    return Class.forName("io.github.jspinak.brobot.action.basic.region.DefineRegionOptions");
                } catch (ClassNotFoundException e) {
                    log.warn("DefineRegionOptions class not found");
                    return null;
                }
            case "HighlightOptions":
                try {
                    return Class.forName("io.github.jspinak.brobot.action.basic.highlight.HighlightOptions");
                } catch (ClassNotFoundException e) {
                    log.warn("HighlightOptions class not found");
                    return null;
                }
            default:
                log.warn("Unmapped ActionConfig type: {}", simpleName);
                return null;
        }
    }
    
    /**
     * Convert list of ActionRecords to entities
     */
    public List<ActionRecordEntity> toEntities(List<ActionRecord> records) {
        return records.stream()
            .map(this::toEntity)
            .collect(Collectors.toList());
    }
    
    /**
     * Convert list of entities to ActionRecords
     */
    public List<ActionRecord> fromEntities(List<ActionRecordEntity> entities) {
        return entities.stream()
            .map(this::fromEntity)
            .collect(Collectors.toList());
    }
}