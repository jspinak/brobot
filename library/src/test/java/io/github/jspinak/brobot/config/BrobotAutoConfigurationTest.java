package io.github.jspinak.brobot.config;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for BrobotAutoConfiguration - Spring Boot auto-configuration for Brobot.
 * Verifies that beans are properly registered and configured.
 */
@DisplayName("BrobotAutoConfiguration Tests")
public class BrobotAutoConfigurationTest extends BrobotTestBase {
    
    private ApplicationContextRunner contextRunner;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(BrobotAutoConfiguration.class));
    }
    
    @Nested
    @DisplayName("Bean Registration")
    class BeanRegistration {
        
        @Test
        @DisplayName("All required beans are registered")
        public void testAllBeansRegistered() {
            contextRunner.run(context -> {
                assertThat(context).hasSingleBean(BrobotProperties.class);
                assertThat(context).hasSingleBean(FrameworkSettings.class);
                assertThat(context).hasSingleBean(ImagePathManager.class);
                assertThat(context).hasSingleBean(BrobotDPIConfiguration.class);
                assertThat(context).hasSingleBean(ExecutionEnvironment.class);
            });
        }
        
        @Test
        @DisplayName("BrobotProperties bean is properly configured")
        public void testBrobotPropertiesBean() {
            contextRunner
                .withPropertyValues(
                    "brobot.core.image-path=/test/images",
                    "brobot.core.mock=true"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(BrobotProperties.class);
                    BrobotProperties props = context.getBean(BrobotProperties.class);
                    assertThat(props.getCore().getImagePath()).isEqualTo("/test/images");
                    assertThat(props.getCore().isMock()).isTrue();
                });
        }
        
        @Test
        @DisplayName("FrameworkSettings is initialized correctly")
        public void testFrameworkSettingsInitialization() {
            contextRunner
                .withPropertyValues("brobot.core.mock=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(FrameworkSettings.class);
                    // Verify FrameworkSettings.mock is set from properties
                    assertTrue(FrameworkSettings.mock);
                });
        }
    }
    
    @Nested
    @DisplayName("Conditional Bean Creation")
    class ConditionalBeanCreation {
        
        @Test
        @DisplayName("Beans are created conditionally based on properties")
        public void testConditionalBeans() {
            contextRunner
                .withPropertyValues("brobot.diagnostics.enabled=true")
                .run(context -> {
                    // Diagnostics beans should be created when enabled
                    assertThat(context).hasBean("diagnosticsConfiguration");
                });
        }
        
        @Test
        @DisplayName("Optional beans are not created when disabled")
        public void testOptionalBeansNotCreated() {
            contextRunner
                .withPropertyValues("brobot.diagnostics.enabled=false")
                .run(context -> {
                    // Diagnostics beans should not be created when disabled
                    assertThat(context).doesNotHaveBean("diagnosticsConfiguration");
                });
        }
    }
    
    @Nested
    @DisplayName("Profile-based Configuration")
    class ProfileBasedConfiguration {
        
        @Test
        @DisplayName("Mock profile activates mock configuration")
        public void testMockProfile() {
            contextRunner
                .withPropertyValues("spring.profiles.active=mock")
                .run(context -> {
                    BrobotProperties props = context.getBean(BrobotProperties.class);
                    // Mock profile should set mock mode
                    assertTrue(props.getCore().isMock());
                });
        }
        
        @Test
        @DisplayName("Production profile uses real execution")
        public void testProductionProfile() {
            contextRunner
                .withPropertyValues("spring.profiles.active=production")
                .run(context -> {
                    BrobotProperties props = context.getBean(BrobotProperties.class);
                    // Production profile should not use mock mode by default
                    assertFalse(props.getCore().isMock());
                });
        }
    }
    
    @Nested
    @DisplayName("Custom Configuration Override")
    class CustomConfigurationOverride {
        
        @Test
        @DisplayName("Custom beans override auto-configuration")
        public void testCustomBeanOverride() {
            contextRunner
                .withUserConfiguration(CustomConfig.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(BrobotProperties.class);
                    BrobotProperties props = context.getBean(BrobotProperties.class);
                    // Custom config should override
                    assertThat(props.getCore().getImagePath()).isEqualTo("/custom/path");
                });
        }
    }
    
    @Configuration
    static class CustomConfig {
        @Bean
        public BrobotProperties customBrobotProperties() {
            BrobotProperties props = new BrobotProperties();
            props.getCore().setImagePath("/custom/path");
            return props;
        }
    }
}