package io.github.jspinak.brobot.util.string;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for Base64Converter - utility for Base64 encoding.
 * Tests file and byte array encoding with various scenarios.
 */
@DisplayName("Base64Converter Tests")
public class Base64ConverterTest extends BrobotTestBase {
    
    @TempDir
    Path tempDir;
    
    private Path testFile;
    private byte[] testBytes;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        testBytes = "Hello, World!".getBytes(StandardCharsets.UTF_8);
    }
    
    @Nested
    @DisplayName("Byte Array Encoding")
    class ByteArrayEncoding {
        
        @Test
        @DisplayName("Encode simple byte array")
        public void testEncodeSimpleByteArray() {
            String encoded = Base64Converter.convert(testBytes);
            
            assertNotNull(encoded);
            assertEquals("SGVsbG8sIFdvcmxkIQ==", encoded);
            
            // Verify it can be decoded back
            byte[] decoded = Base64.getDecoder().decode(encoded);
            assertArrayEquals(testBytes, decoded);
        }
        
        @Test
        @DisplayName("Encode empty byte array")
        public void testEncodeEmptyByteArray() {
            byte[] empty = new byte[0];
            String encoded = Base64Converter.convert(empty);
            
            assertNotNull(encoded);
            assertEquals("", encoded);
        }
        
        @Test
        @DisplayName("Encode single byte")
        public void testEncodeSingleByte() {
            byte[] single = new byte[]{65}; // 'A'
            String encoded = Base64Converter.convert(single);
            
            assertNotNull(encoded);
            assertEquals("QQ==", encoded);
        }
        
        @ParameterizedTest
        @ValueSource(ints = {1, 10, 100, 1000, 10000})
        @DisplayName("Encode various sized byte arrays")
        public void testEncodeVariousSizes(int size) {
            byte[] bytes = new byte[size];
            for (int i = 0; i < size; i++) {
                bytes[i] = (byte)(i % 256);
            }
            
            String encoded = Base64Converter.convert(bytes);
            
            assertNotNull(encoded);
            assertFalse(encoded.isEmpty());
            
            // Verify decode works
            byte[] decoded = Base64.getDecoder().decode(encoded);
            assertArrayEquals(bytes, decoded);
        }
        
        @Test
        @DisplayName("Encode binary data")
        public void testEncodeBinaryData() {
            byte[] binary = new byte[]{0, 1, 2, 3, (byte)255, (byte)254, (byte)253};
            String encoded = Base64Converter.convert(binary);
            
            assertNotNull(encoded);
            
            byte[] decoded = Base64.getDecoder().decode(encoded);
            assertArrayEquals(binary, decoded);
        }
        
        @Test
        @DisplayName("Encode all byte values")
        public void testEncodeAllByteValues() {
            byte[] allBytes = new byte[256];
            for (int i = 0; i < 256; i++) {
                allBytes[i] = (byte)i;
            }
            
            String encoded = Base64Converter.convert(allBytes);
            
            assertNotNull(encoded);
            byte[] decoded = Base64.getDecoder().decode(encoded);
            assertArrayEquals(allBytes, decoded);
        }
    }
    
    @Nested
    @DisplayName("File Encoding")
    class FileEncoding {
        
        @Test
        @DisplayName("Encode text file")
        public void testEncodeTextFile() throws IOException {
            // Create test file
            testFile = tempDir.resolve("test.txt");
            Files.writeString(testFile, "Hello, World!");
            
            String encoded = Base64Converter.convert(testFile.toString());
            
            assertNotNull(encoded);
            assertEquals("SGVsbG8sIFdvcmxkIQ==", encoded);
        }
        
        @Test
        @DisplayName("Encode empty file")
        public void testEncodeEmptyFile() throws IOException {
            testFile = tempDir.resolve("empty.txt");
            Files.createFile(testFile);
            
            String encoded = Base64Converter.convert(testFile.toString());
            
            assertNotNull(encoded);
            assertEquals("", encoded);
        }
        
        @Test
        @DisplayName("Encode binary file")
        public void testEncodeBinaryFile() throws IOException {
            testFile = tempDir.resolve("binary.dat");
            byte[] binaryData = {0, 1, 2, 3, (byte)255, (byte)254};
            Files.write(testFile, binaryData);
            
            String encoded = Base64Converter.convert(testFile.toString());
            
            assertNotNull(encoded);
            byte[] decoded = Base64.getDecoder().decode(encoded);
            assertArrayEquals(binaryData, decoded);
        }
        
        @Test
        @DisplayName("Encode non-existent file returns null")
        public void testEncodeNonExistentFile() {
            String nonExistent = tempDir.resolve("does_not_exist.txt").toString();
            
            String encoded = Base64Converter.convert(nonExistent);
            
            assertNull(encoded);
        }
        
        @Test
        @DisplayName("Encode file with special characters in name")
        public void testEncodeFileWithSpecialChars() throws IOException {
            testFile = tempDir.resolve("test file with spaces.txt");
            Files.writeString(testFile, "Content");
            
            String encoded = Base64Converter.convert(testFile.toString());
            
            assertNotNull(encoded);
            assertEquals("Q29udGVudA==", encoded);
        }
        
        @Test
        @DisplayName("Encode file with Unicode content")
        public void testEncodeUnicodeContent() throws IOException {
            testFile = tempDir.resolve("unicode.txt");
            String unicodeContent = "Hello 世界 🌍";
            Files.writeString(testFile, unicodeContent, StandardCharsets.UTF_8);
            
            String encoded = Base64Converter.convert(testFile.toString());
            
            assertNotNull(encoded);
            byte[] decoded = Base64.getDecoder().decode(encoded);
            assertEquals(unicodeContent, new String(decoded, StandardCharsets.UTF_8));
        }
        
        @ParameterizedTest
        @ValueSource(ints = {1, 100, 1000, 10000})
        @DisplayName("Encode files of various sizes")
        public void testEncodeVariousSizedFiles(int sizeKB) throws IOException {
            testFile = tempDir.resolve("large_" + sizeKB + "kb.dat");
            byte[] data = new byte[sizeKB * 1024];
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte)(i % 256);
            }
            Files.write(testFile, data);
            
            String encoded = Base64Converter.convert(testFile.toString());
            
            assertNotNull(encoded);
            assertTrue(encoded.length() > sizeKB * 1024); // Base64 is larger
            
            // Verify roundtrip
            byte[] decoded = Base64.getDecoder().decode(encoded);
            assertArrayEquals(data, decoded);
        }
    }
    
    @Nested
    @DisplayName("Image File Simulation")
    class ImageFileSimulation {
        
        @Test
        @DisplayName("Encode PNG-like data")
        public void testEncodePNGLikeData() throws IOException {
            // PNG signature bytes
            byte[] pngSignature = {(byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
            testFile = tempDir.resolve("image.png");
            Files.write(testFile, pngSignature);
            
            String encoded = Base64Converter.convert(testFile.toString());
            
            assertNotNull(encoded);
            byte[] decoded = Base64.getDecoder().decode(encoded);
            assertArrayEquals(pngSignature, decoded);
        }
        
        @Test
        @DisplayName("Encode JPEG-like data")
        public void testEncodeJPEGLikeData() throws IOException {
            // JPEG signature bytes
            byte[] jpegSignature = {(byte)0xFF, (byte)0xD8, (byte)0xFF};
            testFile = tempDir.resolve("image.jpg");
            Files.write(testFile, jpegSignature);
            
            String encoded = Base64Converter.convert(testFile.toString());
            
            assertNotNull(encoded);
            byte[] decoded = Base64.getDecoder().decode(encoded);
            assertArrayEquals(jpegSignature, decoded);
        }
    }
    
    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {
        
        @Test
        @DisplayName("Handle null file path")
        public void testNullFilePath() {
            String nullPath = null;
            String encoded = Base64Converter.convert(nullPath);
            
            assertNull(encoded);
        }
        
        @Test
        @DisplayName("Handle invalid file path")
        public void testInvalidFilePath() {
            String invalidPath = "/\0/invalid/path";
            String encoded = Base64Converter.convert(invalidPath);
            
            assertNull(encoded);
        }
        
        @Test
        @DisplayName("Handle directory instead of file")
        public void testDirectoryPath() {
            String encoded = Base64Converter.convert(tempDir.toString());
            
            assertNull(encoded);
        }
        
        @Test
        @DisplayName("Handle file with no read permissions")
        public void testNoReadPermissions() throws IOException {
            // This test is platform-dependent and may not work on all systems
            testFile = tempDir.resolve("no_read.txt");
            Files.writeString(testFile, "content");
            
            // Try to make file unreadable (may not work on all platforms)
            testFile.toFile().setReadable(false);
            
            String encoded = Base64Converter.convert(testFile.toString());
            
            // Restore permissions for cleanup
            testFile.toFile().setReadable(true);
            
            // Result depends on platform permissions
            // Just verify no exception is thrown
            assertTrue(encoded == null || encoded.length() > 0);
        }
    }
    
    @Nested
    @DisplayName("Base64 Format Validation")
    class Base64FormatValidation {
        
        @ParameterizedTest
        @CsvSource({
            "A, QQ==",
            "AB, QUI=",
            "ABC, QUJD",
            "ABCD, QUJDRA==",
            "Hello, SGVsbG8="
        })
        @DisplayName("Verify Base64 output format")
        public void testBase64OutputFormat(String input, String expectedBase64) {
            byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
            String encoded = Base64Converter.convert(bytes);
            
            assertEquals(expectedBase64, encoded);
        }
        
        @Test
        @DisplayName("Base64 output has valid characters")
        public void testBase64ValidCharacters() {
            byte[] randomBytes = new byte[1000];
            for (int i = 0; i < randomBytes.length; i++) {
                randomBytes[i] = (byte)(Math.random() * 256);
            }
            
            String encoded = Base64Converter.convert(randomBytes);
            
            // Check all characters are valid Base64
            assertTrue(encoded.matches("^[A-Za-z0-9+/]*={0,2}$"));
        }
        
        @Test
        @DisplayName("Base64 padding is correct")
        public void testBase64Padding() {
            // Test various lengths that require different padding
            byte[] oneByteNoPadding = new byte[3];  // No padding needed
            byte[] oneBytePadding = new byte[2];    // One = padding
            byte[] twoBytePadding = new byte[1];    // Two == padding
            
            String noPadding = Base64Converter.convert(oneByteNoPadding);
            String onePad = Base64Converter.convert(oneBytePadding);
            String twoPad = Base64Converter.convert(twoBytePadding);
            
            assertFalse(noPadding.endsWith("="));
            assertTrue(onePad.endsWith("=") && !onePad.endsWith("=="));
            assertTrue(twoPad.endsWith("=="));
        }
    }
    
    @Nested
    @DisplayName("Performance Characteristics")
    class PerformanceCharacteristics {
        
        @Test
        @DisplayName("Encode large byte array efficiently")
        public void testLargeByteArrayPerformance() {
            byte[] largeArray = new byte[10 * 1024 * 1024]; // 10MB
            for (int i = 0; i < largeArray.length; i++) {
                largeArray[i] = (byte)(i % 256);
            }
            
            long startTime = System.currentTimeMillis();
            String encoded = Base64Converter.convert(largeArray);
            long endTime = System.currentTimeMillis();
            
            assertNotNull(encoded);
            assertTrue(endTime - startTime < 1000); // Should complete within 1 second
            
            // Verify size increase is about 33%
            double sizeRatio = (double)encoded.length() / largeArray.length;
            assertTrue(sizeRatio > 1.3 && sizeRatio < 1.4);
        }
        
        @Test
        @DisplayName("Multiple conversions are consistent")
        public void testConsistentConversions() {
            byte[] data = "Test data".getBytes(StandardCharsets.UTF_8);
            
            String encoded1 = Base64Converter.convert(data);
            String encoded2 = Base64Converter.convert(data);
            String encoded3 = Base64Converter.convert(data);
            
            assertEquals(encoded1, encoded2);
            assertEquals(encoded2, encoded3);
        }
    }
    
    @Nested
    @DisplayName("Real-World Use Cases")
    class RealWorldUseCases {
        
        @Test
        @DisplayName("Encode screenshot data")
        public void testEncodeScreenshotData() throws IOException {
            // Simulate screenshot data (simplified)
            byte[] screenshotData = new byte[1920 * 1080 * 3]; // RGB data
            for (int i = 0; i < screenshotData.length; i++) {
                screenshotData[i] = (byte)(i % 256);
            }
            
            testFile = tempDir.resolve("screenshot.raw");
            Files.write(testFile, screenshotData);
            
            String encoded = Base64Converter.convert(testFile.toString());
            
            assertNotNull(encoded);
            assertTrue(encoded.length() > screenshotData.length);
        }
        
        @Test
        @DisplayName("Encode pattern for JSON storage")
        public void testEncodePatternForJSON() {
            // Simulate a small image pattern
            byte[] patternData = new byte[50 * 50 * 4]; // RGBA
            for (int i = 0; i < patternData.length; i++) {
                patternData[i] = (byte)(Math.random() * 256);
            }
            
            String encoded = Base64Converter.convert(patternData);
            
            assertNotNull(encoded);
            // Verify it's valid for JSON embedding
            assertFalse(encoded.contains("\n"));
            assertFalse(encoded.contains("\r"));
            assertFalse(encoded.contains("\""));
        }
        
        @Test
        @DisplayName("Encode configuration file")
        public void testEncodeConfigFile() throws IOException {
            String configContent = "{\n  \"key\": \"value\",\n  \"number\": 123\n}";
            testFile = tempDir.resolve("config.json");
            Files.writeString(testFile, configContent);
            
            String encoded = Base64Converter.convert(testFile.toString());
            
            assertNotNull(encoded);
            
            // Verify decode
            byte[] decoded = Base64.getDecoder().decode(encoded);
            assertEquals(configContent, new String(decoded, StandardCharsets.UTF_8));
        }
    }
}