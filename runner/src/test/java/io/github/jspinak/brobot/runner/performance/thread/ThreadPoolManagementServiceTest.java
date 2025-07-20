package io.github.jspinak.brobot.runner.performance.thread;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Timeout;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("ThreadPoolManagementService Tests")
class ThreadPoolManagementServiceTest {
    
    private ThreadPoolManagementService service;
    
    @Mock
    private ThreadPoolFactoryService mockFactory;
    
    @Mock
    private ManagedThreadPool mockPool;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ThreadPoolManagementService(mockFactory);
    }
    
    @Test
    @DisplayName("Should create pool successfully")
    void testCreatePool() {
        // Arrange
        String poolName = "test-pool";
        ThreadPoolConfig config = ThreadPoolConfig.defaultConfig();
        when(mockFactory.createManagedPool(poolName, config)).thenReturn(mockPool);
        
        // Act
        ExecutorService result = service.createPool(poolName, config);
        
        // Assert
        assertThat(result).isEqualTo(mockPool);
        assertThat(service.poolExists(poolName)).isTrue();
        verify(mockFactory).createManagedPool(poolName, config);
    }
    
    @Test
    @DisplayName("Should throw exception for duplicate pool name")
    void testCreateDuplicatePool() {
        // Arrange
        String poolName = "test-pool";
        ThreadPoolConfig config = ThreadPoolConfig.defaultConfig();
        when(mockFactory.createManagedPool(poolName, config)).thenReturn(mockPool);
        service.createPool(poolName, config);
        
        // Act & Assert
        assertThatThrownBy(() -> service.createPool(poolName, config))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("already exists");
    }
    
    @Test
    @DisplayName("Should get existing pool")
    void testGetPool() {
        // Arrange
        String poolName = "test-pool";
        when(mockFactory.createManagedPool(any(), any())).thenReturn(mockPool);
        service.createPool(poolName, ThreadPoolConfig.defaultConfig());
        
        // Act
        Optional<ExecutorService> result = service.getPool(poolName);
        
        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(mockPool);
    }
    
    @Test
    @DisplayName("Should return empty for non-existent pool")
    void testGetNonExistentPool() {
        // Act
        Optional<ExecutorService> result = service.getPool("non-existent");
        
        // Assert
        assertThat(result).isEmpty();
    }
    
    @Test
    @DisplayName("Should shutdown pool successfully")
    @Timeout(5)
    void testShutdownPool() throws InterruptedException {
        // Arrange
        String poolName = "test-pool";
        when(mockFactory.createManagedPool(any(), any())).thenReturn(mockPool);
        when(mockPool.awaitTermination(anyLong(), any())).thenReturn(true);
        service.createPool(poolName, ThreadPoolConfig.defaultConfig());
        
        // Act
        boolean result = service.shutdownPool(poolName, true, 1);
        
        // Assert
        assertThat(result).isTrue();
        assertThat(service.poolExists(poolName)).isFalse();
        verify(mockPool).shutdown();
        verify(mockPool).awaitTermination(1, TimeUnit.SECONDS);
    }
    
    @Test
    @DisplayName("Should force shutdown when timeout exceeded")
    @Timeout(5)
    void testShutdownPoolTimeout() throws InterruptedException {
        // Arrange
        String poolName = "test-pool";
        when(mockFactory.createManagedPool(any(), any())).thenReturn(mockPool);
        when(mockPool.awaitTermination(anyLong(), any())).thenReturn(false);
        service.createPool(poolName, ThreadPoolConfig.defaultConfig());
        
        // Act
        boolean result = service.shutdownPool(poolName, true, 1);
        
        // Assert
        assertThat(result).isTrue();
        verify(mockPool).shutdown();
        verify(mockPool).shutdownNow();
    }
    
    @Test
    @DisplayName("Should return false for non-existent pool shutdown")
    void testShutdownNonExistentPool() {
        // Act
        boolean result = service.shutdownPool("non-existent", false, 0);
        
        // Assert
        assertThat(result).isFalse();
    }
    
    @Test
    @DisplayName("Should get all pool health")
    void testGetAllPoolHealth() {
        // Arrange
        ThreadPoolHealth health = ThreadPoolHealth.unhealthy("test", "test");
        when(mockFactory.createManagedPool(any(), any())).thenReturn(mockPool);
        when(mockPool.getHealth()).thenReturn(health);
        service.createPool("pool1", ThreadPoolConfig.defaultConfig());
        service.createPool("pool2", ThreadPoolConfig.defaultConfig());
        
        // Act
        var result = service.getAllPoolHealth();
        
        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).containsKeys("pool1", "pool2");
    }
    
    @Test
    @DisplayName("Should adjust pool size")
    void testAdjustPoolSize() {
        // Arrange
        String poolName = "test-pool";
        when(mockFactory.createManagedPool(any(), any())).thenReturn(mockPool);
        service.createPool(poolName, ThreadPoolConfig.defaultConfig());
        
        // Act
        boolean result = service.adjustPoolSize(poolName, 5, 10);
        
        // Assert
        assertThat(result).isTrue();
        verify(mockPool).setCorePoolSize(5);
        verify(mockPool).setMaximumPoolSize(10);
    }
    
    @Test
    @DisplayName("Should validate pool size adjustment")
    void testAdjustPoolSizeValidation() {
        // Arrange
        String poolName = "test-pool";
        when(mockFactory.createManagedPool(any(), any())).thenReturn(mockPool);
        service.createPool(poolName, ThreadPoolConfig.defaultConfig());
        
        // Act & Assert
        assertThatThrownBy(() -> service.adjustPoolSize(poolName, 10, 5))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Core size cannot be greater than max size");
    }
    
    @Test
    @DisplayName("Should get total active threads")
    void testGetTotalActiveThreads() {
        // Arrange
        when(mockFactory.createManagedPool(any(), any())).thenReturn(mockPool);
        when(mockPool.getActiveCount()).thenReturn(3);
        service.createPool("pool1", ThreadPoolConfig.defaultConfig());
        service.createPool("pool2", ThreadPoolConfig.defaultConfig());
        
        // Act
        int result = service.getTotalActiveThreads();
        
        // Assert
        assertThat(result).isEqualTo(6);
    }
    
    @Test
    @DisplayName("Should provide diagnostic info")
    void testDiagnosticInfo() {
        // Arrange
        service.enableDiagnosticMode(true);
        
        // Act
        var diagnosticInfo = service.getDiagnosticInfo();
        
        // Assert
        assertThat(diagnosticInfo.getComponent()).isEqualTo("ThreadPoolManagementService");
        assertThat(diagnosticInfo.getStates())
            .containsKeys("totalPools", "totalActiveThreads", "totalQueuedTasks");
    }
}