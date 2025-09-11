package io.github.jspinak.brobot.util.string;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;

import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive test suite for FilenameExtractor - utility for extracting filenames. Tests various
 * path formats, extensions, and edge cases.
 */
@DisplayName("FilenameExtractor Tests")
public class FilenameExtractorTest extends BrobotTestBase {

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
    }

    @Nested
    @DisplayName("Basic Filename Extraction")
    class BasicFilenameExtraction {

        @Test
        @DisplayName("Extract filename from simple path")
        public void testSimplePath() {
            String result =
                    FilenameExtractor.getFilenameWithoutExtensionAndDirectory("/path/to/file.txt");
            assertEquals("file", result);
        }

        @Test
        @DisplayName("Extract filename without path")
        public void testFilenameOnly() {
            String result =
                    FilenameExtractor.getFilenameWithoutExtensionAndDirectory("document.pdf");
            assertEquals("document", result);
        }

        @Test
        @DisplayName("Extract filename without extension")
        public void testNoExtension() {
            String result = FilenameExtractor.getFilenameWithoutExtensionAndDirectory("README");
            assertEquals("README", result);
        }

        @Test
        @DisplayName("Extract from Windows path")
        public void testWindowsPath() {
            String result =
                    FilenameExtractor.getFilenameWithoutExtensionAndDirectory(
                            "C:\\Users\\test\\file.doc");
            assertEquals("file", result);
        }

        @Test
        @DisplayName("Extract from Unix path")
        public void testUnixPath() {
            String result =
                    FilenameExtractor.getFilenameWithoutExtensionAndDirectory(
                            "/home/user/documents/report.xlsx");
            assertEquals("report", result);
        }
    }

    @Nested
    @DisplayName("Multiple Extensions")
    class MultipleExtensions {

        @ParameterizedTest
        @CsvSource({
            "archive.tar.gz, archive.tar",
            "backup.sql.bz2, backup.sql",
            "file.min.js, file.min",
            "data.backup.old, data.backup"
        })
        @DisplayName("Handle multiple extensions")
        public void testMultipleExtensions(String input, String expected) {
            String result = FilenameExtractor.getFilenameWithoutExtensionAndDirectory(input);
            assertEquals(expected, result);
        }

        @Test
        @DisplayName("Many dots in filename")
        public void testManyDots() {
            String result =
                    FilenameExtractor.getFilenameWithoutExtensionAndDirectory(
                            "my.file.with.many.dots.txt");
            assertEquals("my.file.with.many.dots", result);
        }
    }

    @Nested
    @DisplayName("Hidden Files")
    class HiddenFiles {

        @Test
        @DisplayName("Hidden file without extension")
        public void testHiddenFileNoExtension() {
            String result = FilenameExtractor.getFilenameWithoutExtensionAndDirectory(".hidden");
            assertEquals(".hidden", result);
        }

        @Test
        @DisplayName("Hidden file with extension")
        public void testHiddenFileWithExtension() {
            String result =
                    FilenameExtractor.getFilenameWithoutExtensionAndDirectory(".gitignore.bak");
            assertEquals(".gitignore", result);
        }

        @Test
        @DisplayName("Hidden file in path")
        public void testHiddenFileInPath() {
            String result =
                    FilenameExtractor.getFilenameWithoutExtensionAndDirectory(
                            "/home/user/.config/settings.conf");
            assertEquals("settings", result);
        }

        @ParameterizedTest
        @CsvSource({
            ".bashrc, .bashrc",
            ".env, .env",
            ".DS_Store, .DS_Store",
            ".htaccess, .htaccess"
        })
        @DisplayName("Common hidden files")
        public void testCommonHiddenFiles(String input, String expected) {
            String result = FilenameExtractor.getFilenameWithoutExtensionAndDirectory(input);
            assertEquals(expected, result);
        }
    }

    @Nested
    @DisplayName("Special Cases")
    class SpecialCases {

        @Test
        @DisplayName("Single dot")
        public void testSingleDot() {
            String result = FilenameExtractor.getFilenameWithoutExtensionAndDirectory(".");
            assertEquals(".", result);
        }

        @Test
        @DisplayName("Double dot")
        public void testDoubleDot() {
            String result = FilenameExtractor.getFilenameWithoutExtensionAndDirectory("..");
            assertEquals("..", result);
        }

        @Test
        @DisplayName("Triple dot")
        public void testTripleDot() {
            String result = FilenameExtractor.getFilenameWithoutExtensionAndDirectory("...");
            assertEquals("...", result);
        }

        @Test
        @DisplayName("Trailing dot")
        public void testTrailingDot() {
            String result = FilenameExtractor.getFilenameWithoutExtensionAndDirectory("file.");
            assertEquals("file", result);
        }

        @Test
        @DisplayName("Multiple trailing dots")
        public void testMultipleTrailingDots() {
            String result = FilenameExtractor.getFilenameWithoutExtensionAndDirectory("file...");
            assertEquals("file..", result);
        }

        @Test
        @DisplayName("Trailing slash indicates directory")
        public void testTrailingSlash() {
            String result =
                    FilenameExtractor.getFilenameWithoutExtensionAndDirectory(
                            "/path/to/directory/");
            assertEquals("", result);
        }

        @Test
        @DisplayName("Trailing backslash indicates directory")
        public void testTrailingBackslash() {
            String result =
                    FilenameExtractor.getFilenameWithoutExtensionAndDirectory(
                            "C:\\path\\to\\directory\\");
            assertEquals("", result);
        }
    }

    @Nested
    @DisplayName("Path Variations")
    class PathVariations {

        @Test
        @DisplayName("Relative path")
        public void testRelativePath() {
            String result =
                    FilenameExtractor.getFilenameWithoutExtensionAndDirectory(
                            "../relative/path/file.txt");
            assertEquals("file", result);
        }

        @Test
        @DisplayName("Current directory path")
        public void testCurrentDirectoryPath() {
            String result =
                    FilenameExtractor.getFilenameWithoutExtensionAndDirectory("./current/file.txt");
            assertEquals("file", result);
        }

        @Test
        @DisplayName("Mixed path separators")
        public void testMixedSeparators() {
            String result =
                    FilenameExtractor.getFilenameWithoutExtensionAndDirectory(
                            "C:\\path/to\\file.txt");
            assertEquals("file", result);
        }

        @Test
        @DisplayName("UNC path")
        public void testUNCPath() {
            String result =
                    FilenameExtractor.getFilenameWithoutExtensionAndDirectory(
                            "\\\\server\\share\\file.doc");
            assertEquals("file", result);
        }

        @Test
        @DisplayName("Path with spaces")
        public void testPathWithSpaces() {
            String result =
                    FilenameExtractor.getFilenameWithoutExtensionAndDirectory(
                            "/path with spaces/file name.txt");
            assertEquals("file name", result);
        }
    }

    @Nested
    @DisplayName("Null and Empty Handling")
    class NullAndEmptyHandling {

        @Test
        @DisplayName("Null input returns empty string")
        public void testNullInput() {
            String result = FilenameExtractor.getFilenameWithoutExtensionAndDirectory(null);
            assertEquals("", result);
        }

        @ParameterizedTest
        @EmptySource
        @DisplayName("Empty input returns empty string")
        public void testEmptyInput(String input) {
            String result = FilenameExtractor.getFilenameWithoutExtensionAndDirectory(input);
            assertEquals("", result);
        }

        @Test
        @DisplayName("Root path only")
        public void testRootPath() {
            String result = FilenameExtractor.getFilenameWithoutExtensionAndDirectory("/");
            assertEquals("", result);
        }

        @Test
        @DisplayName("Windows drive root")
        public void testWindowsDriveRoot() {
            String result = FilenameExtractor.getFilenameWithoutExtensionAndDirectory("C:\\");
            assertEquals("", result);
        }
    }

    @Nested
    @DisplayName("Various File Extensions")
    class VariousFileExtensions {

        @ParameterizedTest
        @CsvSource({
            "file.txt, file",
            "image.png, image",
            "document.pdf, document",
            "script.js, script",
            "style.css, style",
            "data.json, data",
            "config.xml, config",
            "archive.zip, archive",
            "video.mp4, video",
            "audio.mp3, audio"
        })
        @DisplayName("Common file extensions")
        public void testCommonExtensions(String input, String expected) {
            String result = FilenameExtractor.getFilenameWithoutExtensionAndDirectory(input);
            assertEquals(expected, result);
        }

        @ParameterizedTest
        @CsvSource({
            "file.a, file",
            "file.ab, file",
            "file.abc, file",
            "file.abcd, file",
            "file.abcde, file"
        })
        @DisplayName("Various extension lengths")
        public void testExtensionLengths(String input, String expected) {
            String result = FilenameExtractor.getFilenameWithoutExtensionAndDirectory(input);
            assertEquals(expected, result);
        }

        @Test
        @DisplayName("Numeric extension")
        public void testNumericExtension() {
            String result = FilenameExtractor.getFilenameWithoutExtensionAndDirectory("backup.001");
            assertEquals("backup", result);
        }

        @Test
        @DisplayName("Mixed case extension")
        public void testMixedCaseExtension() {
            String result =
                    FilenameExtractor.getFilenameWithoutExtensionAndDirectory("Document.PDF");
            assertEquals("Document", result);
        }
    }

    @Nested
    @DisplayName("Special Characters")
    class SpecialCharacters {

        @Test
        @DisplayName("Filename with dashes")
        public void testDashes() {
            String result =
                    FilenameExtractor.getFilenameWithoutExtensionAndDirectory("my-file-name.txt");
            assertEquals("my-file-name", result);
        }

        @Test
        @DisplayName("Filename with underscores")
        public void testUnderscores() {
            String result =
                    FilenameExtractor.getFilenameWithoutExtensionAndDirectory("my_file_name.txt");
            assertEquals("my_file_name", result);
        }

        @Test
        @DisplayName("Filename with numbers")
        public void testNumbers() {
            String result =
                    FilenameExtractor.getFilenameWithoutExtensionAndDirectory("file123.txt");
            assertEquals("file123", result);
        }

        @Test
        @DisplayName("Filename with parentheses")
        public void testParentheses() {
            String result =
                    FilenameExtractor.getFilenameWithoutExtensionAndDirectory("file(1).txt");
            assertEquals("file(1)", result);
        }

        @Test
        @DisplayName("Filename with brackets")
        public void testBrackets() {
            String result =
                    FilenameExtractor.getFilenameWithoutExtensionAndDirectory("file[backup].txt");
            assertEquals("file[backup]", result);
        }

        @Test
        @DisplayName("Unicode characters")
        public void testUnicode() {
            String result = FilenameExtractor.getFilenameWithoutExtensionAndDirectory("文件名.txt");
            assertEquals("文件名", result);
        }

        @Test
        @DisplayName("Emoji in filename")
        public void testEmoji() {
            String result = FilenameExtractor.getFilenameWithoutExtensionAndDirectory("file🎉.txt");
            assertEquals("file🎉", result);
        }
    }

    @Nested
    @DisplayName("Real-World Scenarios")
    class RealWorldScenarios {

        @Test
        @DisplayName("Java source file")
        public void testJavaSourceFile() {
            String result =
                    FilenameExtractor.getFilenameWithoutExtensionAndDirectory(
                            "/src/main/java/com/example/MyClass.java");
            assertEquals("MyClass", result);
        }

        @Test
        @DisplayName("Maven POM file")
        public void testMavenPom() {
            String result =
                    FilenameExtractor.getFilenameWithoutExtensionAndDirectory("/project/pom.xml");
            assertEquals("pom", result);
        }

        @Test
        @DisplayName("Node modules path")
        public void testNodeModules() {
            String result =
                    FilenameExtractor.getFilenameWithoutExtensionAndDirectory(
                            "node_modules/@types/node/index.d.ts");
            assertEquals("index.d", result);
        }

        @Test
        @DisplayName("Python package init")
        public void testPythonInit() {
            String result =
                    FilenameExtractor.getFilenameWithoutExtensionAndDirectory(
                            "/usr/lib/python3/dist-packages/__init__.py");
            assertEquals("__init__", result);
        }

        @Test
        @DisplayName("Git internal file")
        public void testGitInternal() {
            String result =
                    FilenameExtractor.getFilenameWithoutExtensionAndDirectory(
                            ".git/objects/2a/3b4c5d6e7f");
            assertEquals("3b4c5d6e7f", result);
        }

        @Test
        @DisplayName("Temp file with random name")
        public void testTempFile() {
            String result =
                    FilenameExtractor.getFilenameWithoutExtensionAndDirectory(
                            "/tmp/tmpfile_abc123xyz.tmp");
            assertEquals("tmpfile_abc123xyz", result);
        }

        @Test
        @DisplayName("Log rotation file")
        public void testLogRotation() {
            String result =
                    FilenameExtractor.getFilenameWithoutExtensionAndDirectory(
                            "/var/log/application.log.2024-01-15");
            assertEquals("application.log", result);
        }
    }
}
