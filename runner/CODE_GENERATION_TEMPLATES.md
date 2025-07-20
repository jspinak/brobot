# Code Generation Templates for Brobot Runner Refactoring

## Overview

These templates accelerate refactoring by providing consistent, AI-friendly code generation patterns that follow all architectural principles.

## 1. Service Class Template

### Template: `service-template.vm`
```velocity
package ${package}.${module}.service;

import ${package}.common.diagnostics.DiagnosticCapable;
import ${package}.common.diagnostics.DiagnosticInfo;
import ${package}.${module}.repository.${entityName}Repository;
import ${package}.${module}.events.${entityName}EventPublisher;
import ${package}.${module}.model.${entityName};
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Service responsible for ${entityName} business logic.
 * 
 * Behavioral Contract:
 * - ${contractPoint1}
 * - ${contractPoint2}
 * - All operations are idempotent
 * 
 * @since ${version}
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ${entityName}Service implements DiagnosticCapable {
    
    private final ${entityName}Repository repository;
    private final ${entityName}EventPublisher eventPublisher;
    private final ${entityName}Validator validator;
    
    #foreach($field in $stateFields)
    private final ${field.type} ${field.name} = new ${field.initializer};
    #end
    
    /**
     * ${primaryOperationDescription}
     * 
     * @param ${paramName} ${paramDescription}
     * @return ${returnDescription}
     * @throws ${exceptionType} if ${exceptionCondition}
     */
    public ${returnType} ${primaryOperation}(${paramType} ${paramName}) {
        String correlationId = MDC.get("correlationId");
        log.info("[{}] Starting ${primaryOperation}: {}", correlationId, ${paramName});
        
        try {
            // Validate input
            ValidationResult validation = validator.validate(${paramName});
            if (!validation.isValid()) {
                throw new ValidationException(
                    "Validation failed for ${primaryOperation}", 
                    validation.getErrors()
                );
            }
            
            // Business logic
            ${entityName} entity = process${entityName}(${paramName});
            
            // Persist
            CompletableFuture<${entityName}> future = repository.save(entity);
            ${entityName} saved = future.join();
            
            // Publish event
            eventPublisher.publish${entityName}${eventType}(saved);
            
            log.info("[{}] Successfully completed ${primaryOperation}", correlationId);
            return saved;
            
        } catch (Exception e) {
            log.error("[{}] Failed to ${primaryOperation}", correlationId, e);
            throw new ${module}Exception(
                String.format("Failed to ${primaryOperation}: %s", e.getMessage()), 
                e
            );
        }
    }
    
    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        return DiagnosticInfo.builder()
            .component("${entityName}Service")
            #foreach($diagnostic in $diagnosticStates)
            .state("${diagnostic.key}", ${diagnostic.value})
            #end
            .timestamp(Instant.now())
            .build();
    }
    
    #foreach($method in $additionalMethods)
    ${method}
    #end
}
```

### Usage Example
```java
@Component
public class ServiceGenerator {
    
    public void generateService(ServiceSpec spec) {
        VelocityContext context = new VelocityContext();
        context.put("package", "io.github.jspinak.brobot.runner");
        context.put("module", spec.getModule());
        context.put("entityName", spec.getEntityName());
        context.put("version", "1.0.0");
        
        // Behavioral contract
        context.put("contractPoint1", "Only one active " + spec.getEntityName());
        context.put("contractPoint2", "State transitions are atomic");
        
        // Primary operation
        context.put("primaryOperation", spec.getPrimaryOperation());
        context.put("primaryOperationDescription", spec.getOperationDescription());
        
        // Generate
        String code = velocityEngine.mergeTemplate(
            "service-template.vm", 
            context
        );
        
        Path outputPath = Paths.get(
            "src/main/java",
            spec.getPackagePath(),
            spec.getEntityName() + "Service.java"
        );
        
        Files.writeString(outputPath, code);
    }
}
```

## 2. Repository Template

### Template: `repository-template.vm`
```velocity
package ${package}.${module}.repository;

import ${package}.${module}.model.${entityName};
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Repository interface for ${entityName} persistence.
 * 
 * All methods return CompletableFuture for async operations.
 * Implementations should handle errors by completing exceptionally.
 */
public interface ${entityName}Repository {
    
    /**
     * Saves the given ${entityName}.
     * 
     * @param ${entityVarName} the entity to save
     * @return saved entity
     */
    CompletableFuture<${entityName}> save(${entityName} ${entityVarName});
    
    /**
     * Finds ${entityName} by ID.
     * 
     * @param id the entity ID
     * @return Optional containing the entity if found
     */
    CompletableFuture<Optional<${entityName}>> findById(${idType} id);
    
    /**
     * Finds all ${entityName} entities.
     * 
     * @return list of all entities
     */
    CompletableFuture<List<${entityName}>> findAll();
    
    /**
     * Deletes ${entityName} by ID.
     * 
     * @param id the entity ID
     * @return true if deleted, false if not found
     */
    CompletableFuture<Boolean> delete(${idType} id);
    
    #foreach($customMethod in $customMethods)
    /**
     * ${customMethod.description}
     */
    CompletableFuture<${customMethod.returnType}> ${customMethod.name}(${customMethod.params});
    #end
}

---

package ${package}.${module}.repository.impl;

import ${package}.${module}.repository.${entityName}Repository;
import ${package}.common.diagnostics.DiagnosticCapable;
import ${package}.common.diagnostics.DiagnosticInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Slf4j
@RequiredArgsConstructor
public class File${entityName}Repository implements ${entityName}Repository, DiagnosticCapable {
    
    private static final String STORAGE_DIR = "${storageDir}";
    private static final String FILE_EXTENSION = ".${extension}.json";
    
    private final Path storagePath = Paths.get(STORAGE_DIR);
    private final ObjectMapper objectMapper;
    private final ${entityName}Validator validator;
    
    // Cache for performance
    private final Map<${idType}, ${entityName}> cache = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void initialize() {
        try {
            Files.createDirectories(storagePath);
            log.info("Initialized ${entityName} repository at: {}", storagePath);
            loadCache();
        } catch (IOException e) {
            throw new RepositoryInitializationException(
                "Failed to initialize ${entityName} repository", e
            );
        }
    }
    
    @Override
    public CompletableFuture<${entityName}> save(${entityName} ${entityVarName}) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Validate
                ValidationResult validation = validator.validate(${entityVarName});
                if (!validation.isValid()) {
                    throw new ValidationException(
                        "Invalid ${entityName}", 
                        validation.getErrors()
                    );
                }
                
                // Serialize
                String json = objectMapper.writeValueAsString(${entityVarName});
                
                // Write to file
                Path filePath = storagePath.resolve(
                    ${entityVarName}.getId() + FILE_EXTENSION
                );
                Files.writeString(filePath, json);
                
                // Update cache
                cache.put(${entityVarName}.getId(), ${entityVarName});
                
                log.debug("Saved ${entityName}: {}", ${entityVarName}.getId());
                return ${entityVarName};
                
            } catch (Exception e) {
                throw new RepositoryException(
                    "Failed to save ${entityName}", e
                );
            }
        });
    }
    
    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        try {
            long fileCount = Files.list(storagePath)
                .filter(p -> p.toString().endsWith(FILE_EXTENSION))
                .count();
                
            return DiagnosticInfo.builder()
                .component("File${entityName}Repository")
                .state("storageDirectory", storagePath.toAbsolutePath())
                .state("fileCount", fileCount)
                .state("cacheSize", cache.size())
                .state("cacheHitRate", calculateCacheHitRate())
                .build();
                
        } catch (IOException e) {
            return DiagnosticInfo.error("File${entityName}Repository", e);
        }
    }
}
```

## 3. View Model Template

### Template: `viewmodel-template.vm`
```velocity
package ${package}.${module}.ui.viewmodel;

import ${package}.${module}.service.${entityName}Service;
import ${package}.common.diagnostics.DiagnosticCapable;
import ${package}.common.diagnostics.DiagnosticInfo;
import ${package}.common.ui.Command;
import ${package}.common.ui.AsyncCommand;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * View Model for ${viewName}.
 * 
 * Provides reactive properties and commands for UI binding.
 * All service interactions are asynchronous.
 */
@Component
@Scope("prototype")
@Slf4j
public class ${viewModelName} implements ViewModel, DiagnosticCapable {
    
    // Services
    private final ${entityName}Service ${entityVarName}Service;
    private final ValidationService validationService;
    private final EventBus eventBus;
    
    // Observable Properties
    #foreach($property in $properties)
    private final ${property.propertyType} ${property.name} = 
        new Simple${property.propertyType}(${property.defaultValue});
    #end
    
    // Observable Collections
    #foreach($collection in $collections)
    private final ObservableList<${collection.itemType}> ${collection.name} = 
        FXCollections.observableArrayList();
    #end
    
    // Commands
    #foreach($command in $commands)
    private final Command ${command.name}Command;
    #end
    
    public ${viewModelName}(
            ${entityName}Service ${entityVarName}Service,
            ValidationService validationService,
            EventBus eventBus) {
        
        this.${entityVarName}Service = ${entityVarName}Service;
        this.validationService = validationService;
        this.eventBus = eventBus;
        
        // Initialize commands
        #foreach($command in $commands)
        this.${command.name}Command = new AsyncCommand(
            this::${command.method},
            ${command.canExecuteBinding},
            "${command.description}"
        );
        #end
        
        // Setup reactive bindings
        setupBindings();
        
        // Subscribe to events
        subscribeToEvents();
    }
    
    // Property getters
    #foreach($property in $properties)
    public ${property.readOnlyType} ${property.name}Property() {
        return ${property.name};
    }
    #end
    
    // Collection getters
    #foreach($collection in $collections)
    public ObservableList<${collection.itemType}> ${collection.getter}() {
        return ${collection.name};
    }
    #end
    
    // Command getters
    #foreach($command in $commands)
    public Command ${command.getter}() {
        return ${command.name}Command;
    }
    #end
    
    // Command implementations
    #foreach($command in $commands)
    private CompletableFuture<Void> ${command.method}() {
        return CompletableFuture.runAsync(() -> {
            try {
                log.info("Executing ${command.name}");
                loading.set(true);
                
                // Command logic
                ${command.implementation}
                
                // Update UI
                Platform.runLater(() -> {
                    // Update properties
                    updateUI();
                });
                
            } catch (Exception e) {
                log.error("Failed to ${command.name}", e);
                Platform.runLater(() -> {
                    errorMessage.set(e.getMessage());
                    showError.set(true);
                });
            } finally {
                Platform.runLater(() -> loading.set(false));
            }
        });
    }
    #end
    
    private void setupBindings() {
        // Validation binding
        valid.bind(Bindings.createBooleanBinding(
            () -> validationMessages.isEmpty(),
            validationMessages
        ));
        
        // Custom bindings
        #foreach($binding in $customBindings)
        ${binding}
        #end
    }
    
    private void subscribeToEvents() {
        #foreach($event in $events)
        eventBus.subscribe(${event.type}.class, this::on${event.handler});
        #end
    }
    
    @Override
    public void initialize() {
        log.debug("Initializing ${viewModelName}");
        // Load initial data
        refreshCommand.execute();
    }
    
    @Override
    public void dispose() {
        log.debug("Disposing ${viewModelName}");
        // Cleanup subscriptions
        eventBus.unsubscribe(this);
    }
    
    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        return DiagnosticInfo.builder()
            .component("${viewModelName}")
            .state("loading", loading.get())
            .state("valid", valid.get())
            .state("itemCount", ${mainCollection}.size())
            #foreach($diagnostic in $diagnosticStates)
            .state("${diagnostic.key}", ${diagnostic.value})
            #end
            .build();
    }
}
```

## 4. Test Template

### Template: `test-template.vm`
```velocity
package ${package}.${module}.${layer};

import ${package}.${module}.${layer}.${className};
import ${package}.test.builders.${entityName}TestDataBuilder;
import ${package}.test.framework.DiagnosticTestRunner;
import ${package}.test.framework.TestScenario;
import ${package}.test.framework.AIAssertions;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.concurrent.CompletableFuture;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for ${className}.
 * 
 * Test scenarios:
 #foreach($scenario in $scenarios)
 * - ${scenario.description}
 #end
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("${className} - ${testDescription}")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ${className}Test {
    
    #foreach($mock in $mocks)
    @Mock
    private ${mock.type} ${mock.name};
    #end
    
    @InjectMocks
    private ${className} ${classVarName};
    
    private ${entityName}TestDataBuilder testDataBuilder;
    
    @BeforeEach
    void setUp() {
        testDataBuilder = new ${entityName}TestDataBuilder();
        
        // Common mock setup
        #foreach($setup in $mockSetups)
        ${setup}
        #end
    }
    
    #foreach($test in $tests)
    @Test
    @Order(${test.order})
    @DisplayName("${test.displayName}")
    @TestScenario(
        given = "${test.given}",
        when = "${test.when}",
        then = "${test.then}",
        verifies = {${test.verifies}}
    )
    void ${test.methodName}() {
        // Given - ${test.given}
        ${test.givenCode}
        
        // When - ${test.when}
        ${test.whenCode}
        
        // Then - ${test.then}
        ${test.thenCode}
        
        // Verify interactions
        ${test.verifyCode}
        
        // AI-friendly assertions
        AIAssertions.assertWithContext(
            ${test.assertion.actual},
            ${test.assertion.expected},
            "${test.assertion.context}"
        );
    }
    #end
    
    @Nested
    @DisplayName("Error Scenarios")
    class ErrorScenarios {
        
        #foreach($errorTest in $errorTests)
        @Test
        @DisplayName("Should handle ${errorTest.errorType}")
        void shouldHandle${errorTest.errorType}() {
            // Given
            ${errorTest.setup}
            
            // When/Then
            assertThatThrownBy(() -> ${errorTest.action})
                .isInstanceOf(${errorTest.exceptionClass}.class)
                .hasMessageContaining("${errorTest.expectedMessage}")
                .satisfies(e -> {
                    // Additional assertions
                    ${errorTest.additionalAssertions}
                });
        }
        #end
    }
    
    @Nested
    @DisplayName("Diagnostic Tests")
    class DiagnosticTests {
        
        @Test
        @DisplayName("Should provide diagnostic information")
        void shouldProvideDiagnosticInfo() {
            // When
            DiagnosticInfo info = ${classVarName}.getDiagnosticInfo();
            
            // Then
            assertThat(info).isNotNull();
            assertThat(info.getComponent()).isEqualTo("${className}");
            assertThat(info.getStates()).containsKeys(
                #foreach($state in $diagnosticStates)
                "${state}"#if($foreach.hasNext), #end
                #end
            );
        }
    }
}
```

## 5. Plugin Template

### Template: `plugin-template.vm`
```velocity
package ${package}.plugins.${pluginId};

import ${package}.plugin.api.*;
import ${package}.common.diagnostics.DiagnosticCapable;
import ${package}.common.diagnostics.DiagnosticInfo;
import lombok.extern.slf4j.Slf4j;

/**
 * ${pluginDescription}
 * 
 * @version ${version}
 * @author ${author}
 */
@PluginInfo(
    id = "${pluginId}",
    name = "${pluginName}",
    version = "${version}",
    type = PluginType.${pluginType}
)
@Slf4j
public class ${pluginClassName} implements ${pluginInterface}, DiagnosticCapable {
    
    private PluginContext context;
    private volatile boolean enabled = false;
    
    #foreach($service in $services)
    private ${service.type} ${service.name};
    #end
    
    @Override
    public PluginMetadata getMetadata() {
        return PluginMetadata.builder()
            .id("${pluginId}")
            .name("${pluginName}")
            .version("${version}")
            .description("${pluginDescription}")
            .author("${author}")
            .type(PluginType.${pluginType})
            .dependencies(List.of(
                #foreach($dep in $dependencies)
                new PluginDependency("${dep.id}", "${dep.version}", ${dep.required})
                #if($foreach.hasNext), #end
                #end
            ))
            .requiredPermissions(List.of(
                #foreach($perm in $permissions)
                "${perm}"#if($foreach.hasNext), #end
                #end
            ))
            .build();
    }
    
    @Override
    public void onEnable(PluginContext context) throws PluginException {
        this.context = context;
        log.info("Enabling ${pluginName} plugin");
        
        try {
            // Initialize services
            #foreach($service in $services)
            ${service.name} = new ${service.implType}();
            context.registerService(${service.type}.class, ${service.name});
            #end
            
            // Subscribe to events
            #foreach($event in $events)
            context.getEventBus().subscribe(
                ${event.type}.class, 
                this::${event.handler}
            );
            #end
            
            // Register UI components (if applicable)
            #if($hasUI)
            context.getUIIntegration().ifPresent(ui -> {
                ui.registerModule(new ${pluginId}UIModule());
            });
            #end
            
            enabled = true;
            log.info("${pluginName} plugin enabled successfully");
            
        } catch (Exception e) {
            throw new PluginException(
                "Failed to enable ${pluginName} plugin", e
            );
        }
    }
    
    @Override
    public void onDisable() {
        log.info("Disabling ${pluginName} plugin");
        
        if (!enabled) {
            return;
        }
        
        try {
            // Cleanup resources
            #foreach($service in $services)
            if (${service.name} != null) {
                ${service.name}.shutdown();
            }
            #end
            
            // Unsubscribe from events
            context.getEventBus().unsubscribeAll(this);
            
            enabled = false;
            log.info("${pluginName} plugin disabled");
            
        } catch (Exception e) {
            log.error("Error disabling plugin", e);
        }
    }
    
    @Override
    public HealthStatus getHealth() {
        if (!enabled) {
            return HealthStatus.DOWN;
        }
        
        // Check service health
        #foreach($service in $services)
        if (!${service.name}.isHealthy()) {
            return HealthStatus.DEGRADED;
        }
        #end
        
        return HealthStatus.UP;
    }
    
    @Override
    public SelfTestResult selfTest() {
        try {
            // Run self tests
            #foreach($test in $selfTests)
            ${test}
            #end
            
            return SelfTestResult.success(
                "All self tests passed"
            );
            
        } catch (Exception e) {
            return SelfTestResult.failure(
                "Self test failed", e
            );
        }
    }
    
    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        return DiagnosticInfo.builder()
            .component("${pluginClassName}")
            .state("enabled", enabled)
            .state("health", getHealth())
            #foreach($diagnostic in $diagnostics)
            .state("${diagnostic.key}", ${diagnostic.value})
            #end
            .build();
    }
    
    #foreach($method in $pluginMethods)
    ${method}
    #end
}
```

## 6. Code Generation CLI

### Usage Examples

```bash
# Generate a complete service layer
brobot-gen service \
  --entity Session \
  --module session \
  --operations "start,end,recover" \
  --repository file

# Generate repository with custom methods
brobot-gen repository \
  --entity Configuration \
  --custom-methods "findByProjectName,findActive" \
  --storage file

# Generate view model for UI
brobot-gen viewmodel \
  --view ConfigurationPanel \
  --properties "projectPath:String,loading:Boolean" \
  --commands "load,save,validate"

# Generate test suite
brobot-gen test \
  --class SessionService \
  --scenarios "create,update,delete,concurrent" \
  --with-errors

# Generate plugin
brobot-gen plugin \
  --id markdown-processor \
  --type DATA \
  --services "MarkdownProcessor,MarkdownRenderer"

# Generate complete module
brobot-gen module \
  --name notification \
  --layers "service,repository,controller,ui" \
  --with-tests
```

### Configuration File: `codegen.yaml`

```yaml
generation:
  basePackage: io.github.jspinak.brobot.runner
  outputDirectory: src/main/java
  testOutputDirectory: src/test/java
  
templates:
  directory: templates/
  customTemplates:
    - name: dto-template
      file: dto-template.vm
    - name: mapper-template
      file: mapper-template.vm
      
defaults:
  author: "Brobot Team"
  version: "1.0.0"
  diagnosticStates:
    - key: "status"
      value: "getStatus()"
    - key: "lastOperation"
      value: "getLastOperationTime()"
      
conventions:
  entityNamePattern: "^[A-Z][a-zA-Z0-9]+$"
  generateBuilder: true
  generateToString: true
  useLombok: true
  
validation:
  validateNames: true
  checkExisting: true
  backupExisting: true
```

## Benefits

1. **Consistency**: All generated code follows the same patterns
2. **Speed**: Generate boilerplate in seconds
3. **Quality**: Built-in best practices and AI-friendly patterns
4. **Customization**: Templates can be modified for specific needs
5. **Documentation**: Generated code includes comprehensive JavaDoc
6. **Testing**: Automatic test generation with scenarios

This template system significantly accelerates the refactoring process while ensuring consistency and quality across the codebase.