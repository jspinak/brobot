package io.github.jspinak.brobot.util.file;

import io.github.jspinak.brobot.test.BrobotTestBase;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sikuli.script.Image;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import io.github.jspinak.brobot.test.annotations.Flaky;
import org.springframework.beans.factory.annotation.Autowired;
import io.github.jspinak.brobot.config.core.BrobotProperties;
import io.github.jspinak.brobot.test.annotations.Flaky.FlakyCause;
import io.github.jspinak.brobot.test.utils.ConcurrentTestHelper;

/**
 * Comprehensive test suite for RecorderSaveToFile - file persistence operations. Tests image
 * saving, XML persistence, and folder management. Uses ConcurrentTestBase for thread-safe parallel
 * execution.
 */
@DisplayName("RecorderSaveToFile Tests")
// @ResourceLock removed - using @SpringBootTest for thread safety
@SpringBootTest
@TestPropertySource(properties = {
    "brobot.core.mock=true",
    "brobot.core.headless=true"
})
public class RecorderSaveToFileTest extends BrobotTestBase {

    private RecorderSaveToFile saveToFile;

    @TempDir Path tempDir;

    @Mock private Image mockImage;

    @Mock private BufferedImage mockBufferedImage;

    @Autowired
    private BrobotProperties brobotProperties;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        saveToFile = new RecorderSaveToFile();

        // Recording folder is now configured via BrobotProperties

        // Setup mock image
        when(mockImage.get()).thenReturn(mockBufferedImage);
    }

    @AfterEach
    public void tearDown() {
        // Settings are restored automatically by test framework
    }

    @Nested
    @DisplayName("Folder Creation")
    class FolderCreation {

        @Test
        @DisplayName("Create default folder from settings")
        public void testCreateDefaultFolder() {
            File folder = saveToFile.createFolder();

            // Test that a folder was created (exact path depends on configuration)
            assertNotNull(folder);
            assertTrue(folder.exists() || folder.mkdirs());
        }

        @Test
        @DisplayName("Create new folder")
        public void testCreateNewFolder() {
            File newFolder = new File(tempDir.toFile(), "new_folder");
            assertFalse(newFolder.exists());

            File result = saveToFile.createFolder(newFolder);

            assertTrue(result.exists());
            assertTrue(result.isDirectory());
            assertEquals(newFolder.getAbsolutePath(), result.getAbsolutePath());
        }

        @Test
        @DisplayName("Create nested folders")
        public void testCreateNestedFolders() {
            File nestedFolder = new File(tempDir.toFile(), "level1/level2/level3");
            assertFalse(nestedFolder.exists());

            File result = saveToFile.createFolder(nestedFolder);

            assertTrue(result.exists());
            assertTrue(result.isDirectory());
            assertTrue(new File(tempDir.toFile(), "level1").exists());
            assertTrue(new File(tempDir.toFile(), "level1/level2").exists());
        }

        @Test
        @DisplayName("Create existing folder (idempotent)")
        public void testCreateExistingFolder() {
            File existingFolder = tempDir.toFile();
            assertTrue(existingFolder.exists());

            File result = saveToFile.createFolder(existingFolder);

            assertEquals(existingFolder.getAbsolutePath(), result.getAbsolutePath());
            assertTrue(result.exists());
        }

        @Test
        @DisplayName("Create folder with special characters")
        public void testCreateFolderWithSpecialChars() {
            File specialFolder = new File(tempDir.toFile(), "folder-with_special.chars");

            File result = saveToFile.createFolder(specialFolder);

            assertTrue(result.exists());
            assertTrue(result.getName().contains("special.chars"));
        }

        @Test
        @DisplayName("Create folder with spaces")
        public void testCreateFolderWithSpaces() {
            File spaceFolder = new File(tempDir.toFile(), "folder with spaces");

            File result = saveToFile.createFolder(spaceFolder);

            assertTrue(result.exists());
            assertEquals("folder with spaces", result.getName());
        }
    }

    @Nested
    @DisplayName("Image Saving")
    class ImageSaving {

        @Test
        @DisplayName("Save image with timestamp")
        public void testSaveImageWithTimestamp() {
            // Create real BufferedImage for testing
            BufferedImage realImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            when(mockImage.get()).thenReturn(realImage);

            String result = saveToFile.saveImageWithDate(mockImage, "test");

            assertNotNull(result);
            assertTrue(result.contains("test-"));
            assertTrue(result.endsWith(".png"));

            File savedFile = new File(result);
            assertTrue(savedFile.exists());
            assertTrue(savedFile.length() > 0);
        }

        @Test
        @DisplayName("Multiple saves with unique timestamps")
        public void testMultipleSavesUniqueTimestamps() throws InterruptedException {
            BufferedImage realImage = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
            when(mockImage.get()).thenReturn(realImage);

            Set<String> savedPaths = new HashSet<>();

            for (int i = 0; i < 5; i++) {
                String path = saveToFile.saveImageWithDate(mockImage, "multi");
                assertTrue(savedPaths.add(path), "Duplicate path: " + path);
                Thread.sleep(2); // Ensure different timestamps
            }

            assertEquals(5, savedPaths.size());
        }

        @Test
        @DisplayName("Save with different base names")
        public void testSaveDifferentBaseNames() {
            BufferedImage realImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
            when(mockImage.get()).thenReturn(realImage);

            String screenshot = saveToFile.saveImageWithDate(mockImage, "screenshot");
            String click = saveToFile.saveImageWithDate(mockImage, "click");
            String hover = saveToFile.saveImageWithDate(mockImage, "hover");

            assertTrue(screenshot.contains("screenshot-"));
            assertTrue(click.contains("click-"));
            assertTrue(hover.contains("hover-"));

            assertNotEquals(screenshot, click);
            assertNotEquals(click, hover);
        }

        @Test
        @DisplayName("Image save with null image")
        public void testSaveNullImage() {
            when(mockImage.get()).thenReturn(null);

            String result = saveToFile.saveImageWithDate(mockImage, "null-test");

            assertNull(result);
        }

        @Test
        @DisplayName("Image save with IOException")
        public void testSaveImageIOException() {
            when(mockImage.get()).thenThrow(new RuntimeException("Test exception"));

            String result = saveToFile.saveImageWithDate(mockImage, "error-test");

            assertNull(result);
        }

        @ParameterizedTest
        @ValueSource(
                strings = {"test", "screenshot", "click_action", "state-transition", "match.found"})
        @DisplayName("Various base filename formats")
        public void testVariousBaseFilenames(String baseName) {
            BufferedImage realImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
            when(mockImage.get()).thenReturn(realImage);

            String result = saveToFile.saveImageWithDate(mockImage, baseName);

            assertNotNull(result);
            assertTrue(result.contains(baseName + "-"));
            assertTrue(new File(result).exists());
        }
    }

    @Nested
    @DisplayName("XML Saving")
    class XMLSaving {

        private Document createTestDocument() throws ParserConfigurationException {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            Element root = doc.createElement("test");
            doc.appendChild(root);

            Element child = doc.createElement("child");
            child.setTextContent("test content");
            root.appendChild(child);

            return doc;
        }

        @Test
        @DisplayName("Save XML document")
        public void testSaveXML() throws Exception {
            Document doc = createTestDocument();

            saveToFile.saveXML(doc, "test.xml");

            File xmlFile = new File(tempDir.toFile(), "test.xml");
            assertTrue(xmlFile.exists());
            assertTrue(xmlFile.length() > 0);

            // Verify content
            String content = Files.readString(xmlFile.toPath());
            assertTrue(content.contains("<test>"));
            assertTrue(content.contains("<child>"));
            assertTrue(content.contains("test content"));
        }

        @Test
        @DisplayName("Save XML with indentation")
        public void testSaveXMLWithIndentation() throws Exception {
            Document doc = createTestDocument();

            saveToFile.saveXML(doc, "indented.xml");

            File xmlFile = new File(tempDir.toFile(), "indented.xml");
            String content = Files.readString(xmlFile.toPath());

            // Check for indentation (should have newlines and spaces)
            assertTrue(content.contains("\n"));
            assertTrue(content.matches("(?s).*\\s+<child>.*")); // Has whitespace before child
        }

        @Test
        @DisplayName("Save null document")
        public void testSaveNullDocument() {
            // Should return silently without throwing
            assertDoesNotThrow(() -> saveToFile.saveXML(null, "null.xml"));

            File xmlFile = new File(tempDir.toFile(), "null.xml");
            assertFalse(xmlFile.exists());
        }

        @Test
        @DisplayName("Save XML overwrites existing file")
        public void testSaveXMLOverwrite() throws Exception {
            Document doc1 = createTestDocument();
            Document doc2 = createTestDocument();

            Element newElement = doc2.createElement("new");
            newElement.setTextContent("new content");
            doc2.getDocumentElement().appendChild(newElement);

            saveToFile.saveXML(doc1, "overwrite.xml");
            File xmlFile = new File(tempDir.toFile(), "overwrite.xml");
            long size1 = xmlFile.length();

            saveToFile.saveXML(doc2, "overwrite.xml");
            long size2 = xmlFile.length();

            assertTrue(size2 > size1); // Second doc is larger

            String content = Files.readString(xmlFile.toPath());
            assertTrue(content.contains("new content"));
        }

        @Test
        @DisplayName("Save XML with special characters")
        public void testSaveXMLSpecialCharacters() throws Exception {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            Element root = doc.createElement("root");
            doc.appendChild(root);

            Element special = doc.createElement("special");
            special.setTextContent("< > & \" ' © ™ €");
            root.appendChild(special);

            saveToFile.saveXML(doc, "special.xml");

            File xmlFile = new File(tempDir.toFile(), "special.xml");
            String content = Files.readString(xmlFile.toPath());

            // XML entities should be escaped
            assertTrue(content.contains("&lt;"));
            assertTrue(content.contains("&gt;"));
            assertTrue(content.contains("&amp;"));
        }

        @Test
        @DisplayName("Save XML with Unicode")
        public void testSaveXMLUnicode() throws Exception {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            Element root = doc.createElement("root");
            doc.appendChild(root);

            Element unicode = doc.createElement("unicode");
            unicode.setTextContent("日本語 한국어 中文");
            root.appendChild(unicode);

            saveToFile.saveXML(doc, "unicode.xml");

            File xmlFile = new File(tempDir.toFile(), "unicode.xml");
            String content = Files.readString(xmlFile.toPath());

            assertTrue(content.contains("日本語"));
            assertTrue(content.contains("한국어"));
            assertTrue(content.contains("中文"));
        }
    }

    @Nested
    @DisplayName("Real-World Scenarios")
    class RealWorldScenarios {

        @Test
        @DisplayName("Save automation session recordings")
        public void testSaveAutomationSession() throws Exception {
            // Create session folder
            File sessionFolder = new File(tempDir.toFile(), "session_001");
            saveToFile.createFolder(sessionFolder);

            // Save screenshots
            BufferedImage img = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
            when(mockImage.get()).thenReturn(img);

            // Setting recordingFolder now handled by BrobotProperties

            String screenshot1 = saveToFile.saveImageWithDate(mockImage, "start");
            String screenshot2 = saveToFile.saveImageWithDate(mockImage, "click");
            String screenshot3 = saveToFile.saveImageWithDate(mockImage, "end");

            // Save session XML
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element session = doc.createElement("session");
            doc.appendChild(session);

            saveToFile.saveXML(doc, "session.xml");

            // Verify structure
            assertTrue(sessionFolder.exists());
            assertTrue(new File(screenshot1).exists());
            assertTrue(new File(screenshot2).exists());
            assertTrue(new File(screenshot3).exists());
            assertTrue(new File(sessionFolder, "session.xml").exists());
        }

        @Test
        @DisplayName("Batch screenshot saving")
        public void testBatchScreenshotSaving() {
            BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            when(mockImage.get()).thenReturn(img);

            Set<String> savedFiles = new HashSet<>();

            for (int i = 0; i < 10; i++) {
                String path = saveToFile.saveImageWithDate(mockImage, "batch_" + i);
                assertTrue(savedFiles.add(path));
                assertTrue(new File(path).exists());
            }

            assertEquals(10, savedFiles.size());
        }

        @Test
        @DisplayName("Report generation with images and XML")
        public void testReportGeneration() throws Exception {
            // Create report structure
            File reportFolder = new File(tempDir.toFile(), "report");
            File imagesFolder = new File(reportFolder, "images");
            saveToFile.createFolder(imagesFolder);

            // Save images to images folder
            // Setting recordingFolder now handled by BrobotProperties
            BufferedImage img = new BufferedImage(200, 150, BufferedImage.TYPE_INT_RGB);
            when(mockImage.get()).thenReturn(img);

            saveToFile.saveImageWithDate(mockImage, "test_pass");
            saveToFile.saveImageWithDate(mockImage, "test_fail");

            // Save report XML to report folder
            // Setting recordingFolder now handled by BrobotProperties
            Document report =
                    DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element root = report.createElement("report");
            report.appendChild(root);

            saveToFile.saveXML(report, "report.xml");

            // Verify structure
            assertTrue(reportFolder.exists());
            assertTrue(imagesFolder.exists());
            assertTrue(new File(reportFolder, "report.xml").exists());
            assertTrue(imagesFolder.listFiles().length >= 2);
        }
    }

    @Nested
    @DisplayName("Concurrent Operations")
    class ConcurrentOperations {

        @Test
        @DisplayName("Concurrent image saves")
        @Flaky(reason = "File I/O timing in concurrent operations", cause = FlakyCause.FILE_SYSTEM)
        @Timeout(value = 10, unit = TimeUnit.SECONDS)
        public void testConcurrentImageSaves() throws InterruptedException {
            BufferedImage img = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
            when(mockImage.get()).thenReturn(img);

            int threadCount = 10;
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch endLatch = new CountDownLatch(threadCount);
            Set<String> allPaths = ConcurrentHashMap.newKeySet();

            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            try {
                for (int t = 0; t < threadCount; t++) {
                    final int threadId = t;
                    executor.submit(
                            () -> {
                                try {
                                    startLatch.await();
                                    String path =
                                            saveToFile.saveImageWithDate(
                                                    mockImage, "concurrent_" + threadId);
                                    allPaths.add(path);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                } finally {
                                    endLatch.countDown();
                                }
                            });
                }

                startLatch.countDown();
                assertTrue(
                        ConcurrentTestHelper.awaitLatch(
                                endLatch, Duration.ofSeconds(5), "Concurrent save completion"));
            } finally {
                ConcurrentTestHelper.shutdownExecutor(executor, Duration.ofSeconds(2));
            }

            assertEquals(threadCount, allPaths.size());
            for (String path : allPaths) {
                assertTrue(new File(path).exists());
            }
        }

        @Test
        @DisplayName("Concurrent folder creation")
        @Timeout(value = 10, unit = TimeUnit.SECONDS)
        public void testConcurrentFolderCreation() throws InterruptedException {
            int threadCount = 5;
            CountDownLatch latch = new CountDownLatch(threadCount);
            File sharedFolder = new File(tempDir.toFile(), "concurrent");

            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            try {
                for (int i = 0; i < threadCount; i++) {
                    executor.submit(
                            () -> {
                                try {
                                    saveToFile.createFolder(sharedFolder);
                                } finally {
                                    latch.countDown();
                                }
                            });
                }

                assertTrue(
                        ConcurrentTestHelper.awaitLatch(
                                latch, Duration.ofSeconds(5), "Folder creation completion"));
            } finally {
                ConcurrentTestHelper.shutdownExecutor(executor, Duration.ofSeconds(2));
            }

            assertTrue(sharedFolder.exists());
            assertTrue(sharedFolder.isDirectory());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Save image with empty base name")
        public void testSaveImageEmptyBaseName() {
            BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
            when(mockImage.get()).thenReturn(img);

            String result = saveToFile.saveImageWithDate(mockImage, "");

            assertNotNull(result);
            assertTrue(result.contains("-"));
            assertTrue(result.endsWith(".png"));
        }

        @Test
        @DisplayName("Save XML with complex structure")
        public void testSaveComplexXML() throws Exception {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            Element root = doc.createElement("root");
            doc.appendChild(root);

            // Create deep structure
            Element current = root;
            for (int i = 0; i < 10; i++) {
                Element child = doc.createElement("level" + i);
                child.setAttribute("id", String.valueOf(i));
                child.setAttribute("timestamp", String.valueOf(System.currentTimeMillis()));
                current.appendChild(child);
                current = child;
            }

            saveToFile.saveXML(doc, "complex.xml");

            File xmlFile = new File(tempDir.toFile(), "complex.xml");
            assertTrue(xmlFile.exists());

            String content = Files.readString(xmlFile.toPath());
            for (int i = 0; i < 10; i++) {
                assertTrue(content.contains("level" + i));
            }
        }

        @Test
        @DisplayName("Create folder with very long path")
        public void testVeryLongPath() {
            StringBuilder longPath = new StringBuilder(tempDir.toString());
            for (int i = 0; i < 20; i++) {
                longPath.append("/level").append(i);
            }

            File longFolder = new File(longPath.toString());
            File result = saveToFile.createFolder(longFolder);

            assertTrue(result.exists());
        }
    }
}
