package io.github.jspinak.brobot.app.restControllers;

import io.github.jspinak.brobot.app.web.restControllers.StateController;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ContextTests {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Autowired
    StateController stateController;

    @Test
    void contextLoads() throws Exception {
        assertThat(stateController).isNotNull();
    }

}
