package io.github.jspinak.brobot.persistence;

import io.github.jspinak.brobot.persistence.config.PersistenceConfiguration;
import io.github.jspinak.brobot.persistence.provider.FileBasedPersistenceProvider;
import io.github.jspinak.brobot.persistence.provider.InMemoryPersistenceProvider;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PersistenceProviderFactory.
 */
@DisplayName("PersistenceProviderFactory Tests")
class PersistenceProviderFactoryTest extends BrobotTestBase {
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
    }
    
    @Test
    @DisplayName("Should create file-based provider from configuration")
    void testCreateFileBasedProvider() {
        // Given
        PersistenceConfiguration config = PersistenceConfiguration.fileDefault();
        config.getFile().setBasePath(tempDir.toString());
        
        // When
        PersistenceProvider provider = PersistenceProviderFactory.create(config);
        
        // Then
        assertNotNull(provider);
        assertInstanceOf(FileBasedPersistenceProvider.class, provider);
    }
    
    @Test
    @DisplayName("Should create in-memory provider from configuration")
    void testCreateInMemoryProvider() {
        // Given
        PersistenceConfiguration config = PersistenceConfiguration.memoryDefault();
        
        // When
        PersistenceProvider provider = PersistenceProviderFactory.create(config);
        
        // Then
        assertNotNull(provider);
        assertInstanceOf(InMemoryPersistenceProvider.class, provider);
    }
    
    @Test
    @DisplayName("Should throw exception for null configuration")
    void testCreateWithNullConfiguration() {
        // When/Then
        assertThrows(IllegalArgumentException.class, 
            () -> PersistenceProviderFactory.create(null),
            "Configuration cannot be null");
    }
    
    @Test
    @DisplayName("Should throw exception for database type without Spring context")
    void testCreateDatabaseProviderWithoutSpring() {
        // Given
        PersistenceConfiguration config = new PersistenceConfiguration();
        config.setType(PersistenceConfiguration.PersistenceType.DATABASE);
        
        // When/Then
        UnsupportedOperationException exception = assertThrows(
            UnsupportedOperationException.class,
            () -> PersistenceProviderFactory.create(config)
        );
        assertTrue(exception.getMessage().contains("Database persistence requires Spring context"));
    }
    
    @Test
    @DisplayName("Should throw exception for custom type")
    void testCreateCustomProvider() {
        // Given
        PersistenceConfiguration config = new PersistenceConfiguration();
        config.setType(PersistenceConfiguration.PersistenceType.CUSTOM);
        
        // When/Then
        UnsupportedOperationException exception = assertThrows(
            UnsupportedOperationException.class,
            () -> PersistenceProviderFactory.create(config)
        );
        assertTrue(exception.getMessage().contains("Custom persistence requires implementing"));
    }
    
    @Test
    @DisplayName("Should create default file-based provider")
    void testCreateDefault() {
        // When
        PersistenceProvider provider = PersistenceProviderFactory.createDefault();
        
        // Then
        assertNotNull(provider);
        assertInstanceOf(FileBasedPersistenceProvider.class, provider);
    }
    
    @Test
    @DisplayName("Should create file-based provider with custom path")
    void testCreateFileWithCustomPath() {
        // Given
        String customPath = tempDir.toString();
        
        // When
        PersistenceProvider provider = PersistenceProviderFactory.createFile(customPath);
        
        // Then
        assertNotNull(provider);
        assertInstanceOf(FileBasedPersistenceProvider.class, provider);
        
        // Start a session to verify the path is used
        String sessionId = provider.startSession("Test", "App", null);
        assertNotNull(sessionId);
        provider.stopSession();
    }
    
    @Test
    @DisplayName("Should create in-memory provider")
    void testCreateInMemory() {
        // When
        PersistenceProvider provider = PersistenceProviderFactory.createInMemory();
        
        // Then
        assertNotNull(provider);
        assertInstanceOf(InMemoryPersistenceProvider.class, provider);
    }
    
    @Test
    @DisplayName("Should create in-memory provider with export path")
    void testCreateInMemoryWithExport() {
        // Given
        String exportPath = tempDir.resolve("export").toString();
        
        // When
        PersistenceProvider provider = PersistenceProviderFactory.createInMemoryWithExport(exportPath);
        
        // Then
        assertNotNull(provider);
        assertInstanceOf(InMemoryPersistenceProvider.class, provider);
        
        // Verify configuration was set correctly
        // Start and stop a session to test the configuration
        String sessionId = provider.startSession("Test", "App", null);
        assertNotNull(sessionId);
        provider.stopSession();
        
        // The actual export on shutdown would happen when the provider is shut down
        // We can't easily test that here without accessing internal state
    }
    
    @Test
    @DisplayName("Should handle all persistence types correctly")
    void testAllPersistenceTypes() {
        // FILE type
        PersistenceConfiguration fileConfig = new PersistenceConfiguration();
        fileConfig.setType(PersistenceConfiguration.PersistenceType.FILE);
        fileConfig.getFile().setBasePath(tempDir.toString());
        PersistenceProvider fileProvider = PersistenceProviderFactory.create(fileConfig);
        assertInstanceOf(FileBasedPersistenceProvider.class, fileProvider);
        
        // MEMORY type
        PersistenceConfiguration memoryConfig = new PersistenceConfiguration();
        memoryConfig.setType(PersistenceConfiguration.PersistenceType.MEMORY);
        PersistenceProvider memoryProvider = PersistenceProviderFactory.create(memoryConfig);
        assertInstanceOf(InMemoryPersistenceProvider.class, memoryProvider);
        
        // DATABASE type - should throw
        PersistenceConfiguration dbConfig = new PersistenceConfiguration();
        dbConfig.setType(PersistenceConfiguration.PersistenceType.DATABASE);
        assertThrows(UnsupportedOperationException.class, 
            () -> PersistenceProviderFactory.create(dbConfig));
        
        // CUSTOM type - should throw
        PersistenceConfiguration customConfig = new PersistenceConfiguration();
        customConfig.setType(PersistenceConfiguration.PersistenceType.CUSTOM);
        assertThrows(UnsupportedOperationException.class, 
            () -> PersistenceProviderFactory.create(customConfig));
    }
}