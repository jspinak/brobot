package io.github.jspinak.brobot.json.module;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.jspinak.brobot.runner.json.module.BrobotJsonModule;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {"java.awt.headless=false"})
public class BrobotJsonModuleTest {

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