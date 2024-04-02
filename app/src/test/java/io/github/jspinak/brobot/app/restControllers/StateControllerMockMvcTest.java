package io.github.jspinak.brobot.app.restControllers;

import io.github.jspinak.brobot.app.services.PatternService;
import io.github.jspinak.brobot.app.services.StateService;
import io.github.jspinak.brobot.app.database.repositories.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The compiler complains when all repositories except for StateRepo (which is mocked with StateService here)
 * are not instantiated with @MockBean. I'm not sure why this happens, since StateService does not depend on the
 * other repositories. There are nested objects in State, but many of them have lazy loading and shouldn't be
 * needed unless explicitly requested. Every time you add a repository, you may need to add that repo here as a mock.
 * If this becomes too cumbersome, think about removing the test (but leave the class and this text as a reminder).
 */
@WebMvcTest(StateController.class)
class StateControllerMockMvcTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StateService stateService;

    @MockBean
    private PatternService patternService;

    @MockBean
    private StateImageRepo stateImageRepo;

    @MockBean
    private StateLocationRepo stateLocationRepo;

    @MockBean
    private ActionOptionsRepo actionOptionsRepo;

    @MockBean
    private StateRegionRepo stateRegionRepo;

    @MockBean
    private StateStringRepo stateStringRepo;

    @MockBean
    private ImageRepo imageRepo;

    @Test
    void getAllStates() throws Exception {
        // Mock the behavior of StateRepo
        when(stateService.getAllStates()).thenReturn(List.of());
        this.mockMvc
                .perform(get("/api/states/all"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)); // Check content type
    }

    /*
    @Test
    void getState() {
        // Mock the behavior of StateService for getState method
        when(stateService.getState("someName")).thenReturn(Optional.of(new State(/* your state object here *//*)));

        // Call the method in StateController that uses StateService
        State state = stateController.getState("someName");

        // Assertions or verifications as needed
        assertNotNull(state);
    }
    */
}