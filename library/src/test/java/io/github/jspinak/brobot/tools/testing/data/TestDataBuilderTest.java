package io.github.jspinak.brobot.tools.testing.data;

import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateString;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for TestDataBuilder
 */
@DisplayName("TestDataBuilder Tests")
public class TestDataBuilderTest extends BrobotTestBase {

    private TestDataBuilder testDataBuilder;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        testDataBuilder = new TestDataBuilder();
    }

    @Nested
    @DisplayName("Scenario Creation Tests")
    class ScenarioCreationTests {

        @Test
        @DisplayName("Should create new scenario builder with name")
        void shouldCreateScenarioBuilderWithName() {
            // Act
            TestScenario.Builder builder = testDataBuilder.scenario("test-scenario");

            // Assert
            assertNotNull(builder);
            String name = (String) ReflectionTestUtils.getField(builder, "name");
            assertEquals("test-scenario", name);
        }

        @Test
        @DisplayName("Should store baseline scenario")
        void shouldStoreBaselineScenario() {
            // Arrange
            TestScenario scenario = TestScenario.builder()
                .name("baseline-test")
                .build();

            // Act
            testDataBuilder.storeBaselineScenario("baseline-test", scenario);

            // Assert
            TestScenario retrieved = testDataBuilder.getBaselineScenario("baseline-test");
            assertNotNull(retrieved);
            assertEquals("baseline-test", retrieved.getName());
        }

        @Test
        @DisplayName("Should retrieve null for non-existent scenario")
        void shouldRetrieveNullForNonExistentScenario() {
            // Act
            TestScenario scenario = testDataBuilder.getBaselineScenario("non-existent");

            // Assert
            assertNull(scenario);
        }

        @Test
        @DisplayName("Should overwrite existing baseline scenario")
        void shouldOverwriteExistingScenario() {
            // Arrange
            TestScenario scenario1 = TestScenario.builder()
                .name("test")
                .version("1.0")
                .build();
            
            TestScenario scenario2 = TestScenario.builder()
                .name("test")
                .version("2.0")
                .build();

            // Act
            testDataBuilder.storeBaselineScenario("test", scenario1);
            testDataBuilder.storeBaselineScenario("test", scenario2);

            // Assert
            TestScenario retrieved = testDataBuilder.getBaselineScenario("test");
            assertEquals("2.0", retrieved.getVersion());
        }
    }

    @Nested
    @DisplayName("Pre-configured Scenario Tests")
    class PreConfiguredScenarioTests {

        @Test
        @DisplayName("Should create login scenario with common elements")
        void shouldCreateLoginScenario() {
            // Act
            TestScenario.Builder builder = testDataBuilder.loginScenario();
            TestScenario scenario = builder.build();

            // Assert
            assertNotNull(builder);
            assertNotNull(scenario);
            assertEquals("login_flow", scenario.getName());
            
            // Should have pre-configured elements
            Map<String, StateImage> images = scenario.getStateImages();
            assertNotNull(images);
            assertEquals(3, images.size()); // username, password, login button
            assertTrue(images.containsKey("username_field"));
            assertTrue(images.containsKey("password_field"));
            assertTrue(images.containsKey("login_button"));
            
            // Should have state strings
            Map<String, StateString> strings = scenario.getStateStrings();
            assertNotNull(strings);
            assertEquals(2, strings.size());
            assertTrue(strings.containsKey("username_text"));
            assertTrue(strings.containsKey("password_text"));
            
            // Should have region
            Map<String, Region> regions = scenario.getRegions();
            assertNotNull(regions);
            assertEquals(1, regions.size());
            assertTrue(regions.containsKey("login_form"));
        }

        @Test
        @DisplayName("Should create navigation scenario with navigation elements")
        void shouldCreateNavigationScenario() {
            // Act
            TestScenario.Builder builder = testDataBuilder.navigationScenario();
            TestScenario scenario = builder.build();

            // Assert
            assertNotNull(builder);
            assertNotNull(scenario);
            assertEquals("navigation_flow", scenario.getName());
            
            // Should have navigation elements
            Map<String, StateImage> images = scenario.getStateImages();
            assertNotNull(images);
            assertEquals(3, images.size()); // back, forward, menu buttons
            assertTrue(images.containsKey("back_button"));
            assertTrue(images.containsKey("forward_button"));
            assertTrue(images.containsKey("menu_button"));
            
            // Should have navigation bar region
            Map<String, Region> regions = scenario.getRegions();
            assertNotNull(regions);
            assertEquals(1, regions.size());
            assertTrue(regions.containsKey("nav_bar"));
            Region navBar = regions.get("nav_bar");
            assertEquals(0, navBar.x());
            assertEquals(0, navBar.y());
            assertEquals(1200, navBar.w());
            assertEquals(80, navBar.h());
        }

        @Test
        @DisplayName("Should create form scenario with form elements")
        void shouldCreateFormScenario() {
            // Act
            TestScenario.Builder builder = testDataBuilder.formScenario();

            // Assert
            assertNotNull(builder);
            String name = (String) ReflectionTestUtils.getField(builder, "name");
            assertEquals("form_flow", name);
        }

        @Test
        @DisplayName("Should provide common variations")
        void shouldProvideCommonVariations() {
            // Act
            Map<String, TestVariation.Builder> variations = testDataBuilder.commonVariations();

            // Assert
            assertNotNull(variations);
            assertTrue(variations.containsKey("small_screen"));
            assertTrue(variations.containsKey("high_dpi"));
        }
    }

    @Nested
    @DisplayName("Multiple Scenario Management Tests")
    class MultipleScenarioTests {

        @Test
        @DisplayName("Should manage multiple baseline scenarios")
        void shouldManageMultipleScenarios() {
            // Arrange
            TestScenario scenario1 = TestScenario.builder()
                .name("scenario1")
                .build();
            
            TestScenario scenario2 = TestScenario.builder()
                .name("scenario2")
                .build();
            
            TestScenario scenario3 = TestScenario.builder()
                .name("scenario3")
                .build();

            // Act
            testDataBuilder.storeBaselineScenario("scenario1", scenario1);
            testDataBuilder.storeBaselineScenario("scenario2", scenario2);
            testDataBuilder.storeBaselineScenario("scenario3", scenario3);

            // Assert
            assertEquals(scenario1, testDataBuilder.getBaselineScenario("scenario1"));
            assertEquals(scenario2, testDataBuilder.getBaselineScenario("scenario2"));
            assertEquals(scenario3, testDataBuilder.getBaselineScenario("scenario3"));
        }

        @Test
        @DisplayName("Should maintain scenario isolation")
        void shouldMaintainScenarioIsolation() {
            // Arrange
            TestScenario scenario1 = TestScenario.builder()
                .name("isolated1")
                .version("1.0")
                .build();

            // Act
            testDataBuilder.storeBaselineScenario("isolated1", scenario1);
            
            // Create modified version
            TestScenario scenario2 = scenario1.toBuilder()
                .version("2.0")
                .build();

            // Assert - stored version should not change since scenarios are immutable
            TestScenario retrieved = testDataBuilder.getBaselineScenario("isolated1");
            assertEquals("1.0", retrieved.getVersion()); // Original is unchanged
        }

        @Test
        @DisplayName("Should handle empty baseline scenarios map")
        void shouldHandleEmptyBaselinesMap() {
            // Act
            TestScenario scenario = testDataBuilder.getBaselineScenario("any");

            // Assert
            assertNull(scenario);
        }
    }

    @Nested
    @DisplayName("Builder Chain Tests")
    class BuilderChainTests {

        @Test
        @DisplayName("Should support method chaining for scenario creation")
        void shouldSupportMethodChaining() {
            // Act - this should compile and not throw
            TestScenario.Builder builder = testDataBuilder
                .scenario("chained")
                .withStateImage("image1", "path1.png")
                .withStateString("string1", "value1")
                .withRegion("region1", new Region(0, 0, 100, 100));

            // Assert
            assertNotNull(builder);
        }

        @Test
        @DisplayName("Should create independent builders")
        void shouldCreateIndependentBuilders() {
            // Act
            TestScenario.Builder builder1 = testDataBuilder.scenario("builder1");
            TestScenario.Builder builder2 = testDataBuilder.scenario("builder2");

            // Assert
            assertNotSame(builder1, builder2);
            
            String name1 = (String) ReflectionTestUtils.getField(builder1, "name");
            String name2 = (String) ReflectionTestUtils.getField(builder2, "name");
            assertNotEquals(name1, name2);
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle null scenario name in storage")
        void shouldHandleNullScenarioName() {
            // Arrange
            TestScenario scenario = TestScenario.builder()
                .name(null)
                .build();

            // Act & Assert - should not throw
            assertDoesNotThrow(() -> 
                testDataBuilder.storeBaselineScenario(null, scenario));
            
            // Can retrieve with null key
            TestScenario retrieved = testDataBuilder.getBaselineScenario(null);
            assertEquals(scenario, retrieved);
        }

        @Test
        @DisplayName("Should handle null scenario in storage")
        void shouldHandleNullScenario() {
            // Act & Assert - should not throw
            assertDoesNotThrow(() -> 
                testDataBuilder.storeBaselineScenario("test", null));
            
            // Should store null
            assertNull(testDataBuilder.getBaselineScenario("test"));
        }

        @Test
        @DisplayName("Should handle empty scenario name")
        void shouldHandleEmptyScenarioName() {
            // Act
            TestScenario.Builder builder = testDataBuilder.scenario("");

            // Assert
            assertNotNull(builder);
            String name = (String) ReflectionTestUtils.getField(builder, "name");
            assertEquals("", name);
        }

        @Test
        @DisplayName("Should handle special characters in scenario name")
        void shouldHandleSpecialCharactersInName() {
            // Arrange
            String specialName = "test-scenario_2024!@#$%^&*()";
            TestScenario scenario = TestScenario.builder()
                .name(specialName)
                .build();

            // Act
            testDataBuilder.storeBaselineScenario(specialName, scenario);

            // Assert
            TestScenario retrieved = testDataBuilder.getBaselineScenario(specialName);
            assertNotNull(retrieved);
            assertEquals(specialName, retrieved.getName());
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should handle large number of scenarios efficiently")
        void shouldHandleLargeNumberOfScenarios() {
            // Arrange
            int scenarioCount = 1000;

            // Act
            long startTime = System.currentTimeMillis();
            
            for (int i = 0; i < scenarioCount; i++) {
                TestScenario scenario = TestScenario.builder()
                    .name("scenario" + i)
                    .build();
                testDataBuilder.storeBaselineScenario("scenario" + i, scenario);
            }
            
            long storeTime = System.currentTimeMillis() - startTime;

            // Retrieve all
            startTime = System.currentTimeMillis();
            for (int i = 0; i < scenarioCount; i++) {
                TestScenario retrieved = testDataBuilder.getBaselineScenario("scenario" + i);
                assertNotNull(retrieved);
            }
            long retrieveTime = System.currentTimeMillis() - startTime;

            // Assert - operations should be fast
            assertTrue(storeTime < 1000, "Storing 1000 scenarios took too long: " + storeTime + "ms");
            assertTrue(retrieveTime < 1000, "Retrieving 1000 scenarios took too long: " + retrieveTime + "ms");
        }

        @Test
        @DisplayName("Should handle concurrent access to scenarios")
        void shouldHandleConcurrentAccess() throws InterruptedException {
            // Arrange
            int threadCount = 10;
            int operationsPerThread = 100;
            Thread[] threads = new Thread[threadCount];

            // Act
            for (int i = 0; i < threadCount; i++) {
                final int threadId = i;
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < operationsPerThread; j++) {
                        TestScenario scenario = TestScenario.builder()
                            .name("thread" + threadId + "_scenario" + j)
                            .build();
                        testDataBuilder.storeBaselineScenario(
                            "thread" + threadId + "_scenario" + j, scenario);
                    }
                });
                threads[i].start();
            }

            // Wait for all threads
            for (Thread thread : threads) {
                thread.join();
            }

            // Assert - all scenarios should be stored
            for (int i = 0; i < threadCount; i++) {
                for (int j = 0; j < operationsPerThread; j++) {
                    TestScenario retrieved = testDataBuilder.getBaselineScenario(
                        "thread" + i + "_scenario" + j);
                    assertNotNull(retrieved);
                }
            }
        }
    }
}