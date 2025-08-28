package io.github.jspinak.brobot.tools.logging.ansi;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Comprehensive tests for AnsiColor ANSI escape code constants.
 * Tests color codes, formatting, terminal output, and usage patterns.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AnsiColor Tests")
public class AnsiColorTest extends BrobotTestBase {
    
    // ANSI escape sequence pattern
    private static final Pattern ANSI_PATTERN = Pattern.compile("\033\\[[0-9;]+m");
    
    @Nested
    @DisplayName("Color Code Format Tests")
    class ColorCodeFormatTests {
        
        @Test
        @DisplayName("Should have valid ANSI escape sequence format")
        void shouldHaveValidAnsiEscapeSequenceFormat() {
            // Test a sample of color codes
            assertTrue(ANSI_PATTERN.matcher(AnsiColor.RESET).matches());
            assertTrue(ANSI_PATTERN.matcher(AnsiColor.RED).matches());
            assertTrue(ANSI_PATTERN.matcher(AnsiColor.GREEN_BOLD).matches());
            assertTrue(ANSI_PATTERN.matcher(AnsiColor.BLUE_UNDERLINED).matches());
            assertTrue(ANSI_PATTERN.matcher(AnsiColor.YELLOW_BACKGROUND).matches());
            assertTrue(ANSI_PATTERN.matcher(AnsiColor.CYAN_BRIGHT).matches());
        }
        
        @Test
        @DisplayName("Should have correct RESET code")
        void shouldHaveCorrectResetCode() {
            assertEquals("\033[0m", AnsiColor.RESET);
        }
        
        @ParameterizedTest
        @MethodSource("io.github.jspinak.brobot.tools.logging.ansi.AnsiColorTest#provideColorCodes")
        @DisplayName("Should start with ESC character")
        void shouldStartWithEscCharacter(String colorName, String colorCode) {
            assertTrue(colorCode.startsWith("\033["),
                colorName + " should start with ESC[");
        }
        
        @ParameterizedTest
        @MethodSource("io.github.jspinak.brobot.tools.logging.ansi.AnsiColorTest#provideColorCodes")
        @DisplayName("Should end with 'm' character")
        void shouldEndWithMCharacter(String colorName, String colorCode) {
            assertTrue(colorCode.endsWith("m"),
                colorName + " should end with 'm'");
        }
    }
    
    @Nested
    @DisplayName("Regular Color Tests")
    class RegularColorTests {
        
        @Test
        @DisplayName("Should have correct regular color codes")
        void shouldHaveCorrectRegularColorCodes() {
            assertEquals("\033[0;30m", AnsiColor.BLACK);
            assertEquals("\033[0;31m", AnsiColor.RED);
            assertEquals("\033[0;32m", AnsiColor.GREEN);
            assertEquals("\033[0;33m", AnsiColor.YELLOW);
            assertEquals("\033[0;34m", AnsiColor.BLUE);
            assertEquals("\033[0;35m", AnsiColor.PURPLE);
            assertEquals("\033[0;36m", AnsiColor.CYAN);
            assertEquals("\033[0;37m", AnsiColor.WHITE);
        }
        
        @Test
        @DisplayName("Should have all 8 regular colors")
        void shouldHaveAllRegularColors() {
            String[] regularColors = {
                AnsiColor.BLACK, AnsiColor.RED, AnsiColor.GREEN, AnsiColor.YELLOW,
                AnsiColor.BLUE, AnsiColor.PURPLE, AnsiColor.CYAN, AnsiColor.WHITE
            };
            
            assertEquals(8, regularColors.length);
            Set<String> uniqueCodes = new HashSet<>(Arrays.asList(regularColors));
            assertEquals(8, uniqueCodes.size(), "All regular colors should be unique");
        }
    }
    
    @Nested
    @DisplayName("Bold Color Tests")
    class BoldColorTests {
        
        @Test
        @DisplayName("Should have correct bold color codes")
        void shouldHaveCorrectBoldColorCodes() {
            assertEquals("\033[1;30m", AnsiColor.BLACK_BOLD);
            assertEquals("\033[1;31m", AnsiColor.RED_BOLD);
            assertEquals("\033[1;32m", AnsiColor.GREEN_BOLD);
            assertEquals("\033[1;33m", AnsiColor.YELLOW_BOLD);
            assertEquals("\033[1;34m", AnsiColor.BLUE_BOLD);
            assertEquals("\033[1;35m", AnsiColor.PURPLE_BOLD);
            assertEquals("\033[1;36m", AnsiColor.CYAN_BOLD);
            assertEquals("\033[1;37m", AnsiColor.WHITE_BOLD);
        }
        
        @Test
        @DisplayName("Bold colors should use '1' attribute")
        void boldColorsShouldUse1Attribute() {
            assertTrue(AnsiColor.RED_BOLD.contains("[1;"));
            assertTrue(AnsiColor.GREEN_BOLD.contains("[1;"));
            assertTrue(AnsiColor.BLUE_BOLD.contains("[1;"));
        }
    }
    
    @Nested
    @DisplayName("Underlined Color Tests")
    class UnderlinedColorTests {
        
        @Test
        @DisplayName("Should have correct underlined color codes")
        void shouldHaveCorrectUnderlinedColorCodes() {
            assertEquals("\033[4;30m", AnsiColor.BLACK_UNDERLINED);
            assertEquals("\033[4;31m", AnsiColor.RED_UNDERLINED);
            assertEquals("\033[4;32m", AnsiColor.GREEN_UNDERLINED);
            assertEquals("\033[4;33m", AnsiColor.YELLOW_UNDERLINED);
            assertEquals("\033[4;34m", AnsiColor.BLUE_UNDERLINED);
            assertEquals("\033[4;35m", AnsiColor.PURPLE_UNDERLINED);
            assertEquals("\033[4;36m", AnsiColor.CYAN_UNDERLINED);
            assertEquals("\033[4;37m", AnsiColor.WHITE_UNDERLINED);
        }
        
        @Test
        @DisplayName("Underlined colors should use '4' attribute")
        void underlinedColorsShouldUse4Attribute() {
            assertTrue(AnsiColor.RED_UNDERLINED.contains("[4;"));
            assertTrue(AnsiColor.GREEN_UNDERLINED.contains("[4;"));
            assertTrue(AnsiColor.BLUE_UNDERLINED.contains("[4;"));
        }
    }
    
    @Nested
    @DisplayName("Background Color Tests")
    class BackgroundColorTests {
        
        @Test
        @DisplayName("Should have correct background color codes")
        void shouldHaveCorrectBackgroundColorCodes() {
            assertEquals("\033[40m", AnsiColor.BLACK_BACKGROUND);
            assertEquals("\033[41m", AnsiColor.RED_BACKGROUND);
            assertEquals("\033[42m", AnsiColor.GREEN_BACKGROUND);
            assertEquals("\033[43m", AnsiColor.YELLOW_BACKGROUND);
            assertEquals("\033[44m", AnsiColor.BLUE_BACKGROUND);
            assertEquals("\033[45m", AnsiColor.PURPLE_BACKGROUND);
            assertEquals("\033[46m", AnsiColor.CYAN_BACKGROUND);
            assertEquals("\033[47m", AnsiColor.WHITE_BACKGROUND);
        }
        
        @Test
        @DisplayName("Background colors should use codes 40-47")
        void backgroundColorsShouldUseCorrectCodes() {
            assertTrue(AnsiColor.BLACK_BACKGROUND.contains("[40m"));
            assertTrue(AnsiColor.RED_BACKGROUND.contains("[41m"));
            assertTrue(AnsiColor.GREEN_BACKGROUND.contains("[42m"));
            assertTrue(AnsiColor.YELLOW_BACKGROUND.contains("[43m"));
            assertTrue(AnsiColor.BLUE_BACKGROUND.contains("[44m"));
            assertTrue(AnsiColor.PURPLE_BACKGROUND.contains("[45m"));
            assertTrue(AnsiColor.CYAN_BACKGROUND.contains("[46m"));
            assertTrue(AnsiColor.WHITE_BACKGROUND.contains("[47m"));
        }
    }
    
    @Nested
    @DisplayName("Bright Color Tests")
    class BrightColorTests {
        
        @Test
        @DisplayName("Should have correct bright color codes")
        void shouldHaveCorrectBrightColorCodes() {
            assertEquals("\033[0;90m", AnsiColor.BLACK_BRIGHT);
            assertEquals("\033[0;91m", AnsiColor.RED_BRIGHT);
            assertEquals("\033[0;92m", AnsiColor.GREEN_BRIGHT);
            assertEquals("\033[0;93m", AnsiColor.YELLOW_BRIGHT);
            assertEquals("\033[0;94m", AnsiColor.BLUE_BRIGHT);
            assertEquals("\033[0;95m", AnsiColor.PURPLE_BRIGHT);
            assertEquals("\033[0;96m", AnsiColor.CYAN_BRIGHT);
            assertEquals("\033[0;97m", AnsiColor.WHITE_BRIGHT);
        }
        
        @Test
        @DisplayName("Bright colors should use codes 90-97")
        void brightColorsShouldUseCorrectCodes() {
            assertTrue(AnsiColor.BLACK_BRIGHT.contains("90m"));
            assertTrue(AnsiColor.RED_BRIGHT.contains("91m"));
            assertTrue(AnsiColor.GREEN_BRIGHT.contains("92m"));
            assertTrue(AnsiColor.YELLOW_BRIGHT.contains("93m"));
            assertTrue(AnsiColor.BLUE_BRIGHT.contains("94m"));
            assertTrue(AnsiColor.PURPLE_BRIGHT.contains("95m"));
            assertTrue(AnsiColor.CYAN_BRIGHT.contains("96m"));
            assertTrue(AnsiColor.WHITE_BRIGHT.contains("97m"));
        }
    }
    
    @Nested
    @DisplayName("Bold Bright Color Tests")
    class BoldBrightColorTests {
        
        @Test
        @DisplayName("Should have correct bold bright color codes")
        void shouldHaveCorrectBoldBrightColorCodes() {
            assertEquals("\033[1;90m", AnsiColor.BLACK_BOLD_BRIGHT);
            assertEquals("\033[1;91m", AnsiColor.RED_BOLD_BRIGHT);
            assertEquals("\033[1;92m", AnsiColor.GREEN_BOLD_BRIGHT);
            assertEquals("\033[1;93m", AnsiColor.YELLOW_BOLD_BRIGHT);
            assertEquals("\033[1;94m", AnsiColor.BLUE_BOLD_BRIGHT);
            assertEquals("\033[1;95m", AnsiColor.PURPLE_BOLD_BRIGHT);
            assertEquals("\033[1;96m", AnsiColor.CYAN_BOLD_BRIGHT);
            assertEquals("\033[1;97m", AnsiColor.WHITE_BOLD_BRIGHT);
        }
        
        @Test
        @DisplayName("Bold bright colors should combine attributes")
        void boldBrightColorsShouldCombineAttributes() {
            // Should have both bold (1) and bright (90-97) attributes
            assertTrue(AnsiColor.RED_BOLD_BRIGHT.contains("[1;"));
            assertTrue(AnsiColor.RED_BOLD_BRIGHT.contains("91m"));
        }
    }
    
    @Nested
    @DisplayName("Bright Background Color Tests")
    class BrightBackgroundColorTests {
        
        @Test
        @DisplayName("Should have correct bright background color codes")
        void shouldHaveCorrectBrightBackgroundColorCodes() {
            assertEquals("\033[0;100m", AnsiColor.BLACK_BACKGROUND_BRIGHT);
            assertEquals("\033[0;101m", AnsiColor.RED_BACKGROUND_BRIGHT);
            assertEquals("\033[0;102m", AnsiColor.GREEN_BACKGROUND_BRIGHT);
            assertEquals("\033[0;103m", AnsiColor.YELLOW_BACKGROUND_BRIGHT);
            assertEquals("\033[0;104m", AnsiColor.BLUE_BACKGROUND_BRIGHT);
            assertEquals("\033[0;105m", AnsiColor.PURPLE_BACKGROUND_BRIGHT);
            assertEquals("\033[0;106m", AnsiColor.CYAN_BACKGROUND_BRIGHT);
            assertEquals("\033[0;107m", AnsiColor.WHITE_BACKGROUND_BRIGHT);
        }
        
        @Test
        @DisplayName("Bright backgrounds should use codes 100-107")
        void brightBackgroundsShouldUseCorrectCodes() {
            assertTrue(AnsiColor.BLACK_BACKGROUND_BRIGHT.contains("100m"));
            assertTrue(AnsiColor.RED_BACKGROUND_BRIGHT.contains("101m"));
            assertTrue(AnsiColor.GREEN_BACKGROUND_BRIGHT.contains("102m"));
            assertTrue(AnsiColor.YELLOW_BACKGROUND_BRIGHT.contains("103m"));
            assertTrue(AnsiColor.BLUE_BACKGROUND_BRIGHT.contains("104m"));
            assertTrue(AnsiColor.PURPLE_BACKGROUND_BRIGHT.contains("105m"));
            assertTrue(AnsiColor.CYAN_BACKGROUND_BRIGHT.contains("106m"));
            assertTrue(AnsiColor.WHITE_BACKGROUND_BRIGHT.contains("107m"));
        }
    }
    
    @Nested
    @DisplayName("Usage Pattern Tests")
    class UsagePatternTests {
        
        private PrintStream originalOut;
        private ByteArrayOutputStream outputStream;
        
        @BeforeEach
        void setupOutputStream() {
            originalOut = System.out;
            outputStream = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outputStream));
        }
        
        @AfterEach
        void restoreOutputStream() {
            System.setOut(originalOut);
        }
        
        @Test
        @DisplayName("Should format success message correctly")
        void shouldFormatSuccessMessage() {
            String message = AnsiColor.GREEN + "✓ Test passed" + AnsiColor.RESET;
            System.out.print(message);
            
            String output = outputStream.toString();
            assertTrue(output.contains("\033[0;32m"));
            assertTrue(output.contains("✓ Test passed"));
            assertTrue(output.contains("\033[0m"));
        }
        
        @Test
        @DisplayName("Should format error message correctly")
        void shouldFormatErrorMessage() {
            String message = AnsiColor.RED_BOLD + "✗ Error: " + AnsiColor.RESET + "Something went wrong";
            System.out.print(message);
            
            String output = outputStream.toString();
            assertTrue(output.contains("\033[1;31m"));
            assertTrue(output.contains("✗ Error: "));
            assertTrue(output.contains("\033[0m"));
            assertTrue(output.contains("Something went wrong"));
        }
        
        @Test
        @DisplayName("Should format warning with background")
        void shouldFormatWarningWithBackground() {
            String message = AnsiColor.YELLOW_BACKGROUND + " WARNING " + AnsiColor.RESET;
            System.out.print(message);
            
            String output = outputStream.toString();
            assertTrue(output.contains("\033[43m"));
            assertTrue(output.contains(" WARNING "));
            assertTrue(output.contains("\033[0m"));
        }
        
        @Test
        @DisplayName("Should combine foreground and background colors")
        void shouldCombineForegroundAndBackground() {
            String message = AnsiColor.WHITE_BOLD + AnsiColor.RED_BACKGROUND + 
                           " CRITICAL " + AnsiColor.RESET;
            System.out.print(message);
            
            String output = outputStream.toString();
            assertTrue(output.contains("\033[1;37m")); // White bold
            assertTrue(output.contains("\033[41m"));    // Red background
            assertTrue(output.contains(" CRITICAL "));
            assertTrue(output.contains("\033[0m"));
        }
    }
    
    @Nested
    @DisplayName("Field Property Tests")
    class FieldPropertyTests {
        
        @Test
        @DisplayName("All color fields should be public static final")
        void allColorFieldsShouldBePublicStaticFinal() {
            Field[] fields = AnsiColor.class.getDeclaredFields();
            
            for (Field field : fields) {
                if (field.getType() == String.class) {
                    int modifiers = field.getModifiers();
                    assertTrue(Modifier.isPublic(modifiers),
                        field.getName() + " should be public");
                    assertTrue(Modifier.isStatic(modifiers),
                        field.getName() + " should be static");
                    assertTrue(Modifier.isFinal(modifiers),
                        field.getName() + " should be final");
                }
            }
        }
        
        @Test
        @DisplayName("Should have expected number of color constants")
        void shouldHaveExpectedNumberOfColorConstants() {
            Field[] fields = AnsiColor.class.getDeclaredFields();
            long colorFieldCount = Arrays.stream(fields)
                .filter(f -> f.getType() == String.class)
                .filter(f -> Modifier.isPublic(f.getModifiers()))
                .filter(f -> Modifier.isStatic(f.getModifiers()))
                .filter(f -> Modifier.isFinal(f.getModifiers()))
                .count();
            
            // 1 RESET + 8 colors × 7 variants (regular, bold, underlined, 
            // background, bright, bold bright, bright background) = 57
            assertEquals(57, colorFieldCount, "Should have 57 color constants");
        }
        
        @Test
        @DisplayName("All color constants should be non-null")
        void allColorConstantsShouldBeNonNull() throws IllegalAccessException {
            Field[] fields = AnsiColor.class.getDeclaredFields();
            
            for (Field field : fields) {
                if (field.getType() == String.class && 
                    Modifier.isStatic(field.getModifiers())) {
                    Object value = field.get(null);
                    assertNotNull(value, field.getName() + " should not be null");
                }
            }
        }
        
        @Test
        @DisplayName("All color constants should be unique")
        void allColorConstantsShouldBeUnique() throws IllegalAccessException {
            Field[] fields = AnsiColor.class.getDeclaredFields();
            Set<String> uniqueValues = new HashSet<>();
            List<String> duplicates = new ArrayList<>();
            
            for (Field field : fields) {
                if (field.getType() == String.class && 
                    Modifier.isStatic(field.getModifiers())) {
                    String value = (String) field.get(null);
                    if (!uniqueValues.add(value)) {
                        duplicates.add(field.getName() + "=" + value);
                    }
                }
            }
            
            assertTrue(duplicates.isEmpty(), 
                "Found duplicate color codes: " + duplicates);
        }
    }
    
    @Nested
    @DisplayName("Naming Convention Tests")
    class NamingConventionTests {
        
        @Test
        @DisplayName("Color names should follow naming convention")
        void colorNamesShouldFollowNamingConvention() {
            // Regular colors: COLOR
            assertNotNull(AnsiColor.RED);
            assertNotNull(AnsiColor.GREEN);
            assertNotNull(AnsiColor.BLUE);
            
            // Bold colors: COLOR_BOLD
            assertNotNull(AnsiColor.RED_BOLD);
            assertNotNull(AnsiColor.GREEN_BOLD);
            assertNotNull(AnsiColor.BLUE_BOLD);
            
            // Underlined colors: COLOR_UNDERLINED
            assertNotNull(AnsiColor.RED_UNDERLINED);
            assertNotNull(AnsiColor.GREEN_UNDERLINED);
            assertNotNull(AnsiColor.BLUE_UNDERLINED);
            
            // Background colors: COLOR_BACKGROUND
            assertNotNull(AnsiColor.RED_BACKGROUND);
            assertNotNull(AnsiColor.GREEN_BACKGROUND);
            assertNotNull(AnsiColor.BLUE_BACKGROUND);
            
            // Bright colors: COLOR_BRIGHT
            assertNotNull(AnsiColor.RED_BRIGHT);
            assertNotNull(AnsiColor.GREEN_BRIGHT);
            assertNotNull(AnsiColor.BLUE_BRIGHT);
            
            // Bold bright colors: COLOR_BOLD_BRIGHT
            assertNotNull(AnsiColor.RED_BOLD_BRIGHT);
            assertNotNull(AnsiColor.GREEN_BOLD_BRIGHT);
            assertNotNull(AnsiColor.BLUE_BOLD_BRIGHT);
            
            // Bright background colors: COLOR_BACKGROUND_BRIGHT
            assertNotNull(AnsiColor.RED_BACKGROUND_BRIGHT);
            assertNotNull(AnsiColor.GREEN_BACKGROUND_BRIGHT);
            assertNotNull(AnsiColor.BLUE_BACKGROUND_BRIGHT);
        }
        
        @Test
        @DisplayName("All color names should be uppercase")
        void allColorNamesShouldBeUppercase() {
            Field[] fields = AnsiColor.class.getDeclaredFields();
            
            for (Field field : fields) {
                if (field.getType() == String.class) {
                    String name = field.getName();
                    assertEquals(name.toUpperCase(), name,
                        "Field " + name + " should be all uppercase");
                }
            }
        }
    }
    
    @Nested
    @DisplayName("Completeness Tests")
    class CompletenessTests {
        
        @Test
        @DisplayName("Should have all color variants for each base color")
        void shouldHaveAllColorVariantsForEachBaseColor() {
            String[] baseColors = {"BLACK", "RED", "GREEN", "YELLOW", 
                                  "BLUE", "PURPLE", "CYAN", "WHITE"};
            String[] variants = {"", "_BOLD", "_UNDERLINED", "_BACKGROUND", 
                                "_BRIGHT", "_BOLD_BRIGHT", "_BACKGROUND_BRIGHT"};
            
            for (String baseColor : baseColors) {
                for (String variant : variants) {
                    String fieldName = baseColor + variant;
                    try {
                        Field field = AnsiColor.class.getField(fieldName);
                        assertNotNull(field, "Should have field: " + fieldName);
                    } catch (NoSuchFieldException e) {
                        fail("Missing color constant: " + fieldName);
                    }
                }
            }
        }
    }
    
    // Helper method to provide color codes for parameterized tests
    private static Stream<Arguments> provideColorCodes() {
        return Stream.of(
            Arguments.of("RESET", AnsiColor.RESET),
            Arguments.of("RED", AnsiColor.RED),
            Arguments.of("GREEN_BOLD", AnsiColor.GREEN_BOLD),
            Arguments.of("BLUE_UNDERLINED", AnsiColor.BLUE_UNDERLINED),
            Arguments.of("YELLOW_BACKGROUND", AnsiColor.YELLOW_BACKGROUND),
            Arguments.of("CYAN_BRIGHT", AnsiColor.CYAN_BRIGHT),
            Arguments.of("PURPLE_BOLD_BRIGHT", AnsiColor.PURPLE_BOLD_BRIGHT),
            Arguments.of("WHITE_BACKGROUND_BRIGHT", AnsiColor.WHITE_BACKGROUND_BRIGHT)
        );
    }
}