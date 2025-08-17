package io.github.jspinak.brobot.action.logging;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Engine for processing dynamic message templates with variable substitution.
 * Supports placeholders like ${variableName} that get replaced with actual values.
 */
@Component
public class DynamicMessageTemplateEngine {
    
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private final Map<String, Object> globalVariables = new HashMap<>();
    
    /**
     * Parse a template and return a list of variable names found
     */
    public String[] parse(String template) {
        if (template == null) return new String[0];
        
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        return matcher.results()
            .map(mr -> mr.group(1))
            .toArray(String[]::new);
    }
    
    /**
     * Substitute variables in a template with their values
     */
    public String substitute(String template, Map<String, Object> variables) {
        if (template == null) return "";
        
        // Combine global and local variables, with local taking precedence
        Map<String, Object> allVariables = new HashMap<>(globalVariables);
        if (variables != null) {
            allVariables.putAll(variables);
        }
        
        // Add system variables
        allVariables.put("timestamp", LocalDateTime.now().format(DATE_FORMAT));
        allVariables.put("thread", Thread.currentThread().getName());
        
        // Replace placeholders
        String result = template;
        for (Map.Entry<String, Object> entry : allVariables.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            String value = String.valueOf(entry.getValue());
            result = result.replace(placeholder, value);
        }
        
        return result;
    }
    
    /**
     * Create a new template with predefined variables
     */
    public MessageTemplate withVariables(String template, Map<String, Object> variables) {
        return new MessageTemplate(template, variables);
    }
    
    /**
     * Format a message using ActionConfig and ActionResult context
     */
    public String formatMessage(String template, ActionConfig config, ActionResult result) {
        Map<String, Object> variables = new HashMap<>();
        
        // Add action config variables
        if (config != null) {
            variables.put("configClass", config.getClass().getSimpleName());
        }
        
        // Add action result variables
        if (result != null) {
            variables.put("success", result.isSuccess());
            variables.put("matchCount", result.getMatchList().size());
            variables.put("duration", result.getDuration());
            if (!result.getMatchList().isEmpty()) {
                variables.put("firstMatchScore", result.getMatchList().get(0).getScore());
            }
        }
        
        return substitute(template, variables);
    }
    
    /**
     * Set a global variable that will be available in all templates
     */
    public void setGlobalVariable(String name, Object value) {
        globalVariables.put(name, value);
    }
    
    /**
     * Clear all global variables
     */
    public void clearGlobalVariables() {
        globalVariables.clear();
    }
    
    /**
     * Helper class representing a template with its variables
     */
    public class MessageTemplate {
        private final String template;
        private final Map<String, Object> variables;
        
        public MessageTemplate(String template, Map<String, Object> variables) {
            this.template = template;
            this.variables = variables != null ? new HashMap<>(variables) : new HashMap<>();
        }
        
        public MessageTemplate withVariable(String name, Object value) {
            variables.put(name, value);
            return this;
        }
        
        public String render() {
            return substitute(template, variables);
        }
        
        public String render(Map<String, Object> additionalVariables) {
            Map<String, Object> combined = new HashMap<>(variables);
            if (additionalVariables != null) {
                combined.putAll(additionalVariables);
            }
            return substitute(template, combined);
        }
    }
}