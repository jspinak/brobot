package io.github.jspinak.brobot.tools.logging;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseMoveOptions;
import io.github.jspinak.brobot.action.basic.mouse.MousePressOptions;
import io.github.jspinak.brobot.model.action.MouseButton;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.model.state.StateObjectMetadata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsoleReporterTest {

    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream outputStream;
    private PrintStream testOut;
    
    @Mock
    private Match match;
    
    @Mock
    private StateObject stateObject;
    
    @Mock
    private StateObjectMetadata stateObjectMetadata;
    
    @BeforeEach
    void setUp() {
        outputStream = new ByteArrayOutputStream();
        testOut = new PrintStream(outputStream);
        System.setOut(testOut);
    }
    
    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        // Reset output level to default
        ConsoleReporter.outputLevel = ConsoleReporter.OutputLevel.HIGH;
    }
    
    @Test
    void testMinReportingLevel() {
        // Test with HIGH output level
        ConsoleReporter.outputLevel = ConsoleReporter.OutputLevel.HIGH;
        assertTrue(ConsoleReporter.minReportingLevel(ConsoleReporter.OutputLevel.HIGH));
        assertTrue(ConsoleReporter.minReportingLevel(ConsoleReporter.OutputLevel.LOW));
        assertTrue(ConsoleReporter.minReportingLevel(ConsoleReporter.OutputLevel.NONE));
        
        // Test with LOW output level
        ConsoleReporter.outputLevel = ConsoleReporter.OutputLevel.LOW;
        assertFalse(ConsoleReporter.minReportingLevel(ConsoleReporter.OutputLevel.HIGH));
        assertTrue(ConsoleReporter.minReportingLevel(ConsoleReporter.OutputLevel.LOW));
        assertTrue(ConsoleReporter.minReportingLevel(ConsoleReporter.OutputLevel.NONE));
        
        // Test with NONE output level
        ConsoleReporter.outputLevel = ConsoleReporter.OutputLevel.NONE;
        assertFalse(ConsoleReporter.minReportingLevel(ConsoleReporter.OutputLevel.HIGH));
        assertFalse(ConsoleReporter.minReportingLevel(ConsoleReporter.OutputLevel.LOW));
        assertTrue(ConsoleReporter.minReportingLevel(ConsoleReporter.OutputLevel.NONE));
    }
    
    @Test
    void testPrint_WithStateObject() {
        // Setup
        when(stateObject.getName()).thenReturn("TestButton");
        when(match.toString()).thenReturn("[100,200,50,30]");
        
        ActionConfig clickConfig = new ClickOptions.Builder()
            .build();
        
        ConsoleReporter.outputLevel = ConsoleReporter.OutputLevel.HIGH;
        
        // Execute
        boolean result = ConsoleReporter.print(match, stateObject, clickConfig);
        
        // Verify
        assertTrue(result);
        String output = outputStream.toString();
        assertTrue(output.contains("CLICK: TestButton"));
        assertTrue(output.contains("[100,200,50,30]"));
    }
    
    @Test
    void testPrint_WithStateObjectMetadata() {
        // Setup
        when(stateObjectMetadata.getStateObjectName()).thenReturn("TestImage");
        when(match.toString()).thenReturn("[50,75,100,100]");
        
        ActionConfig findConfig = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.BEST)
            .build();
        
        ConsoleReporter.outputLevel = ConsoleReporter.OutputLevel.HIGH;
        
        // Execute
        boolean result = ConsoleReporter.print(match, stateObjectMetadata, findConfig);
        
        // Verify
        assertTrue(result);
        String output = outputStream.toString();
        assertTrue(output.contains("PatternFind: TestImage") || output.contains("FIND: TestImage"));
        assertTrue(output.contains("[50,75,100,100]"));
    }
    
    @Test
    void testPrint_LowOutputLevel() {
        // Setup
        when(stateObject.getName()).thenReturn("MenuItem");
        
        ActionConfig moveConfig = new MouseMoveOptions.Builder()
            .build();
        
        ConsoleReporter.outputLevel = ConsoleReporter.OutputLevel.LOW;
        
        // Execute
        ConsoleReporter.print(match, stateObject, moveConfig);
        
        // Verify
        String output = outputStream.toString();
        assertTrue(output.contains("Move: MenuItem") || output.contains("MOVE: MenuItem"));
        assertFalse(output.contains("match=")); // Match details not shown at LOW level
    }
    
    @Test
    void testPrint_NoneOutputLevel() {
        // Setup
        when(stateObject.getName()).thenReturn("TextField");
        
        ActionConfig typeConfig = new TypeOptions.Builder()
            .build();
        
        ConsoleReporter.outputLevel = ConsoleReporter.OutputLevel.NONE;
        
        // Execute
        ConsoleReporter.print(match, stateObject, typeConfig);
        
        // Verify
        String output = outputStream.toString();
        assertTrue(output.isEmpty()); // Nothing printed at NONE level
    }
    
    @Test
    void testPrint_WithClickAction() {
        // Setup
        when(stateObject.getName()).thenReturn("Button");
        when(match.toString()).thenReturn("[100,200,50,30]");
        
        ActionConfig clickConfig = new ClickOptions.Builder()
            .setNumberOfClicks(2)
            .setPressOptions(MousePressOptions.builder()
                .button(MouseButton.LEFT)
                .build())
            .build();
        
        ConsoleReporter.outputLevel = ConsoleReporter.OutputLevel.HIGH;
        
        // Execute
        ConsoleReporter.print(match, stateObject, clickConfig);
        
        // Verify
        String output = outputStream.toString();
        assertTrue(output.contains("Click: Button") || output.contains("CLICK: Button"));
        assertTrue(output.contains("[100,200,50,30]"));
    }
    
    @Test
    void testPrint_String() {
        // Test with HIGH level
        ConsoleReporter.outputLevel = ConsoleReporter.OutputLevel.HIGH;
        assertTrue(ConsoleReporter.print("Test message"));
        assertEquals("Test message", outputStream.toString());
        
        // Test with LOW level
        outputStream.reset();
        ConsoleReporter.outputLevel = ConsoleReporter.OutputLevel.LOW;
        assertFalse(ConsoleReporter.print("Test message"));
        assertTrue(outputStream.toString().isEmpty());
    }
    
    @Test
    void testPrint_StringWithLevel() {
        ConsoleReporter.outputLevel = ConsoleReporter.OutputLevel.LOW;
        
        // Should print at LOW level
        assertTrue(ConsoleReporter.print(ConsoleReporter.OutputLevel.LOW, "Low level message"));
        assertTrue(outputStream.toString().contains("Low level message"));
        
        // Should not print at HIGH level
        outputStream.reset();
        assertFalse(ConsoleReporter.print(ConsoleReporter.OutputLevel.HIGH, "High level message"));
        assertTrue(outputStream.toString().isEmpty());
    }
    
    @Test
    void testPrintln() {
        ConsoleReporter.outputLevel = ConsoleReporter.OutputLevel.HIGH;
        
        // Test empty println
        assertTrue(ConsoleReporter.println());
        assertEquals(System.lineSeparator(), outputStream.toString());
        
        // Test println with string
        outputStream.reset();
        assertTrue(ConsoleReporter.println("Test line"));
        assertEquals("Test line" + System.lineSeparator(), outputStream.toString());
    }
    
    @Test
    void testPrintln_WithLevel() {
        ConsoleReporter.outputLevel = ConsoleReporter.OutputLevel.LOW;
        
        // Should print at NONE level
        assertTrue(ConsoleReporter.println(ConsoleReporter.OutputLevel.NONE, "Essential message"));
        assertTrue(outputStream.toString().contains("Essential message"));
        
        // Should not print at HIGH level
        outputStream.reset();
        assertFalse(ConsoleReporter.println(ConsoleReporter.OutputLevel.HIGH, "Detailed message"));
        assertTrue(outputStream.toString().isEmpty());
    }
    
    @Test
    void testFormat() {
        ConsoleReporter.outputLevel = ConsoleReporter.OutputLevel.HIGH;
        
        assertTrue(ConsoleReporter.format("Value: %d, Name: %s", 42, "Test"));
        assertEquals("Value: 42, Name: Test", outputStream.toString());
    }
    
    @Test
    void testFormatln() {
        ConsoleReporter.outputLevel = ConsoleReporter.OutputLevel.HIGH;
        
        assertTrue(ConsoleReporter.formatln("Count: %d", 10));
        assertEquals("Count: 10" + System.lineSeparator(), outputStream.toString());
    }
    
    @Test
    void testFormat_WithLevel() {
        ConsoleReporter.outputLevel = ConsoleReporter.OutputLevel.LOW;
        
        // Should print at LOW level
        assertTrue(ConsoleReporter.format(ConsoleReporter.OutputLevel.LOW, "Low: %s", "test"));
        assertEquals("Low: test", outputStream.toString());
        
        // Should not print at HIGH level
        outputStream.reset();
        assertFalse(ConsoleReporter.format(ConsoleReporter.OutputLevel.HIGH, "High: %s", "test"));
        assertTrue(outputStream.toString().isEmpty());
    }
    
    @Test
    void testOutputLevelMap() {
        // Verify output level values
        assertEquals(0, ConsoleReporter.outputLevels.get(ConsoleReporter.OutputLevel.NONE));
        assertEquals(1, ConsoleReporter.outputLevels.get(ConsoleReporter.OutputLevel.LOW));
        assertEquals(2, ConsoleReporter.outputLevels.get(ConsoleReporter.OutputLevel.HIGH));
    }
    
    @Test
    void testMaxMockMatchesFindAll() {
        // Verify default value
        assertEquals(10, ConsoleReporter.MaxMockMatchesFindAll);
        
        // Test modification
        ConsoleReporter.MaxMockMatchesFindAll = 20;
        assertEquals(20, ConsoleReporter.MaxMockMatchesFindAll);
        
        // Reset to default
        ConsoleReporter.MaxMockMatchesFindAll = 10;
    }
}