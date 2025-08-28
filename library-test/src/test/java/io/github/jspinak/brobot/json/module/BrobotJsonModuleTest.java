package io.github.jspinak.brobot.json.module;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.jspinak.brobot.runner.json.module.BrobotJsonModule;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import io.github.jspinak.brobot.test.TestEnvironmentInitializer;
import io.github.jspinak.brobot.test.mock.MockGuiAccessConfig;
import io.github.jspinak.brobot.test.mock.MockGuiAccessMonitor;
import io.github.jspinak.brobot.test.mock.MockScreenConfig;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = io.github.jspinak.brobot.BrobotTestApplication.class,
    properties = {
        "brobot.gui-access.continue-on-error=true",
        "brobot.gui-access.check-on-startup=false",
        "java.awt.headless=true",
        "spring.main.allow-bean-definition-overriding=true",
        "brobot.test.type=unit",
        "brobot.capture.physical-resolution=false",
        "brobot.mock.enabled=true"
    })
@Import({MockGuiAccessConfig.class, MockGuiAccessMonitor.class, MockScreenConfig.class,
         io.github.jspinak.brobot.test.config.TestApplicationConfiguration.class})
@ContextConfiguration(initializers = TestEnvironmentInitializer.class)
public class BrobotJsonModuleTest {
    
    @BeforeAll
    static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
    }

    @Autowired
    private BrobotJsonModule brobotJsonModule;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testBrobotJsonModuleIsLoaded() {
        // Prüfen, ob die BrobotJsonModule-Bean korrekt injiziert wurde
        assertThat(brobotJsonModule).isNotNull();
    }

    @Test
    public void testObjectMapperHasBrobotJsonModule() {
        // Prüfen, ob der ObjectMapper das BrobotJsonModule registriert hat
        assertThat(objectMapper.getRegisteredModuleIds())
                .contains("BrobotJsonModule");
    }
}