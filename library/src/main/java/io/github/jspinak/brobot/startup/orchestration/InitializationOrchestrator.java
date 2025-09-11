package io.github.jspinak.brobot.startup.orchestration;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.annotations.AnnotationProcessor;
import io.github.jspinak.brobot.config.core.BrobotProperties;
import io.github.jspinak.brobot.config.core.BrobotPropertiesInitializer;
import io.github.jspinak.brobot.config.core.EarlyImagePathInitializer;
import io.github.jspinak.brobot.config.core.FrameworkSettings;
import io.github.jspinak.brobot.config.core.FrameworkSettingsConfig;
import io.github.jspinak.brobot.config.dpi.AutoScalingConfiguration;
import io.github.jspinak.brobot.config.environment.ExecutionEnvironmentConfig;
import io.github.jspinak.brobot.config.logging.SikuliXLoggingConfig;
import io.github.jspinak.brobot.config.mock.MockConfiguration;
import io.github.jspinak.brobot.startup.state.InitialStateAutoConfiguration;
import io.github.jspinak.brobot.statemanagement.InitialStates;
import io.github.jspinak.brobot.statemanagement.SearchRegionDependencyInitializer;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import io.github.jspinak.brobot.tools.logging.ConsoleReporterInitializer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Central orchestrator for Brobot's critical initialization path.
 *
 * <p>This orchestrator provides:
 *
 * <ul>
 *   <li>Clear visibility of initialization phases and their order
 *   <li>Centralized error handling and recovery
 *   <li>Performance monitoring of initialization steps
 *   <li>Health check status for each phase
 *   <li>Conditional initialization based on configuration
 * </ul>
 *
 * <p>Initialization Phases:
 *
 * <ol>
 *   <li><b>Phase 0: Early Core</b> - Logging suppression, DPI settings
 *   <li><b>Phase 1: Core Configuration</b> - Properties, framework settings, mock mode
 *   <li><b>Phase 2: Environment Setup</b> - Execution environment, auto-scaling, image paths
 *   <li><b>Phase 3: Component Initialization</b> - State dependencies, annotations, reporters
 *   <li><b>Phase 4: State Activation</b> - Initial state discovery and activation
 * </ol>
 *
 * @since 1.3.0
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class InitializationOrchestrator {

    // Core configuration components
    @Autowired(required = false)
    private SikuliXLoggingConfig sikuliXLoggingConfig;

    @Autowired(required = false)
    private BrobotPropertiesInitializer propertiesInitializer;

    @Autowired(required = false)
    private BrobotProperties brobotProperties;

    @Autowired(required = false)
    private FrameworkSettingsConfig frameworkSettingsConfig;

    @Autowired(required = false)
    private MockConfiguration mockConfiguration;

    // Environment setup components
    @Autowired(required = false)
    private ExecutionEnvironmentConfig executionEnvironmentConfig;

    @Autowired(required = false)
    private AutoScalingConfiguration autoScalingConfiguration;

    @Autowired(required = false)
    private EarlyImagePathInitializer imagePathInitializer;

    // Capture services
    @Autowired(required = false)
    private io.github.jspinak.brobot.capture.ScreenResolutionManager screenResolutionManager;

    @Autowired(required = false)
    private io.github.jspinak.brobot.capture.UnifiedCaptureService unifiedCaptureService;

    @Autowired(required = false)
    private io.github.jspinak.brobot.capture.BrobotCaptureService brobotCaptureService;

    @Autowired(required = false)
    private io.github.jspinak.brobot.capture.BrobotScreenCapture brobotScreenCapture;

    // DPI Configuration
    @Autowired(required = false)
    private io.github.jspinak.brobot.config.dpi.DPIConfiguration dpiConfiguration;

    // Utilities
    @Autowired(required = false)
    private io.github.jspinak.brobot.util.image.core.BufferedImageUtilities bufferedImageUtilities;

    // Property verification
    @Autowired(required = false)
    private io.github.jspinak.brobot.config.core.BrobotPropertyVerifier propertyVerifier;

    // Diagnostics
    @Autowired(required = false)
    private io.github.jspinak.brobot.config.environment.HeadlessDiagnostics headlessDiagnostics;

    // Component initialization
    @Autowired(required = false)
    private SearchRegionDependencyInitializer searchRegionInitializer;

    @Autowired(required = false)
    private io.github.jspinak.brobot.initialization.StateInitializationOrchestrator
            stateInitializationOrchestrator;

    @Autowired(required = false)
    private AnnotationProcessor annotationProcessor;

    @Autowired(required = false)
    private ConsoleReporterInitializer consoleReporterInitializer;

    @Autowired(required = false)
    private EventListenerConfiguration eventListenerConfiguration;

    @Autowired(required = false)
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    private ApplicationEventMulticaster eventMulticaster;

    // State management
    @Autowired(required = false)
    private InitialStates initialStates;

    @Autowired(required = false)
    private StateMemory stateMemory;

    @Autowired(required = false)
    private InitialStateAutoConfiguration initialStateAutoConfiguration;

    // Initialization tracking
    @Getter private final Map<String, PhaseStatus> phaseStatuses = new ConcurrentHashMap<>();
    private final AtomicBoolean initializationComplete = new AtomicBoolean(false);
    private Instant startTime;

    /** Phase status tracking for health checks and debugging. */
    @Getter
    public static class PhaseStatus {
        private final String name;
        private final int order;
        private boolean completed;
        private boolean successful;
        private String errorMessage;
        private Duration duration;
        private final List<String> completedSteps = new ArrayList<>();
        private final List<String> failedSteps = new ArrayList<>();

        public PhaseStatus(String name, int order) {
            this.name = name;
            this.order = order;
            this.completed = false;
            this.successful = false;
        }

        public void markCompleted(boolean success, Duration duration) {
            this.completed = true;
            this.successful = success;
            this.duration = duration;
        }

        public void addCompletedStep(String step) {
            completedSteps.add(step);
        }

        public void addFailedStep(String step, String error) {
            failedSteps.add(step + " - " + error);
            this.successful = false;
            this.errorMessage = error;
        }
    }

    /**
     * Phase 0: Early core initialization - runs immediately after bean creation. Critical for
     * suppressing unwanted logs and setting up core framework.
     */
    @PostConstruct
    public void initializeEarlyCore() {
        startTime = Instant.now();
        PhaseStatus phase = new PhaseStatus("Early Core Initialization", 0);
        phaseStatuses.put("early-core", phase);

        log.info("╔══════════════════════════════════════════════════════════════════╗");
        log.info("║     BROBOT INITIALIZATION ORCHESTRATOR - STARTING                ║");
        log.info("╚══════════════════════════════════════════════════════════════════╝");

        try {
            // Suppress SikuliX logging first
            if (sikuliXLoggingConfig != null) {
                sikuliXLoggingConfig.disableSikuliXLogging();
                phase.addCompletedStep("SikuliX logging suppressed");
            }

            // Configure DPI scaling early
            // DPI configuration removed - now handled differently

            // Windows scaling configuration removed - now handled differently

            phase.markCompleted(true, Duration.between(startTime, Instant.now()));
            log.info("✅ Phase 0: Early Core Initialization - COMPLETED");

        } catch (Exception e) {
            phase.addFailedStep("Early core initialization", e.getMessage());
            phase.markCompleted(false, Duration.between(startTime, Instant.now()));
            log.error("❌ Phase 0: Early Core Initialization - FAILED", e);
            // Continue despite early failures - some are non-critical
        }
    }

    /** Phase 1: Core configuration - triggered when application environment is prepared. */
    @EventListener(ApplicationEnvironmentPreparedEvent.class)
    @Order(1)
    public void initializeCoreConfiguration(ApplicationEnvironmentPreparedEvent event) {
        Instant phaseStart = Instant.now();
        PhaseStatus phase = new PhaseStatus("Core Configuration", 1);
        phaseStatuses.put("core-config", phase);

        log.info("════════════════════════════════════════════════════════════════════");
        log.info("Phase 1: Core Configuration - STARTING");
        log.info("════════════════════════════════════════════════════════════════════");

        try {
            // Initialize properties from configuration files
            if (propertiesInitializer != null) {
                propertiesInitializer.onApplicationStarted();
                phase.addCompletedStep("Properties initialized from configuration");
                log.debug("Properties loaded with framework settings");
            }

            // Apply framework settings
            if (frameworkSettingsConfig != null) {
                frameworkSettingsConfig.initializeFrameworkSettings();
                phase.addCompletedStep("Framework settings applied");
            }

            // Configure mock mode if needed
            if (mockConfiguration != null && FrameworkSettings.mock) {
                // Mock configuration beans are automatically created in mock mode
                phase.addCompletedStep("Mock mode configured");
                log.info("Mock mode is ENABLED");
            }

            phase.markCompleted(true, Duration.between(phaseStart, Instant.now()));
            log.info(
                    "✅ Phase 1: Core Configuration - COMPLETED in {}ms",
                    phase.getDuration().toMillis());

        } catch (Exception e) {
            phase.addFailedStep("Core configuration", e.getMessage());
            phase.markCompleted(false, Duration.between(phaseStart, Instant.now()));
            log.error("❌ Phase 1: Core Configuration - FAILED", e);
            throw new RuntimeException("Critical failure in core configuration", e);
        }
    }

    /** Phase 2: Environment setup - triggered when application starts. */
    @EventListener(ApplicationStartedEvent.class)
    @Order(2)
    public void initializeEnvironmentSetup(ApplicationStartedEvent event) {
        Instant phaseStart = Instant.now();
        PhaseStatus phase = new PhaseStatus("Environment Setup", 2);
        phaseStatuses.put("environment", phase);

        log.info("════════════════════════════════════════════════════════════════════");
        log.info("Phase 2: Environment Setup - STARTING");
        log.info("════════════════════════════════════════════════════════════════════");

        try {
            // Configure execution environment
            if (executionEnvironmentConfig != null) {
                executionEnvironmentConfig.initializeExecutionEnvironment();
                phase.addCompletedStep("Execution environment configured");
            }

            // Configure auto-scaling
            if (autoScalingConfiguration != null) {
                autoScalingConfiguration.init();
                phase.addCompletedStep("Auto-scaling initialized");
            }

            // Initialize image paths
            if (imagePathInitializer != null) {
                imagePathInitializer.initializeImagePaths();
                phase.addCompletedStep("Image paths configured");
                log.debug("Image paths initialized");
            }

            // Initialize DPI Configuration
            if (dpiConfiguration != null) {
                try {
                    dpiConfiguration.configureDPIScalingEarly();
                    phase.addCompletedStep("DPI configuration initialized");
                    log.debug("DPI configuration completed");
                } catch (Exception e) {
                    log.warn("DPI configuration failed (non-critical): {}", e.getMessage());
                    phase.addCompletedStep("DPI configuration skipped");
                }
            }

            // Initialize Capture Services
            initializeCaptureServices(phase);

            // Image Utilities are initialized via @PostConstruct (empty method)
            // Just verify the bean is available
            if (bufferedImageUtilities != null) {
                phase.addCompletedStep("Image utilities available");
                log.debug("BufferedImage utilities ready");
            }

            phase.markCompleted(true, Duration.between(phaseStart, Instant.now()));
            log.info(
                    "✅ Phase 2: Environment Setup - COMPLETED in {}ms",
                    phase.getDuration().toMillis());

        } catch (Exception e) {
            phase.addFailedStep("Environment setup", e.getMessage());
            phase.markCompleted(false, Duration.between(phaseStart, Instant.now()));
            log.error("❌ Phase 2: Environment Setup - FAILED", e);
            // Environment setup failures are often recoverable
        }
    }

    /** Phase 3: Component initialization - triggered when context is refreshed. */
    @EventListener(ContextRefreshedEvent.class)
    @Order(3)
    public void initializeComponents(ContextRefreshedEvent event) {
        Instant phaseStart = Instant.now();
        PhaseStatus phase = new PhaseStatus("Component Initialization", 3);
        phaseStatuses.put("components", phase);

        log.info("════════════════════════════════════════════════════════════════════");
        log.info("Phase 3: Component Initialization - STARTING");
        log.info("════════════════════════════════════════════════════════════════════");

        try {
            // Initialize search region dependencies
            if (searchRegionInitializer != null) {
                // SearchRegionDependencyInitializer uses @PostConstruct
                phase.addCompletedStep("Search region dependencies initialized");
            }

            // Initialize console reporter
            if (consoleReporterInitializer != null) {
                // ConsoleReporterInitializer uses @PostConstruct
                phase.addCompletedStep("Console reporter initialized");
            }

            // Configure and verify event listeners
            if (eventListenerConfiguration != null) {
                eventListenerConfiguration.logEventConfiguration();
                phase.addCompletedStep("Event listeners configured");

                // Verify listener registration
                verifyListenerRegistration(phase);
            }

            // Run headless diagnostics if in headless environment
            if (java.awt.GraphicsEnvironment.isHeadless()) {
                if (headlessDiagnostics != null) {
                    try {
                        log.info("Running headless environment diagnostics...");
                        headlessDiagnostics.diagnoseHeadlessMode();
                        phase.addCompletedStep("Headless diagnostics completed");
                    } catch (Exception e) {
                        log.warn("Headless diagnostics failed: {}", e.getMessage());
                        phase.addCompletedStep("Headless diagnostics failed");
                    }
                }
            }

            // Verify properties configuration
            if (propertyVerifier != null) {
                try {
                    propertyVerifier.verifyProperties();
                    phase.addCompletedStep("Properties verified");
                    log.debug("Property verification completed");
                } catch (Exception e) {
                    log.warn("Property verification failed: {}", e.getMessage());
                    phase.addCompletedStep("Property verification failed");
                }
            }

            phase.markCompleted(true, Duration.between(phaseStart, Instant.now()));
            log.info(
                    "✅ Phase 3: Component Initialization - COMPLETED in {}ms",
                    phase.getDuration().toMillis());

        } catch (Exception e) {
            phase.addFailedStep("Component initialization", e.getMessage());
            phase.markCompleted(false, Duration.between(phaseStart, Instant.now()));
            log.error("❌ Phase 3: Component Initialization - FAILED", e);
        }
    }

    /** Phase 4: State activation - triggered when application is fully ready. */
    @EventListener(ApplicationReadyEvent.class)
    @Order(4)
    public void activateStates(ApplicationReadyEvent event) {
        Instant phaseStart = Instant.now();
        PhaseStatus phase = new PhaseStatus("State Activation", 4);
        phaseStatuses.put("states", phase);

        log.info("════════════════════════════════════════════════════════════════════");
        log.info("Phase 4: State Activation - STARTING");
        log.info("════════════════════════════════════════════════════════════════════");

        try {
            // Process annotations
            if (annotationProcessor != null) {
                annotationProcessor.processAnnotations();
                phase.addCompletedStep("Annotations processed");
            }

            // Initialize state dependencies and search regions
            if (stateInitializationOrchestrator != null) {
                log.info("Initializing state dependencies and search regions");
                // Manually trigger the state initialization since we're doing it explicitly here
                io.github.jspinak.brobot.annotations.StatesRegisteredEvent statesEvent =
                        new io.github.jspinak.brobot.annotations.StatesRegisteredEvent(
                                this,
                                initialStates != null
                                        ? initialStates.getRegisteredInitialStates().size()
                                        : 0,
                                0);
                stateInitializationOrchestrator.orchestrateInitialization(statesEvent);
                phase.addCompletedStep("State dependencies and search regions initialized");
            }

            // Auto-activate initial states if configured
            if (initialStateAutoConfiguration != null
                    && initialStates != null
                    && initialStates.hasRegisteredInitialStates()) {

                initialStateAutoConfiguration.autoActivateInitialStates(event);
                phase.addCompletedStep("Initial states activated");

                // Log active states
                if (stateMemory != null && !stateMemory.getActiveStates().isEmpty()) {
                    log.info("Active states: {}", stateMemory.getActiveStateNames());
                }
            }

            phase.markCompleted(true, Duration.between(phaseStart, Instant.now()));
            log.info(
                    "✅ Phase 4: State Activation - COMPLETED in {}ms",
                    phase.getDuration().toMillis());

        } catch (Exception e) {
            phase.addFailedStep("State activation", e.getMessage());
            phase.markCompleted(false, Duration.between(phaseStart, Instant.now()));
            log.error("❌ Phase 4: State Activation - FAILED", e);
        }

        // Final summary
        finalizeInitialization();

        // Generate and log detailed initialization report
        if (log.isInfoEnabled()) {
            String report = generateInitializationReport();
            log.info(report);
        }
    }

    /** Verify that event listeners are properly registered. */
    private void verifyListenerRegistration(PhaseStatus phase) {
        try {
            if (eventMulticaster == null) {
                log.debug("[LISTENER VERIFY] EventMulticaster not available");
                return;
            }

            // Check for specific Brobot event listeners
            String[] importantListeners = {
                "SearchRegionDependencyInitializer",
                "StateInitializationOrchestrator",
                "AutoStartupVerifier"
            };

            for (String listenerName : importantListeners) {
                try {
                    Object bean =
                            applicationContext.getBean(
                                    listenerName.substring(0, 1).toLowerCase()
                                            + listenerName.substring(1));
                    if (bean != null) {
                        log.debug("[LISTENER VERIFY] ✓ Found listener bean: {}", listenerName);
                    }
                } catch (Exception e) {
                    log.debug("[LISTENER VERIFY] Listener not found: {}", listenerName);
                }
            }

            phase.addCompletedStep("Event listeners verified");

        } catch (Exception e) {
            log.warn("[LISTENER VERIFY] Could not verify listeners: {}", e.getMessage());
            // This is non-critical, so we don't fail the phase
        }
    }

    /** Finalize initialization and provide summary. */
    private void finalizeInitialization() {
        initializationComplete.set(true);
        Duration totalDuration = Duration.between(startTime, Instant.now());

        log.info("╔══════════════════════════════════════════════════════════════════╗");
        log.info("║     BROBOT INITIALIZATION COMPLETE                               ║");
        log.info("╚══════════════════════════════════════════════════════════════════╝");

        // Summary statistics
        long successfulPhases =
                phaseStatuses.values().stream().filter(PhaseStatus::isSuccessful).count();

        log.info("Initialization Summary:");
        log.info("  Total Duration: {}ms", totalDuration.toMillis());
        log.info("  Phases Completed: {}/{}", successfulPhases, phaseStatuses.size());
        log.info("  Mock Mode: {}", FrameworkSettings.mock ? "ENABLED" : "DISABLED");

        // Phase details
        phaseStatuses.values().stream()
                .sorted(Comparator.comparingInt(PhaseStatus::getOrder))
                .forEach(
                        phase -> {
                            String status = phase.isSuccessful() ? "✅" : "❌";
                            log.info(
                                    "  {} Phase {}: {} - {}ms",
                                    status,
                                    phase.getOrder(),
                                    phase.getName(),
                                    phase.getDuration() != null
                                            ? phase.getDuration().toMillis()
                                            : "N/A");

                            if (!phase.isSuccessful() && phase.getErrorMessage() != null) {
                                log.warn("    Error: {}", phase.getErrorMessage());
                            }
                        });

        // Warnings for critical failures
        if (successfulPhases < phaseStatuses.size()) {
            log.warn(
                    "⚠️ Some initialization phases failed. Application may not function"
                            + " correctly.");
            log.warn("Check the logs above for details on failed phases.");
        }
    }

    /**
     * Generate a detailed initialization report. This can be called via JMX or a health endpoint.
     */
    public String generateInitializationReport() {
        StringBuilder report = new StringBuilder();
        report.append("\n╔════════════════════════════════════════════════════════════════════╗\n");
        report.append("║                    BROBOT INITIALIZATION REPORT                    ║\n");
        report.append("╚════════════════════════════════════════════════════════════════════╝\n\n");

        // Overall Status
        boolean allPhasesSuccessful =
                phaseStatuses.values().stream().allMatch(PhaseStatus::isSuccessful);
        String overallStatus = allPhasesSuccessful ? "✅ SUCCESS" : "⚠️ PARTIAL SUCCESS";
        report.append("Overall Status: ").append(overallStatus).append("\n");

        if (startTime != null) {
            Duration totalTime = Duration.between(startTime, Instant.now());
            report.append("Total Initialization Time: ")
                    .append(totalTime.toMillis())
                    .append("ms\n");
        }

        // Configuration Summary
        report.append("\n📋 Configuration Summary:\n");
        report.append("  • Mock Mode: ")
                .append(FrameworkSettings.mock ? "ENABLED" : "DISABLED")
                .append("\n");
        if (brobotProperties != null && brobotProperties.getCore() != null) {
            report.append("  • Headless: ")
                    .append(brobotProperties.getCore().isHeadless())
                    .append("\n");
        }
        report.append("  • Environment: ")
                .append(java.awt.GraphicsEnvironment.isHeadless() ? "HEADLESS" : "DISPLAY")
                .append("\n");

        // Phase Details
        report.append("\n📊 Initialization Phases:\n");
        phaseStatuses.values().stream()
                .sorted(Comparator.comparingInt(PhaseStatus::getOrder))
                .forEach(
                        phase -> {
                            String icon = phase.isSuccessful() ? "✅" : "❌";
                            report.append("\n")
                                    .append(icon)
                                    .append(" Phase ")
                                    .append(phase.getOrder())
                                    .append(": ")
                                    .append(phase.getName())
                                    .append("\n");

                            if (phase.getDuration() != null) {
                                report.append("   Duration: ")
                                        .append(phase.getDuration().toMillis())
                                        .append("ms\n");
                            }

                            // Completed steps
                            if (!phase.getCompletedSteps().isEmpty()) {
                                report.append("   Completed:\n");
                                phase.getCompletedSteps()
                                        .forEach(
                                                step ->
                                                        report.append("     • ")
                                                                .append(step)
                                                                .append("\n"));
                            }

                            // Failed steps
                            if (!phase.getFailedSteps().isEmpty()) {
                                report.append("   Failed:\n");
                                phase.getFailedSteps()
                                        .forEach(
                                                step ->
                                                        report.append("     ✗ ")
                                                                .append(step)
                                                                .append("\n"));
                            }
                        });

        // Critical Services Status
        report.append("\n🔧 Critical Services:\n");
        report.append("  • Capture Services: ")
                .append(unifiedCaptureService != null ? "✅ Available" : "❌ Not Available")
                .append("\n");
        report.append("  • State Management: ")
                .append(stateInitializationOrchestrator != null ? "✅ Available" : "❌ Not Available")
                .append("\n");
        report.append("  • Console Reporter: ")
                .append(consoleReporterInitializer != null ? "✅ Available" : "❌ Not Available")
                .append("\n");

        // Warnings and Recommendations
        if (!allPhasesSuccessful) {
            report.append("\n⚠️ Warnings:\n");
            phaseStatuses.values().stream()
                    .filter(phase -> !phase.isSuccessful())
                    .forEach(
                            phase -> {
                                report.append("  • Phase '")
                                        .append(phase.getName())
                                        .append("' failed");
                                if (phase.getErrorMessage() != null) {
                                    report.append(": ").append(phase.getErrorMessage());
                                }
                                report.append("\n");
                            });
        }

        report.append("\n════════════════════════════════════════════════════════════════════\n");
        return report.toString();
    }

    /**
     * Initialize capture services in the correct order. This replaces the individual @PostConstruct
     * methods for better control.
     */
    private void initializeCaptureServices(PhaseStatus phase) {
        log.debug("Initializing capture services stack...");

        try {
            // 1. Screen Resolution Manager (foundation)
            if (screenResolutionManager != null) {
                screenResolutionManager.initialize();
                phase.addCompletedStep("Screen resolution manager initialized");
                log.debug("Screen resolution detection ready");
            }

            // 2. Brobot Screen Capture (core capture)
            if (brobotScreenCapture != null) {
                brobotScreenCapture.init();
                phase.addCompletedStep("Screen capture service initialized");
                log.debug("Core screen capture ready");
            }

            // 3. Brobot Capture Service (provider management)
            if (brobotCaptureService != null) {
                brobotCaptureService.init();
                phase.addCompletedStep("Capture provider service initialized");
                io.github.jspinak.brobot.capture.provider.CaptureProvider provider =
                        brobotCaptureService.getActiveProvider();
                log.debug("Capture provider selected: {}", provider.getClass().getSimpleName());
            }

            // 4. Unified Capture Service (top-level API)
            if (unifiedCaptureService != null) {
                unifiedCaptureService.init();
                phase.addCompletedStep("Unified capture service initialized");
                log.debug("Capture service stack ready");
            }

            log.info("Capture services initialized successfully");

        } catch (Exception e) {
            log.error("Failed to initialize capture services", e);
            phase.addFailedStep("Capture services", e.getMessage());
            // Capture services are critical - but we'll continue to allow mock mode
            if (!FrameworkSettings.mock) {
                log.warn("Capture services failed in non-mock mode - functionality may be limited");
            }
        }
    }

    /**
     * Health check endpoint data.
     *
     * @return Map containing initialization status and phase details
     */
    public Map<String, Object> getInitializationStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("initialized", initializationComplete.get());
        status.put("startTime", startTime);
        status.put("phases", phaseStatuses);

        if (initializationComplete.get()) {
            status.put("totalDuration", Duration.between(startTime, Instant.now()).toMillis());
        }

        // Add current configuration
        Map<String, Object> config = new HashMap<>();
        config.put("mockMode", FrameworkSettings.mock);
        if (brobotProperties != null && brobotProperties.getCore() != null) {
            config.put("imagePath", brobotProperties.getCore().getImagePath());
        }
        status.put("configuration", config);

        // Add active states
        if (stateMemory != null) {
            status.put("activeStates", stateMemory.getActiveStateNames());
        }

        return status;
    }

    /**
     * Check if initialization is complete and successful.
     *
     * @return true if all critical phases completed successfully
     */
    public boolean isInitializationSuccessful() {
        if (!initializationComplete.get()) {
            return false;
        }

        // Check critical phases (core-config and components are critical)
        PhaseStatus coreConfig = phaseStatuses.get("core-config");
        PhaseStatus components = phaseStatuses.get("components");

        return coreConfig != null
                && coreConfig.isSuccessful()
                && components != null
                && components.isSuccessful();
    }

    /** Reset initialization state (mainly for testing). */
    public void resetInitialization() {
        phaseStatuses.clear();
        initializationComplete.set(false);
        startTime = null;
    }

    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }
}
