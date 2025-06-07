package io.github.jspinak.brobot.datatypes.project;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Project class.
 */
class ProjectTest {

    @Test
    void testProjectGettersAndSetters() {
        Project project = new Project();
        LocalDateTime now = LocalDateTime.now();
        List<String> states = new ArrayList<>();
        Map<String, Object> customProps = new HashMap<>();

        project.setId(1L);
        project.setName("Test Project");
        project.setDescription("A project for testing purposes.");
        project.setVersion("1.0.0-alpha");
        project.setAuthor("Test Author");
        project.setCreated(now);
        project.setUpdated(now);
        project.setStates(null); // Type in original class is List<State>
        project.setStateTransitions(null); // Type is List<StateTransitions>
        project.setAutomation(new AutomationUI());
        project.setConfiguration(new ProjectConfiguration());
        project.setOrganization("Test Org");
        project.setWebsite("https://example.com");
        project.setLicense("MIT");
        project.setCreatedDate("2025-06-07");
        project.setCustomProperties(customProps);

        assertEquals(1L, project.getId());
        assertEquals("Test Project", project.getName());
        assertEquals("A project for testing purposes.", project.getDescription());
        assertEquals("1.0.0-alpha", project.getVersion());
        assertEquals("Test Author", project.getAuthor());
        assertEquals(now, project.getCreated());
        assertEquals(now, project.getUpdated());
        assertNull(project.getStates());
        assertNull(project.getStateTransitions());
        assertNotNull(project.getAutomation());
        assertNotNull(project.getConfiguration());
        assertEquals("Test Org", project.getOrganization());
        assertEquals("https://example.com", project.getWebsite());
        assertEquals("MIT", project.getLicense());
        assertEquals("2025-06-07", project.getCreatedDate());
        assertEquals(customProps, project.getCustomProperties());
    }

    @Test
    void testReset() {
        Project project = new Project();
        project.setId(99L);
        project.setName("Initial Project Name");
        project.setDescription("Some description");
        project.setAuthor("Some Author");
        project.setCustomProperties(new HashMap<String, Object>() {{ put("key", "value"); }});

        project.reset();

        assertNull(project.getId());
        assertNull(project.getName());
        assertNull(project.getDescription());
        assertNull(project.getAuthor());
        assertNull(project.getCreated());
        assertNull(project.getUpdated());
        assertNull(project.getStates());
        assertNull(project.getStateTransitions());
        assertNull(project.getAutomation());
        assertNull(project.getConfiguration());
        assertNull(project.getOrganization());
        assertNull(project.getWebsite());
        assertNull(project.getLicense());
        assertNull(project.getCreatedDate());
        assertNotNull(project.getCustomProperties());
        assertTrue(project.getCustomProperties().isEmpty());
    }
}
