package io.github.jspinak.brobot.aspects.annotations;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.test.TestCategories;
import org.junit.jupiter.api.*;

import java.lang.annotation.Documented;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for @Recoverable annotation.
 * Tests retry logic configuration, exception handling, backoff strategies, and fallback methods.
 */
@DisplayName("@Recoverable Annotation Tests")
@Tag(TestCategories.UNIT)
@Tag(TestCategories.FAST)
@Tag("annotations")
@Tag("aspects")
public class RecoverableAnnotationTest extends BrobotTestBase {
    
    // Custom exception classes for testing
    static class NetworkException extends Exception {}
    static class ValidationException extends Exception {}
    static class DatabaseException extends Exception {}
    
    // Test class with various @Recoverable configurations
    static class RecoverableTestClass {
        
        @Recoverable
        public void simpleRecoverableMethod() throws Exception {}
        
        @Recoverable(maxRetries = 5, delay = 2000, backoff = 3.0)
        public void customRetryMethod() throws Exception {}
        
        @Recoverable(
            maxRetries = 10,
            delay = 500,
            backoff = 1.5,
            timeout = 60000,
            jitter = false,
            strategy = Recoverable.RecoveryStrategy.FIBONACCI_BACKOFF,
            fallbackMethod = "fallbackHandler",
            logRetries = false
        )
        public String complexRecoverableMethod() throws Exception {
            return "result";
        }
        
        @Recoverable(retryOn = {NetworkException.class, DatabaseException.class})
        public void selectiveRetryMethod() throws Exception {}
        
        @Recoverable(skipOn = {ValidationException.class})
        public void skipSpecificExceptionsMethod() throws Exception {}
        
        @Recoverable(
            retryOn = {NetworkException.class},
            skipOn = {ValidationException.class}
        )
        public void mixedExceptionHandling() throws Exception {}
        
        @Recoverable(timeout = -1)
        public void noTimeoutMethod() throws Exception {}
        
        @Recoverable(strategy = Recoverable.RecoveryStrategy.FIXED_DELAY)
        public void fixedDelayMethod() throws Exception {}
        
        @Recoverable(strategy = Recoverable.RecoveryStrategy.EXPONENTIAL_BACKOFF)
        public void exponentialBackoffMethod() throws Exception {}
        
        @Recoverable(strategy = Recoverable.RecoveryStrategy.CUSTOM, fallbackMethod = "customHandler")
        public void customStrategyMethod() throws Exception {}
        
        // Fallback methods
        public String fallbackHandler() {
            return "fallback";
        }
        
        public void customHandler() {}
        
        public void unrecoverableMethod() throws Exception {}
    }
    
    @Recoverable
    static class ClassLevelRecoverable {
        public void method1() throws Exception {}
        public void method2() throws Exception {}
    }
    
    @BeforeEach
    public void setupTest() {
        super.setupTest();
    }
    
    @Nested
    @DisplayName("Annotation Presence Tests")
    class AnnotationPresenceTests {
        
        @Test
        @DisplayName("Should detect @Recoverable on methods")
        void shouldDetectRecoverableOnMethods() throws NoSuchMethodException {
            Method simpleMethod = RecoverableTestClass.class.getMethod("simpleRecoverableMethod");
            Method customMethod = RecoverableTestClass.class.getMethod("customRetryMethod");
            Method unrecoverableMethod = RecoverableTestClass.class.getMethod("unrecoverableMethod");
            
            assertTrue(simpleMethod.isAnnotationPresent(Recoverable.class));
            assertTrue(customMethod.isAnnotationPresent(Recoverable.class));
            assertFalse(unrecoverableMethod.isAnnotationPresent(Recoverable.class));
        }
        
        @Test
        @DisplayName("Should detect @Recoverable on class")
        void shouldDetectRecoverableOnClass() {
            assertTrue(ClassLevelRecoverable.class.isAnnotationPresent(Recoverable.class));
            assertFalse(RecoverableTestClass.class.isAnnotationPresent(Recoverable.class));
        }
        
        @Test
        @DisplayName("Should have runtime retention")
        void shouldHaveRuntimeRetention() throws NoSuchMethodException {
            Method method = RecoverableTestClass.class.getMethod("simpleRecoverableMethod");
            Recoverable annotation = method.getAnnotation(Recoverable.class);
            assertNotNull(annotation);
        }
    }
    
    @Nested
    @DisplayName("Default Values Tests")
    class DefaultValuesTests {
        
        @Test
        @DisplayName("Should have correct default values")
        void shouldHaveCorrectDefaultValues() throws NoSuchMethodException {
            Method method = RecoverableTestClass.class.getMethod("simpleRecoverableMethod");
            Recoverable annotation = method.getAnnotation(Recoverable.class);
            
            assertEquals(3, annotation.maxRetries());
            assertEquals(1000, annotation.delay());
            assertEquals(2.0, annotation.backoff(), 0.001);
            assertEquals(30000, annotation.timeout());
            assertTrue(annotation.jitter());
            assertEquals(Recoverable.RecoveryStrategy.EXPONENTIAL_BACKOFF, annotation.strategy());
            assertEquals("", annotation.fallbackMethod());
            assertTrue(annotation.logRetries());
            assertArrayEquals(new Class[]{}, annotation.retryOn());
            assertArrayEquals(new Class[]{}, annotation.skipOn());
        }
    }
    
    @Nested
    @DisplayName("Retry Configuration Tests")
    class RetryConfigurationTests {
        
        @Test
        @DisplayName("Should read custom retry configuration")
        void shouldReadCustomRetryConfig() throws NoSuchMethodException {
            Method method = RecoverableTestClass.class.getMethod("customRetryMethod");
            Recoverable annotation = method.getAnnotation(Recoverable.class);
            
            assertEquals(5, annotation.maxRetries());
            assertEquals(2000, annotation.delay());
            assertEquals(3.0, annotation.backoff(), 0.001);
        }
        
        @Test
        @DisplayName("Should read complex configuration")
        void shouldReadComplexConfiguration() throws NoSuchMethodException {
            Method method = RecoverableTestClass.class.getMethod("complexRecoverableMethod");
            Recoverable annotation = method.getAnnotation(Recoverable.class);
            
            assertEquals(10, annotation.maxRetries());
            assertEquals(500, annotation.delay());
            assertEquals(1.5, annotation.backoff(), 0.001);
            assertEquals(60000, annotation.timeout());
            assertFalse(annotation.jitter());
            assertEquals(Recoverable.RecoveryStrategy.FIBONACCI_BACKOFF, annotation.strategy());
            assertEquals("fallbackHandler", annotation.fallbackMethod());
            assertFalse(annotation.logRetries());
        }
        
        @Test
        @DisplayName("Should support no timeout")
        void shouldSupportNoTimeout() throws NoSuchMethodException {
            Method method = RecoverableTestClass.class.getMethod("noTimeoutMethod");
            Recoverable annotation = method.getAnnotation(Recoverable.class);
            
            assertEquals(-1, annotation.timeout());
        }
    }
    
    @Nested
    @DisplayName("Exception Handling Tests")
    class ExceptionHandlingTests {
        
        @Test
        @DisplayName("Should specify exceptions to retry on")
        void shouldSpecifyRetryExceptions() throws NoSuchMethodException {
            Method method = RecoverableTestClass.class.getMethod("selectiveRetryMethod");
            Recoverable annotation = method.getAnnotation(Recoverable.class);
            
            Class<? extends Throwable>[] retryOn = annotation.retryOn();
            assertEquals(2, retryOn.length);
            assertTrue(Arrays.asList(retryOn).contains(NetworkException.class));
            assertTrue(Arrays.asList(retryOn).contains(DatabaseException.class));
        }
        
        @Test
        @DisplayName("Should specify exceptions to skip")
        void shouldSpecifySkipExceptions() throws NoSuchMethodException {
            Method method = RecoverableTestClass.class.getMethod("skipSpecificExceptionsMethod");
            Recoverable annotation = method.getAnnotation(Recoverable.class);
            
            Class<? extends Throwable>[] skipOn = annotation.skipOn();
            assertEquals(1, skipOn.length);
            assertEquals(ValidationException.class, skipOn[0]);
        }
        
        @Test
        @DisplayName("Should handle mixed exception configuration")
        void shouldHandleMixedExceptionConfig() throws NoSuchMethodException {
            Method method = RecoverableTestClass.class.getMethod("mixedExceptionHandling");
            Recoverable annotation = method.getAnnotation(Recoverable.class);
            
            Class<? extends Throwable>[] retryOn = annotation.retryOn();
            Class<? extends Throwable>[] skipOn = annotation.skipOn();
            
            assertEquals(1, retryOn.length);
            assertEquals(NetworkException.class, retryOn[0]);
            assertEquals(1, skipOn.length);
            assertEquals(ValidationException.class, skipOn[0]);
        }
        
        @Test
        @DisplayName("Should have empty exception arrays by default")
        void shouldHaveEmptyExceptionArraysByDefault() throws NoSuchMethodException {
            Method method = RecoverableTestClass.class.getMethod("simpleRecoverableMethod");
            Recoverable annotation = method.getAnnotation(Recoverable.class);
            
            assertEquals(0, annotation.retryOn().length);
            assertEquals(0, annotation.skipOn().length);
        }
    }
    
    @Nested
    @DisplayName("Recovery Strategy Tests")
    class RecoveryStrategyTests {
        
        @Test
        @DisplayName("Should support fixed delay strategy")
        void shouldSupportFixedDelay() throws NoSuchMethodException {
            Method method = RecoverableTestClass.class.getMethod("fixedDelayMethod");
            Recoverable annotation = method.getAnnotation(Recoverable.class);
            
            assertEquals(Recoverable.RecoveryStrategy.FIXED_DELAY, annotation.strategy());
        }
        
        @Test
        @DisplayName("Should support exponential backoff strategy")
        void shouldSupportExponentialBackoff() throws NoSuchMethodException {
            Method method = RecoverableTestClass.class.getMethod("exponentialBackoffMethod");
            Recoverable annotation = method.getAnnotation(Recoverable.class);
            
            assertEquals(Recoverable.RecoveryStrategy.EXPONENTIAL_BACKOFF, annotation.strategy());
        }
        
        @Test
        @DisplayName("Should support fibonacci backoff strategy")
        void shouldSupportFibonacciBackoff() throws NoSuchMethodException {
            Method method = RecoverableTestClass.class.getMethod("complexRecoverableMethod");
            Recoverable annotation = method.getAnnotation(Recoverable.class);
            
            assertEquals(Recoverable.RecoveryStrategy.FIBONACCI_BACKOFF, annotation.strategy());
        }
        
        @Test
        @DisplayName("Should support custom strategy with fallback")
        void shouldSupportCustomStrategy() throws NoSuchMethodException {
            Method method = RecoverableTestClass.class.getMethod("customStrategyMethod");
            Recoverable annotation = method.getAnnotation(Recoverable.class);
            
            assertEquals(Recoverable.RecoveryStrategy.CUSTOM, annotation.strategy());
            assertEquals("customHandler", annotation.fallbackMethod());
        }
        
        @Test
        @DisplayName("Should default to exponential backoff")
        void shouldDefaultToExponentialBackoff() throws NoSuchMethodException {
            Method method = RecoverableTestClass.class.getMethod("simpleRecoverableMethod");
            Recoverable annotation = method.getAnnotation(Recoverable.class);
            
            assertEquals(Recoverable.RecoveryStrategy.EXPONENTIAL_BACKOFF, annotation.strategy());
        }
    }
    
    @Nested
    @DisplayName("Fallback Method Tests")
    class FallbackMethodTests {
        
        @Test
        @DisplayName("Should specify fallback method")
        void shouldSpecifyFallbackMethod() throws NoSuchMethodException {
            Method method = RecoverableTestClass.class.getMethod("complexRecoverableMethod");
            Recoverable annotation = method.getAnnotation(Recoverable.class);
            
            assertEquals("fallbackHandler", annotation.fallbackMethod());
        }
        
        @Test
        @DisplayName("Should have empty fallback by default")
        void shouldHaveEmptyFallbackByDefault() throws NoSuchMethodException {
            Method method = RecoverableTestClass.class.getMethod("simpleRecoverableMethod");
            Recoverable annotation = method.getAnnotation(Recoverable.class);
            
            assertEquals("", annotation.fallbackMethod());
        }
        
        @Test
        @DisplayName("Should verify fallback method exists")
        void shouldVerifyFallbackMethodExists() throws NoSuchMethodException {
            Method recoverableMethod = RecoverableTestClass.class.getMethod("complexRecoverableMethod");
            Recoverable annotation = recoverableMethod.getAnnotation(Recoverable.class);
            String fallbackName = annotation.fallbackMethod();
            
            // Verify the fallback method exists in the class
            Method fallbackMethod = RecoverableTestClass.class.getMethod(fallbackName);
            assertNotNull(fallbackMethod);
        }
    }
    
    @Nested
    @DisplayName("Jitter and Logging Tests")
    class JitterAndLoggingTests {
        
        @Test
        @DisplayName("Should enable jitter by default")
        void shouldEnableJitterByDefault() throws NoSuchMethodException {
            Method method = RecoverableTestClass.class.getMethod("simpleRecoverableMethod");
            Recoverable annotation = method.getAnnotation(Recoverable.class);
            
            assertTrue(annotation.jitter());
        }
        
        @Test
        @DisplayName("Should allow disabling jitter")
        void shouldAllowDisablingJitter() throws NoSuchMethodException {
            Method method = RecoverableTestClass.class.getMethod("complexRecoverableMethod");
            Recoverable annotation = method.getAnnotation(Recoverable.class);
            
            assertFalse(annotation.jitter());
        }
        
        @Test
        @DisplayName("Should enable logging by default")
        void shouldEnableLoggingByDefault() throws NoSuchMethodException {
            Method method = RecoverableTestClass.class.getMethod("simpleRecoverableMethod");
            Recoverable annotation = method.getAnnotation(Recoverable.class);
            
            assertTrue(annotation.logRetries());
        }
        
        @Test
        @DisplayName("Should allow disabling logging")
        void shouldAllowDisablingLogging() throws NoSuchMethodException {
            Method method = RecoverableTestClass.class.getMethod("complexRecoverableMethod");
            Recoverable annotation = method.getAnnotation(Recoverable.class);
            
            assertFalse(annotation.logRetries());
        }
    }
    
    @Nested
    @DisplayName("Enum Strategy Tests")
    class EnumStrategyTests {
        
        @Test
        @DisplayName("Should have all recovery strategies")
        void shouldHaveAllRecoveryStrategies() {
            Recoverable.RecoveryStrategy[] strategies = Recoverable.RecoveryStrategy.values();
            
            assertEquals(4, strategies.length);
            assertTrue(Arrays.asList(strategies).contains(Recoverable.RecoveryStrategy.FIXED_DELAY));
            assertTrue(Arrays.asList(strategies).contains(Recoverable.RecoveryStrategy.EXPONENTIAL_BACKOFF));
            assertTrue(Arrays.asList(strategies).contains(Recoverable.RecoveryStrategy.FIBONACCI_BACKOFF));
            assertTrue(Arrays.asList(strategies).contains(Recoverable.RecoveryStrategy.CUSTOM));
        }
        
        @Test
        @DisplayName("Should get strategy by name")
        void shouldGetStrategyByName() {
            Recoverable.RecoveryStrategy strategy = 
                Recoverable.RecoveryStrategy.valueOf("EXPONENTIAL_BACKOFF");
            assertEquals(Recoverable.RecoveryStrategy.EXPONENTIAL_BACKOFF, strategy);
        }
    }
    
    @Nested
    @DisplayName("Annotation Metadata Tests")
    class AnnotationMetadataTests {
        
        @Test
        @DisplayName("Should be documented")
        void shouldBeDocumented() {
            assertTrue(Recoverable.class.isAnnotationPresent(Documented.class));
        }
        
        @Test
        @DisplayName("Should have proper annotation methods")
        void shouldHaveProperAnnotationMethods() throws NoSuchMethodException {
            assertNotNull(Recoverable.class.getMethod("maxRetries"));
            assertNotNull(Recoverable.class.getMethod("delay"));
            assertNotNull(Recoverable.class.getMethod("backoff"));
            assertNotNull(Recoverable.class.getMethod("timeout"));
            assertNotNull(Recoverable.class.getMethod("retryOn"));
            assertNotNull(Recoverable.class.getMethod("skipOn"));
            assertNotNull(Recoverable.class.getMethod("jitter"));
            assertNotNull(Recoverable.class.getMethod("strategy"));
            assertNotNull(Recoverable.class.getMethod("fallbackMethod"));
            assertNotNull(Recoverable.class.getMethod("logRetries"));
            
            // Verify return types
            assertEquals(int.class, Recoverable.class.getMethod("maxRetries").getReturnType());
            assertEquals(long.class, Recoverable.class.getMethod("delay").getReturnType());
            assertEquals(double.class, Recoverable.class.getMethod("backoff").getReturnType());
            assertEquals(boolean.class, Recoverable.class.getMethod("jitter").getReturnType());
            assertEquals(String.class, Recoverable.class.getMethod("fallbackMethod").getReturnType());
        }
        
        @Test
        @DisplayName("Should have proper annotation type")
        void shouldHaveProperAnnotationType() {
            assertTrue(Recoverable.class.isAnnotation());
            assertEquals("Recoverable", Recoverable.class.getSimpleName());
        }
    }
}