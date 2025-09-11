package io.github.jspinak.brobot.util.file;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive test suite for FilenameUtils - filename manipulation utility. Tests extension
 * handling, filename extraction, and edge cases.
 */
@DisplayName("FilenameUtils Tests")
public class FilenameUtilsTest extends BrobotTestBase {

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
    }

    @Nested
    @DisplayName("Add PNG Extension")
    class AddPngExtension {

        @Test
        @DisplayName("Add PNG to extensionless filename")
        public void testAddPngToExtensionless() {
            String result = FilenameUtils.addPngExtensionIfNeeded("screenshot");
            assertEquals("screenshot.png", result);
        }

        @Test
        @DisplayName("Keep existing PNG extension")
        public void testKeepExistingPng() {
            String result = FilenameUtils.addPngExtensionIfNeeded("image.png");
            assertEquals("image.png", result);
        }

        @Test
        @DisplayName("Keep other extensions unchanged")
        public void testKeepOtherExtensions() {
            String result = FilenameUtils.addPngExtensionIfNeeded("document.pdf");
            assertEquals("document.pdf", result);
        }

        @ParameterizedTest
        @CsvSource({
            "file, file.png",
            "screenshot, screenshot.png",
            "test_image, test_image.png",
            "my-file, my-file.png",
            "file123, file123.png"
        })
        @DisplayName("Add PNG to various extensionless filenames")
        public void testAddPngToVariousExtensionless(String input, String expected) {
            String result = FilenameUtils.addPngExtensionIfNeeded(input);
            assertEquals(expected, result);
        }

        @ParameterizedTest
        @CsvSource({
            "image.jpg, image.jpg",
            "document.pdf, document.pdf",
            "archive.zip, archive.zip",
            "data.csv, data.csv",
            "script.js, script.js"
        })
        @DisplayName("Preserve existing extensions")
        public void testPreserveExistingExtensions(String input, String expected) {
            String result = FilenameUtils.addPngExtensionIfNeeded(input);
            assertEquals(expected, result);
        }

        @Test
        @DisplayName("Handle empty string")
        public void testEmptyString() {
            String result = FilenameUtils.addPngExtensionIfNeeded("");
            assertEquals(".png", result);
        }

        @Test
        @DisplayName("Handle single character")
        public void testSingleCharacter() {
            String result = FilenameUtils.addPngExtensionIfNeeded("a");
            assertEquals("a.png", result);
        }
    }

    @Nested
    @DisplayName("Multiple Dots Handling")
    class MultipleDotsHandling {

        @Test
        @DisplayName("File with multiple dots keeps original")
        public void testMultipleDots() {
            String result = FilenameUtils.addPngExtensionIfNeeded("file.name.txt");
            assertEquals("file.name.txt", result); // Has dots, so no .png added
        }

        @Test
        @DisplayName("Compound extension preserved")
        public void testCompoundExtension() {
            String result = FilenameUtils.addPngExtensionIfNeeded("archive.tar.gz");
            assertEquals("archive.tar.gz", result);
        }

        @Test
        @DisplayName("Version number with dots")
        public void testVersionNumberWithDots() {
            String result = FilenameUtils.addPngExtensionIfNeeded("app.v1.2.3");
            assertEquals("app.v1.2.3", result);
        }

        @Test
        @DisplayName("IP address-like filename")
        public void testIPAddressLikeFilename() {
            String result = FilenameUtils.addPngExtensionIfNeeded("192.168.1.1");
            assertEquals("192.168.1.1", result);
        }
    }

    @Nested
    @DisplayName("Get Filename Without Extension")
    class GetFilenameWithoutExtension {

        @Test
        @DisplayName("Remove simple extension")
        public void testRemoveSimpleExtension() {
            String result = FilenameUtils.getFileNameWithoutExtension("image.png");
            assertEquals("image", result);
        }

        @Test
        @DisplayName("Handle extensionless filename")
        public void testExtensionlessFilename() {
            String result = FilenameUtils.getFileNameWithoutExtension("readme");
            assertEquals("readme", result);
        }

        @ParameterizedTest
        @CsvSource({
            "document.pdf, document",
            "image.jpg, image",
            "data.csv, data",
            "script.js, script",
            "style.css, style"
        })
        @DisplayName("Remove various extensions")
        public void testRemoveVariousExtensions(String input, String expected) {
            String result = FilenameUtils.getFileNameWithoutExtension(input);
            assertEquals(expected, result);
        }

        @Test
        @DisplayName("Handle multiple dots - removes last extension")
        public void testMultipleDotsRemoveLastExtension() {
            String result = FilenameUtils.getFileNameWithoutExtension("file.name.txt");
            assertEquals("file.name", result);
        }

        @Test
        @DisplayName("Handle compound extension")
        public void testCompoundExtensionRemoveLast() {
            String result = FilenameUtils.getFileNameWithoutExtension("archive.tar.gz");
            assertEquals("archive.tar", result);
        }

        @Test
        @DisplayName("Empty string returns empty")
        public void testEmptyStringReturnsEmpty() {
            String result = FilenameUtils.getFileNameWithoutExtension("");
            assertEquals("", result);
        }

        @Test
        @DisplayName("Single dot returns empty")
        public void testSingleDotReturnsEmpty() {
            String result = FilenameUtils.getFileNameWithoutExtension(".");
            assertEquals("", result);
        }
    }

    @Nested
    @DisplayName("Hidden Files and Special Cases")
    class HiddenFilesAndSpecialCases {

        @Test
        @DisplayName("Hidden file with extension")
        public void testHiddenFileWithExtension() {
            String addResult = FilenameUtils.addPngExtensionIfNeeded(".hidden.txt");
            assertEquals(".hidden.txt", addResult); // Has dot, no PNG added

            String removeResult = FilenameUtils.getFileNameWithoutExtension(".hidden.txt");
            assertEquals(".hidden", removeResult);
        }

        @Test
        @DisplayName("Hidden file without extension")
        public void testHiddenFileWithoutExtension() {
            String addResult = FilenameUtils.addPngExtensionIfNeeded(".gitignore");
            assertEquals(".gitignore", addResult); // Has dot, no PNG added

            String removeResult = FilenameUtils.getFileNameWithoutExtension(".gitignore");
            assertEquals("", removeResult); // Everything before last dot
        }

        @Test
        @DisplayName("File ending with dot")
        public void testFileEndingWithDot() {
            String addResult = FilenameUtils.addPngExtensionIfNeeded("file.");
            assertEquals("file.", addResult); // Has dot, no PNG added

            String removeResult = FilenameUtils.getFileNameWithoutExtension("file.");
            assertEquals("file", removeResult);
        }

        @Test
        @DisplayName("Multiple consecutive dots")
        public void testMultipleConsecutiveDots() {
            String addResult = FilenameUtils.addPngExtensionIfNeeded("file..txt");
            assertEquals("file..txt", addResult);

            String removeResult = FilenameUtils.getFileNameWithoutExtension("file..txt");
            assertEquals("file.", removeResult);
        }
    }

    @Nested
    @DisplayName("Path-like Filenames")
    class PathLikeFilenames {

        @Test
        @DisplayName("Unix path separator in filename")
        public void testUnixPathInFilename() {
            String addResult = FilenameUtils.addPngExtensionIfNeeded("folder/file");
            assertEquals("folder/file.png", addResult);

            String removeResult = FilenameUtils.getFileNameWithoutExtension("folder/file.txt");
            assertEquals("folder/file", removeResult);
        }

        @Test
        @DisplayName("Windows path separator in filename")
        public void testWindowsPathInFilename() {
            String addResult = FilenameUtils.addPngExtensionIfNeeded("folder\\file");
            assertEquals("folder\\file.png", addResult);

            String removeResult = FilenameUtils.getFileNameWithoutExtension("folder\\file.txt");
            assertEquals("folder\\file", removeResult);
        }

        @Test
        @DisplayName("Full path with extension")
        public void testFullPathWithExtension() {
            String path = "/home/user/documents/report.pdf";
            String addResult = FilenameUtils.addPngExtensionIfNeeded(path);
            assertEquals(path, addResult); // Has dot, unchanged

            String removeResult = FilenameUtils.getFileNameWithoutExtension(path);
            assertEquals("/home/user/documents/report", removeResult);
        }
    }

    @Nested
    @DisplayName("Special Characters")
    class SpecialCharacters {

        @ParameterizedTest
        @ValueSource(
                strings = {
                    "file-name",
                    "file_name",
                    "file name",
                    "file@name",
                    "file#name",
                    "file$name",
                    "file%name",
                    "file&name",
                    "file+name",
                    "file=name"
                })
        @DisplayName("Add PNG to filenames with special characters")
        public void testSpecialCharactersAddPng(String filename) {
            String result = FilenameUtils.addPngExtensionIfNeeded(filename);
            assertEquals(filename + ".png", result);
        }

        @Test
        @DisplayName("Unicode characters in filename")
        public void testUnicodeCharacters() {
            String addResult = FilenameUtils.addPngExtensionIfNeeded("文件名");
            assertEquals("文件名.png", addResult);

            String removeResult = FilenameUtils.getFileNameWithoutExtension("文件名.txt");
            assertEquals("文件名", removeResult);
        }

        @Test
        @DisplayName("Emoji in filename")
        public void testEmojiInFilename() {
            String addResult = FilenameUtils.addPngExtensionIfNeeded("screenshot_😀");
            assertEquals("screenshot_😀.png", addResult);

            String removeResult = FilenameUtils.getFileNameWithoutExtension("screenshot_😀.png");
            assertEquals("screenshot_😀", removeResult);
        }
    }

    @Nested
    @DisplayName("Case Sensitivity")
    class CaseSensitivity {

        @ParameterizedTest
        @CsvSource({
            "FILE.PNG, FILE.PNG",
            "File.Png, File.Png",
            "file.png, file.png",
            "FILE.TXT, FILE.TXT",
            "FiLe.TxT, FiLe.TxT"
        })
        @DisplayName("Preserve case in extensions")
        public void testPreserveCaseInExtensions(String input, String expected) {
            String result = FilenameUtils.addPngExtensionIfNeeded(input);
            assertEquals(expected, result);
        }

        @ParameterizedTest
        @CsvSource({
            "IMAGE.PNG, IMAGE",
            "Image.Jpg, Image",
            "DOCUMENT.PDF, DOCUMENT",
            "FiLe.ExT, FiLe"
        })
        @DisplayName("Remove extension preserving case")
        public void testRemoveExtensionPreservingCase(String input, String expected) {
            String result = FilenameUtils.getFileNameWithoutExtension(input);
            assertEquals(expected, result);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Very long filename")
        public void testVeryLongFilename() {
            String longName = "a".repeat(255); // Max filename length on many systems
            String addResult = FilenameUtils.addPngExtensionIfNeeded(longName);
            assertEquals(longName + ".png", addResult);

            String withExt = longName + ".txt";
            String removeResult = FilenameUtils.getFileNameWithoutExtension(withExt);
            assertEquals(longName, removeResult);
        }

        @Test
        @DisplayName("Filename with only dots")
        public void testOnlyDots() {
            String addResult = FilenameUtils.addPngExtensionIfNeeded("...");
            assertEquals("...", addResult); // Has dots, no PNG added

            String removeResult = FilenameUtils.getFileNameWithoutExtension("...");
            assertEquals("..", removeResult);
        }

        @Test
        @DisplayName("Numeric filename")
        public void testNumericFilename() {
            String addResult = FilenameUtils.addPngExtensionIfNeeded("12345");
            assertEquals("12345.png", addResult);

            String removeResult = FilenameUtils.getFileNameWithoutExtension("12345.678");
            assertEquals("12345", removeResult);
        }

        @Test
        @DisplayName("Extension only")
        public void testExtensionOnly() {
            String addResult = FilenameUtils.addPngExtensionIfNeeded(".png");
            assertEquals(".png", addResult); // Already has dot

            String removeResult = FilenameUtils.getFileNameWithoutExtension(".png");
            assertEquals("", removeResult);
        }
    }

    @Nested
    @DisplayName("Real-World Scenarios")
    class RealWorldScenarios {

        @Test
        @DisplayName("Screenshot naming")
        public void testScreenshotNaming() {
            String timestamp = "screenshot_2024-01-15_143022";
            String result = FilenameUtils.addPngExtensionIfNeeded(timestamp);
            assertEquals(timestamp + ".png", result);

            String withExt = timestamp + ".png";
            String nameOnly = FilenameUtils.getFileNameWithoutExtension(withExt);
            assertEquals(timestamp, nameOnly);
        }

        @Test
        @DisplayName("State image files")
        public void testStateImageFiles() {
            String[] stateImages = {
                "login_button", "submit_form", "error_dialog", "success_message"
            };

            for (String image : stateImages) {
                String withPng = FilenameUtils.addPngExtensionIfNeeded(image);
                assertTrue(withPng.endsWith(".png"));

                String removed = FilenameUtils.getFileNameWithoutExtension(withPng);
                assertEquals(image, removed);
            }
        }

        @Test
        @DisplayName("Version numbered files")
        public void testVersionNumberedFiles() {
            String versionedFile = "app_v2.1.0";
            String result = FilenameUtils.addPngExtensionIfNeeded(versionedFile);
            assertEquals("app_v2.1.0", result); // Has dots, no PNG added

            String withRealExt = "app_v2.1.0.jar";
            String nameOnly = FilenameUtils.getFileNameWithoutExtension(withRealExt);
            assertEquals("app_v2.1.0", nameOnly);
        }

        @Test
        @DisplayName("Report files")
        public void testReportFiles() {
            String[] reports = {
                "daily_report_2024-01-15",
                "test_results_final",
                "performance_metrics",
                "error_log_verbose"
            };

            for (String report : reports) {
                // Add PNG to report names for visual reports
                String visual = FilenameUtils.addPngExtensionIfNeeded(report);
                assertEquals(report + ".png", visual);

                // Remove extensions from existing reports
                String pdfReport = report + ".pdf";
                String nameOnly = FilenameUtils.getFileNameWithoutExtension(pdfReport);
                assertEquals(report, nameOnly);
            }
        }
    }

    @Nested
    @DisplayName("Performance Characteristics")
    class PerformanceCharacteristics {

        @Test
        @DisplayName("Handle many operations efficiently")
        public void testManyOperations() {
            long startTime = System.currentTimeMillis();

            for (int i = 0; i < 10000; i++) {
                String filename = "file_" + i;
                String withPng = FilenameUtils.addPngExtensionIfNeeded(filename);
                String withoutExt = FilenameUtils.getFileNameWithoutExtension(withPng);

                assertEquals(filename + ".png", withPng);
                assertEquals(filename, withoutExt);
            }

            long endTime = System.currentTimeMillis();
            assertTrue(
                    endTime - startTime < 1000, "Operations took " + (endTime - startTime) + "ms");
        }

        @Test
        @DisplayName("Consistent results")
        public void testConsistentResults() {
            String filename = "test_file";

            // Multiple calls produce same results
            String result1 = FilenameUtils.addPngExtensionIfNeeded(filename);
            String result2 = FilenameUtils.addPngExtensionIfNeeded(filename);
            String result3 = FilenameUtils.addPngExtensionIfNeeded(filename);

            assertEquals(result1, result2);
            assertEquals(result2, result3);

            String withExt = "test_file.txt";
            String remove1 = FilenameUtils.getFileNameWithoutExtension(withExt);
            String remove2 = FilenameUtils.getFileNameWithoutExtension(withExt);

            assertEquals(remove1, remove2);
        }
    }
}
