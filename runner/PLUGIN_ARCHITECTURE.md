# Plugin Architecture Specification for Brobot Runner

## Overview

This specification defines a comprehensive plugin architecture that enables modular extensibility for the Brobot Runner. The architecture supports dynamic loading, isolation, lifecycle management, and seamless integration with the diagnostic infrastructure.

## Architecture Goals

1. **Dynamic Loading**: Load/unload plugins at runtime without restarting
2. **Isolation**: Plugins run in isolated contexts to prevent conflicts
3. **Dependency Management**: Handle inter-plugin dependencies gracefully
4. **API Stability**: Provide stable APIs that plugins can rely on
5. **Security**: Sandbox plugin execution for safety
6. **Diagnostics**: Full integration with diagnostic infrastructure

## Core Components

### 1. Plugin API Definition

```java
// Core plugin interface
public interface BrobotPlugin {
    /**
     * Unique identifier for the plugin
     */
    String getId();
    
    /**
     * Plugin metadata including version, dependencies, etc.
     */
    PluginMetadata getMetadata();
    
    /**
     * Called when plugin is loaded and should initialize
     */
    void onEnable(PluginContext context) throws PluginException;
    
    /**
     * Called when plugin is being unloaded
     */
    void onDisable();
    
    /**
     * Plugin health check
     */
    HealthStatus getHealth();
    
    /**
     * Self-test for validation
     */
    SelfTestResult selfTest();
}

// Extended plugin interfaces for specific capabilities
public interface UIPlugin extends BrobotPlugin {
    List<UIModule> getUIModules();
    List<MenuItem> getMenuItems();
    Optional<Node> getSettingsPanel();
}

public interface AutomationPlugin extends BrobotPlugin {
    List<AutomationAction> getActions();
    List<AutomationTrigger> getTriggers();
    AutomationProcessor getProcessor();
}

public interface DataPlugin extends BrobotPlugin {
    List<DataSource> getDataSources();
    List<DataTransformer> getTransformers();
    List<DataExporter> getExporters();
}
```

### 2. Plugin Metadata and Manifest

```java
@Value
@Builder
@JsonDeserialize(builder = PluginMetadata.PluginMetadataBuilder.class)
public class PluginMetadata {
    String id;
    String name;
    String version;
    String description;
    String author;
    String website;
    String mainClass;
    PluginType type;
    List<PluginDependency> dependencies;
    List<String> requiredPermissions;
    Map<String, Object> defaultConfiguration;
    ResourceRequirements resources;
    
    // Compatibility information
    String minBrobotVersion;
    String maxBrobotVersion;
    List<String> supportedPlatforms;
}

// Plugin manifest file (plugin.json)
{
  "id": "image-processor",
  "name": "Advanced Image Processor",
  "version": "1.2.0",
  "description": "Provides advanced image processing capabilities",
  "author": "Brobot Team",
  "website": "https://github.com/brobot/image-processor",
  "mainClass": "com.brobot.plugins.imageprocessor.ImageProcessorPlugin",
  "type": "AUTOMATION",
  "dependencies": [
    {
      "pluginId": "core-utils",
      "minVersion": "1.0.0",
      "required": true
    }
  ],
  "requiredPermissions": [
    "FILE_READ",
    "FILE_WRITE",
    "NETWORK_ACCESS"
  ],
  "resources": {
    "maxMemoryMB": 512,
    "maxThreads": 4
  },
  "minBrobotVersion": "2.0.0"
}
```

### 3. Plugin Context and Services

```java
// Plugin context provides access to host services
public interface PluginContext {
    // Core services
    EventBus getEventBus();
    ConfigurationService getConfiguration();
    LoggingService getLogger();
    ResourceManager getResourceManager();
    
    // Service registry for plugin-provided services
    <T> void registerService(Class<T> serviceClass, T implementation);
    <T> Optional<T> getService(Class<T> serviceClass);
    
    // Plugin-specific storage
    Path getDataDirectory();
    Path getConfigDirectory();
    
    // Inter-plugin communication
    Optional<PluginProxy> getPlugin(String pluginId);
    
    // UI integration (if applicable)
    Optional<UIIntegration> getUIIntegration();
}

// Plugin proxy for safe inter-plugin communication
public interface PluginProxy {
    String getId();
    PluginMetadata getMetadata();
    <T> Optional<T> getService(Class<T> serviceClass);
    CompletableFuture<Object> sendMessage(String action, Object payload);
}
```

### 4. Plugin Lifecycle Manager

```java
@Component
@Slf4j
public class PluginLifecycleManager {
    private final Map<String, PluginContainer> loadedPlugins = new ConcurrentHashMap<>();
    private final PluginLoader pluginLoader;
    private final PluginValidator validator;
    private final PluginSandbox sandbox;
    private final DiagnosticService diagnosticService;
    
    public CompletableFuture<PluginLoadResult> loadPlugin(Path pluginPath) {
        return CompletableFuture.supplyAsync(() -> {
            String correlationId = UUID.randomUUID().toString();
            MDC.put("correlationId", correlationId);
            
            try {
                log.info("[{}] Loading plugin from: {}", correlationId, pluginPath);
                
                // Validate plugin package
                ValidationResult validation = validator.validate(pluginPath);
                if (!validation.isValid()) {
                    return PluginLoadResult.failure(validation.getErrors());
                }
                
                // Load plugin metadata
                PluginMetadata metadata = pluginLoader.loadMetadata(pluginPath);
                
                // Check dependencies
                DependencyCheckResult depCheck = checkDependencies(metadata);
                if (!depCheck.isSatisfied()) {
                    return PluginLoadResult.failure(
                        "Unsatisfied dependencies: " + depCheck.getMissing());
                }
                
                // Create isolated classloader
                PluginClassLoader classLoader = new PluginClassLoader(
                    pluginPath, 
                    this.getClass().getClassLoader(),
                    metadata.getId()
                );
                
                // Load main class
                Class<?> mainClass = classLoader.loadClass(metadata.getMainClass());
                BrobotPlugin plugin = (BrobotPlugin) mainClass.getDeclaredConstructor()
                    .newInstance();
                
                // Create sandboxed context
                PluginContext context = createPluginContext(metadata, classLoader);
                
                // Initialize plugin in sandbox
                sandbox.execute(() -> {
                    plugin.onEnable(context);
                    return null;
                }, metadata.getRequiredPermissions());
                
                // Create container
                PluginContainer container = new PluginContainer(
                    metadata, plugin, classLoader, context);
                
                // Register with lifecycle manager
                loadedPlugins.put(metadata.getId(), container);
                
                // Register with diagnostic service
                diagnosticService.registerComponent(
                    "plugin:" + metadata.getId(), 
                    new PluginDiagnosticAdapter(container)
                );
                
                log.info("[{}] Successfully loaded plugin: {} v{}", 
                    correlationId, metadata.getName(), metadata.getVersion());
                
                return PluginLoadResult.success(metadata);
                
            } catch (Exception e) {
                log.error("[{}] Failed to load plugin", correlationId, e);
                return PluginLoadResult.failure(e.getMessage());
            } finally {
                MDC.remove("correlationId");
            }
        });
    }
    
    public CompletableFuture<Void> unloadPlugin(String pluginId) {
        return CompletableFuture.runAsync(() -> {
            PluginContainer container = loadedPlugins.remove(pluginId);
            if (container != null) {
                log.info("Unloading plugin: {}", pluginId);
                
                try {
                    // Notify plugin of shutdown
                    container.getPlugin().onDisable();
                    
                    // Cleanup resources
                    container.cleanup();
                    
                    // Unregister from diagnostic service
                    diagnosticService.unregisterComponent("plugin:" + pluginId);
                    
                } catch (Exception e) {
                    log.error("Error during plugin unload", e);
                }
            }
        });
    }
}
```

### 5. Plugin Sandbox and Security

```java
@Component
public class PluginSandbox {
    private final SecurityManager securityManager;
    private final ResourceLimiter resourceLimiter;
    
    public <T> T execute(Supplier<T> action, List<String> permissions) 
            throws SecurityException {
        // Create restricted security context
        AccessControlContext context = createRestrictedContext(permissions);
        
        // Apply resource limits
        ResourceLimitContext limitContext = resourceLimiter.createContext();
        
        return AccessController.doPrivileged(
            (PrivilegedAction<T>) () -> {
                try {
                    limitContext.enter();
                    return action.get();
                } finally {
                    limitContext.exit();
                }
            }, 
            context
        );
    }
    
    private AccessControlContext createRestrictedContext(List<String> permissions) {
        Permissions perms = new Permissions();
        
        // Add basic permissions
        perms.add(new RuntimePermission("accessDeclaredMembers"));
        perms.add(new PropertyPermission("*", "read"));
        
        // Add requested permissions
        for (String permission : permissions) {
            perms.add(mapPermission(permission));
        }
        
        ProtectionDomain domain = new ProtectionDomain(
            new CodeSource(null, (Certificate[]) null), perms);
        
        return new AccessControlContext(new ProtectionDomain[] { domain });
    }
}

// Resource limiter for plugins
@Component
public class ResourceLimiter {
    public ResourceLimitContext createContext() {
        return new ResourceLimitContext() {
            private final long maxMemory = 512 * 1024 * 1024; // 512MB
            private final int maxThreads = 4;
            private final ThreadGroup threadGroup = new ThreadGroup("plugin-threads");
            
            @Override
            public void enter() {
                // Monitor and enforce limits
            }
            
            @Override
            public void exit() {
                // Cleanup
            }
        };
    }
}
```

### 6. Plugin Discovery and Repository

```java
@Component
@Slf4j
public class PluginDiscoveryService {
    private final List<PluginRepository> repositories;
    private final PluginMetadataCache cache;
    
    public CompletableFuture<List<AvailablePlugin>> discoverPlugins() {
        List<CompletableFuture<List<AvailablePlugin>>> futures = 
            repositories.stream()
                .map(repo -> repo.listAvailablePlugins())
                .collect(Collectors.toList());
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .collect(Collectors.toList()));
    }
    
    public CompletableFuture<Path> downloadPlugin(String pluginId, String version) {
        return findPlugin(pluginId, version)
            .thenCompose(plugin -> downloadToCache(plugin));
    }
}

// Plugin repository interface
public interface PluginRepository {
    String getName();
    CompletableFuture<List<AvailablePlugin>> listAvailablePlugins();
    CompletableFuture<PluginPackage> downloadPlugin(String pluginId, String version);
}

// Local file system repository
@Component
public class LocalPluginRepository implements PluginRepository {
    private final Path pluginDirectory;
    
    @Override
    public CompletableFuture<List<AvailablePlugin>> listAvailablePlugins() {
        return CompletableFuture.supplyAsync(() -> {
            try (Stream<Path> paths = Files.walk(pluginDirectory)) {
                return paths
                    .filter(p -> p.toString().endsWith(".jar"))
                    .map(this::loadPluginInfo)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
            } catch (IOException e) {
                log.error("Failed to scan plugin directory", e);
                return Collections.emptyList();
            }
        });
    }
}
```

### 7. Plugin UI Integration

```java
// UI module provided by plugins
public interface PluginUIModule {
    String getId();
    String getTitle();
    Node createView();
    Optional<Node> createToolbar();
    Optional<Menu> createMenu();
    KeyCombination getActivationKey();
}

// UI integration service
@Component
public class PluginUIIntegrationService {
    private final ObservableList<PluginUIModule> modules = 
        FXCollections.observableArrayList();
    private final Map<String, Tab> pluginTabs = new HashMap<>();
    
    public void registerUIModule(PluginUIModule module) {
        Platform.runLater(() -> {
            modules.add(module);
            
            // Create tab for module
            Tab tab = new Tab(module.getTitle());
            tab.setContent(module.createView());
            tab.setClosable(false);
            
            pluginTabs.put(module.getId(), tab);
            
            // Notify UI of new module
            eventBus.post(new PluginUIModuleAddedEvent(module));
        });
    }
    
    public void unregisterUIModule(String moduleId) {
        Platform.runLater(() -> {
            modules.removeIf(m -> m.getId().equals(moduleId));
            Tab tab = pluginTabs.remove(moduleId);
            
            if (tab != null) {
                eventBus.post(new PluginUIModuleRemovedEvent(moduleId));
            }
        });
    }
}
```

### 8. Plugin Communication

```java
// Message-based communication between plugins
public interface PluginMessageBus {
    void send(PluginMessage message);
    void subscribe(String topic, Consumer<PluginMessage> handler);
    CompletableFuture<PluginMessage> request(PluginMessage message, Duration timeout);
}

@Value
@Builder
public class PluginMessage {
    String sourcePluginId;
    String targetPluginId;
    String topic;
    String action;
    Object payload;
    Map<String, String> headers;
    Instant timestamp;
}

// Implementation
@Component
public class PluginMessageBusImpl implements PluginMessageBus {
    private final Map<String, List<Consumer<PluginMessage>>> subscribers = 
        new ConcurrentHashMap<>();
    
    @Override
    public void send(PluginMessage message) {
        // Validate sender has permission
        validateSender(message.getSourcePluginId());
        
        // Route to target or topic subscribers
        if (message.getTargetPluginId() != null) {
            routeToPlugin(message);
        } else if (message.getTopic() != null) {
            routeToTopic(message);
        }
    }
    
    @Override
    public CompletableFuture<PluginMessage> request(
            PluginMessage message, Duration timeout) {
        CompletableFuture<PluginMessage> future = new CompletableFuture<>();
        
        String requestId = UUID.randomUUID().toString();
        message.getHeaders().put("requestId", requestId);
        message.getHeaders().put("replyTo", message.getSourcePluginId());
        
        // Register reply handler
        replyHandlers.put(requestId, future);
        
        // Send request
        send(message);
        
        // Setup timeout
        scheduler.schedule(() -> {
            future.completeExceptionally(
                new TimeoutException("Request timed out after " + timeout));
            replyHandlers.remove(requestId);
        }, timeout.toMillis(), TimeUnit.MILLISECONDS);
        
        return future;
    }
}
```

### 9. Plugin Testing Framework

```java
// Test harness for plugin development
public class PluginTestHarness {
    private final MockPluginContext context;
    private final TestEventBus eventBus;
    private final DiagnosticCapture diagnostics;
    
    public static PluginTestHarness create() {
        return new PluginTestHarness();
    }
    
    public PluginTestHarness withConfiguration(Map<String, Object> config) {
        context.setConfiguration(config);
        return this;
    }
    
    public PluginTestHarness withService(Class<?> serviceClass, Object implementation) {
        context.registerService(serviceClass, implementation);
        return this;
    }
    
    public PluginTestResult test(BrobotPlugin plugin) {
        try {
            // Initialize plugin
            plugin.onEnable(context);
            
            // Run self-test
            SelfTestResult selfTest = plugin.selfTest();
            
            // Capture diagnostics
            DiagnosticInfo diagnosticInfo = plugin.getDiagnosticInfo();
            
            // Simulate lifecycle
            Thread.sleep(100);
            
            // Disable plugin
            plugin.onDisable();
            
            return PluginTestResult.builder()
                .success(true)
                .selfTestResult(selfTest)
                .diagnostics(diagnosticInfo)
                .capturedEvents(eventBus.getCapturedEvents())
                .build();
                
        } catch (Exception e) {
            return PluginTestResult.failure(e);
        }
    }
}

// Example plugin test
public class ImageProcessorPluginTest {
    @Test
    void testPluginLifecycle() {
        // Arrange
        ImageProcessorPlugin plugin = new ImageProcessorPlugin();
        
        PluginTestHarness harness = PluginTestHarness.create()
            .withConfiguration(Map.of(
                "maxImageSize", 1024,
                "enableCaching", true
            ))
            .withService(ImageService.class, new MockImageService());
        
        // Act
        PluginTestResult result = harness.test(plugin);
        
        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getSelfTestResult().isPassed()).isTrue();
        assertThat(result.getCapturedEvents())
            .anyMatch(e -> e.getType().equals("PLUGIN_INITIALIZED"));
    }
}
```

### 10. Example Plugin Implementation

```java
@PluginInfo(
    id = "markdown-processor",
    name = "Markdown Processor",
    version = "1.0.0",
    type = PluginType.DATA
)
@Slf4j
public class MarkdownProcessorPlugin implements DataPlugin, DiagnosticCapable {
    private PluginContext context;
    private MarkdownProcessor processor;
    private final AtomicLong processedCount = new AtomicLong();
    
    @Override
    public PluginMetadata getMetadata() {
        return PluginMetadata.builder()
            .id("markdown-processor")
            .name("Markdown Processor")
            .version("1.0.0")
            .description("Processes markdown files with advanced features")
            .author("Brobot Team")
            .type(PluginType.DATA)
            .dependencies(List.of(
                new PluginDependency("core-utils", "1.0.0", true)
            ))
            .requiredPermissions(List.of("FILE_READ", "FILE_WRITE"))
            .build();
    }
    
    @Override
    public void onEnable(PluginContext context) throws PluginException {
        this.context = context;
        log.info("Enabling Markdown Processor Plugin");
        
        try {
            // Initialize processor
            processor = new MarkdownProcessor(context.getConfiguration());
            
            // Register services
            context.registerService(MarkdownProcessor.class, processor);
            
            // Subscribe to events
            context.getEventBus().subscribe(
                FileProcessingEvent.class, 
                this::handleFileProcessing
            );
            
            log.info("Markdown Processor Plugin enabled successfully");
            
        } catch (Exception e) {
            throw new PluginException("Failed to initialize plugin", e);
        }
    }
    
    @Override
    public void onDisable() {
        log.info("Disabling Markdown Processor Plugin");
        
        if (processor != null) {
            processor.shutdown();
        }
    }
    
    @Override
    public List<DataTransformer> getTransformers() {
        return List.of(
            new MarkdownToHtmlTransformer(processor),
            new MarkdownToPdfTransformer(processor)
        );
    }
    
    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        return DiagnosticInfo.builder()
            .component("MarkdownProcessorPlugin")
            .states(Map.of(
                "status", getHealth().toString(),
                "processedFiles", processedCount.get(),
                "cacheSize", processor.getCacheSize(),
                "activeTransforms", processor.getActiveTransformCount()
            ))
            .timestamp(Instant.now())
            .build();
    }
    
    @Override
    public SelfTestResult selfTest() {
        try {
            // Test markdown processing
            String testMarkdown = "# Test\n\nThis is a **test**.";
            String html = processor.toHtml(testMarkdown);
            
            if (html.contains("<h1>Test</h1>")) {
                return SelfTestResult.success(
                    "Markdown processing working correctly");
            } else {
                return SelfTestResult.failure(
                    "Markdown processing produced unexpected output");
            }
            
        } catch (Exception e) {
            return SelfTestResult.failure(
                "Self-test failed with error", e);
        }
    }
    
    private void handleFileProcessing(FileProcessingEvent event) {
        if (event.getFile().toString().endsWith(".md")) {
            processedCount.incrementAndGet();
            // Process markdown file
        }
    }
}
```

## Benefits

1. **Modularity**: Clean separation between core and extensions
2. **Flexibility**: Add new features without modifying core
3. **Stability**: Plugins can't crash the main application
4. **Diagnostics**: Full visibility into plugin behavior
5. **Testing**: Comprehensive testing framework
6. **Security**: Sandboxed execution with permissions

This plugin architecture enables the Brobot Runner to be extended with new capabilities while maintaining stability, security, and debuggability.