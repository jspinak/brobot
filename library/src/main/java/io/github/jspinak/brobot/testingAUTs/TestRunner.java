package io.github.jspinak.brobot.testingAUTs;

import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import io.github.jspinak.brobot.manageStates.StateMemory;
import io.github.jspinak.brobot.manageStates.StateTransitionsManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TestRunner {

    private StateTransitionsManagement stateTransitionsManagement;
    //private final RecordScreen recordScreen;
    private final RenderFileUploader renderFileUploader;
    private StateMemory stateMemory;
    private final AllStatesInProjectService allStatesInProjectService;

    public TestRunner(StateTransitionsManagement stateTransitionsManagement,
                      RecordScreen recordScreen, RenderFileUploader renderFileUploader,
                      StateMemory stateMemory, AllStatesInProjectService allStatesInProjectService) {
        this.stateTransitionsManagement = stateTransitionsManagement;
        //this.recordScreen = recordScreen;
        this.renderFileUploader = renderFileUploader;
        this.stateMemory = stateMemory;
        this.allStatesInProjectService = allStatesInProjectService;
    }

    /*
    basic implementation for testing communication with the backend application
     */
    public void runTest(String destination) {
        LocalDateTime startTime = LocalDateTime.now();
        TestRun testRun = new TestRun("test", startTime, stateMemory.getActiveStates());
        Logger logger = LoggerFactory.getLogger(TestRunner.class);
        logger.info("Test started at" + startTime);
        //recordScreen.startRecording("recording/test.mp4", 10000000);
        Long destinationId = allStatesInProjectService.getState(destination).get().getId();
        stateTransitionsManagement.openState(destinationId);
        //recordScreen.stopRecording();
        testRun.setEndTime(LocalDateTime.now());
        testRun.setRecordingFilename("test");
        testRun.setEndStates(stateMemory.getActiveStates());
       // dataSender.send(testRun, "insert post-address"); // "https://your-website.com/api/upload"
    }

}
