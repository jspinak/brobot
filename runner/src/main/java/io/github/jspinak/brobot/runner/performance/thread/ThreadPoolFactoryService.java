package io.github.jspinak.brobot.runner.performance.thread;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service responsible for creating thread pools with appropriate configurations.
 * 
 * This service provides factory methods for creating different types of
 * thread pools optimized for various workloads. It handles thread factory
 * creation and pool configuration.
 * 
 * Thread Safety: This class is thread-safe.
 * 
 * @since 1.0.0
 */
@Slf4j
@Service
public class ThreadPoolFactoryService {
    
    /**
     * Create a managed thread pool with the given configuration.
     * 
     * @param name pool name
     * @param config pool configuration
     * @return managed thread pool
     */
    public ManagedThreadPool createManagedPool(String name, ThreadPoolConfig config) {
        config.validate();
        
        ThreadFactory threadFactory = createThreadFactory(name);
        RejectedExecutionHandler rejectionHandler = createRejectionHandler(name);
        
        ManagedThreadPool pool = new ManagedThreadPool(
            name,
            config.getCorePoolSize(),
            config.getMaximumPoolSize(),
            config.getKeepAliveTime(),
            config.getKeepAliveUnit(),
            new LinkedBlockingQueue<>(config.getQueueCapacity()),
            threadFactory,
            rejectionHandler
        );
        
        pool.allowCoreThreadTimeOut(config.isAllowCoreThreadTimeout());
        
        log.info("Created managed thread pool '{}' with configuration: {}", name, config);
        
        return pool;
    }
    
    /**
     * Create a default thread pool.
     * 
     * @param name pool name
     * @return thread pool executor
     */
    public ThreadPoolExecutor createDefaultPool(String name) {
        return createManagedPool(name, ThreadPoolConfig.defaultConfig());
    }
    
    /**
     * Create an I/O intensive thread pool.
     * 
     * @param name pool name
     * @return thread pool executor
     */
    public ThreadPoolExecutor createIOIntensivePool(String name) {
        return createManagedPool(name, ThreadPoolConfig.ioIntensiveConfig());
    }
    
    /**
     * Create a CPU intensive thread pool.
     * 
     * @param name pool name
     * @return thread pool executor
     */
    public ThreadPoolExecutor createCPUIntensivePool(String name) {
        return createManagedPool(name, ThreadPoolConfig.cpuIntensiveConfig());
    }
    
    /**
     * Create a single-threaded executor.
     * 
     * @param name executor name
     * @return single thread executor
     */
    public ExecutorService createSingleThreadExecutor(String name) {
        return Executors.newSingleThreadExecutor(createThreadFactory(name));
    }
    
    /**
     * Create a scheduled thread pool.
     * 
     * @param name pool name
     * @param corePoolSize number of threads
     * @return scheduled executor service
     */
    public ScheduledExecutorService createScheduledPool(String name, int corePoolSize) {
        return Executors.newScheduledThreadPool(corePoolSize, createThreadFactory(name));
    }
    
    /**
     * Create a work-stealing pool (ForkJoinPool).
     * 
     * @param name pool name
     * @param parallelism parallelism level
     * @return fork join pool
     */
    public ForkJoinPool createWorkStealingPool(String name, int parallelism) {
        return new ForkJoinPool(
            parallelism,
            pool -> {
                ForkJoinWorkerThread thread = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
                thread.setName(name + "-worker-" + thread.getPoolIndex());
                return thread;
            },
            (thread, exception) -> log.error("Uncaught exception in pool '{}'", name, exception),
            true // async mode
        );
    }
    
    /**
     * Create a thread factory with proper naming and exception handling.
     * 
     * @param poolName name prefix for threads
     * @return thread factory
     */
    public ThreadFactory createThreadFactory(String poolName) {
        return new OptimizedThreadFactory(poolName);
    }
    
    /**
     * Create a rejection handler for the pool.
     * 
     * @param poolName pool name for logging
     * @return rejection handler
     */
    private RejectedExecutionHandler createRejectionHandler(String poolName) {
        return (r, executor) -> {
            log.warn("Task rejected from pool '{}' - Queue size: {}, Active threads: {}",
                    poolName, executor.getQueue().size(), executor.getActiveCount());
            
            // Try to run in caller thread as fallback
            if (!executor.isShutdown()) {
                try {
                    r.run();
                } catch (Exception e) {
                    log.error("Failed to execute rejected task in caller thread", e);
                }
            }
        };
    }
    
    /**
     * Custom thread factory for consistent thread configuration.
     */
    static class OptimizedThreadFactory implements ThreadFactory {
        private final String namePrefix;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        
        OptimizedThreadFactory(String poolName) {
            group = Thread.currentThread().getThreadGroup();
            namePrefix = poolName + "-thread-";
        }
        
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, 
                                namePrefix + threadNumber.getAndIncrement(), 
                                0);
            
            // Ensure consistent thread configuration
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            
            // Set uncaught exception handler
            t.setUncaughtExceptionHandler((thread, exception) -> 
                log.error("Uncaught exception in thread '{}'", thread.getName(), exception)
            );
            
            return t;
        }
    }
}

/**
 * Managed thread pool that tracks metrics and provides health information.
 */
@Slf4j
class ManagedThreadPool extends ThreadPoolExecutor {
    private final String name;
    private final AtomicLong totalTasks = new AtomicLong();
    private final AtomicLong completedTasks = new AtomicLong();
    private final AtomicLong rejectedTasks = new AtomicLong();
    private final AtomicLong totalTaskDuration = new AtomicLong();
    private final AtomicInteger peakActiveThreads = new AtomicInteger();
    
    ManagedThreadPool(String name, int corePoolSize, int maximumPoolSize,
                     long keepAliveTime, TimeUnit unit,
                     BlockingQueue<Runnable> workQueue,
                     ThreadFactory threadFactory,
                     RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, 
              workQueue, threadFactory, handler);
        this.name = name;
    }
    
    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        totalTasks.incrementAndGet();
        
        // Track peak active threads
        int active = getActiveCount();
        int currentPeak = peakActiveThreads.get();
        if (active > currentPeak) {
            peakActiveThreads.compareAndSet(currentPeak, active);
        }
    }
    
    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        completedTasks.incrementAndGet();
        
        if (t != null) {
            log.error("Task execution failed in pool '{}'", name, t);
        }
    }
    
    @Override
    public void execute(Runnable command) {
        try {
            super.execute(command);
        } catch (RejectedExecutionException e) {
            rejectedTasks.incrementAndGet();
            throw e;
        }
    }
    
    /**
     * Get current health snapshot.
     * 
     * @return thread pool health
     */
    public ThreadPoolHealth getHealth() {
        int poolSize = getPoolSize();
        int activeThreads = getActiveCount();
        int queueSize = getQueue().size();
        
        double utilization = poolSize > 0 ? 
            (double) activeThreads / poolSize : 0.0;
            
        long avgDuration = completedTasks.get() > 0 ?
            totalTaskDuration.get() / completedTasks.get() : 0;
        
        boolean healthy = !isShutdown() && 
                         rejectedTasks.get() < totalTasks.get() * 0.05; // Less than 5% rejected
        
        return new ThreadPoolHealth(
            name,
            healthy,
            poolSize,
            activeThreads,
            queueSize,
            totalTasks.get(),
            completedTasks.get(),
            rejectedTasks.get(),
            utilization,
            avgDuration,
            getLargestPoolSize()
        );
    }
    
    /**
     * Get the pool name.
     * 
     * @return pool name
     */
    public String getName() {
        return name;
    }
}