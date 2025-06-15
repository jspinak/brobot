package io.github.jspinak.brobot.runner.performance;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class ThreadManagementOptimizer {

    private final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
    
    // Managed thread pools
    private final Map<String, ManagedThreadPool> threadPools = new ConcurrentHashMap<>();
    
    // Global thread pool for shared tasks
    private final ForkJoinPool sharedPool = new ForkJoinPool(
        Runtime.getRuntime().availableProcessors(),
        ForkJoinPool.defaultForkJoinWorkerThreadFactory,
        (thread, exception) -> log.error("Uncaught exception in shared pool", exception),
        true // Async mode for better throughput
    );
    
    // Monitoring
    private final ScheduledExecutorService monitor = Executors.newSingleThreadScheduledExecutor(
        r -> new Thread(r, "thread-monitor")
    );
    
    private volatile int maxThreadCount = 200;
    private volatile double cpuThreshold = 0.9;
    
    @PostConstruct
    public void initialize() {
        log.info("Initializing thread management optimizer");
        
        // Enable thread contention monitoring
        threadBean.setThreadContentionMonitoringEnabled(true);
        
        // Start monitoring
        monitor.scheduleAtFixedRate(this::monitorThreads, 5, 5, TimeUnit.SECONDS);
        
        // Configure system thread pools
        configureSystemThreadPools();
    }
    
    @PreDestroy
    public void shutdown() {
        monitor.shutdownNow();
        sharedPool.shutdownNow();
        threadPools.values().forEach(ManagedThreadPool::shutdown);
    }
    
    private void configureSystemThreadPools() {
        // Configure ForkJoinPool common pool
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism",
            String.valueOf(Runtime.getRuntime().availableProcessors()));
        
        System.setProperty("java.util.concurrent.ForkJoinPool.common.threadFactory",
            "io.github.jspinak.brobot.runner.performance.ThreadManagementOptimizer$OptimizedThreadFactory");
    }
    
    /**
     * Create an optimized thread pool for a specific purpose.
     */
    public ExecutorService createOptimizedPool(String name, ThreadPoolConfig config) {
        ManagedThreadPool pool = new ManagedThreadPool(name, config);
        threadPools.put(name, pool);
        return pool;
    }
    
    /**
     * Get the shared ForkJoinPool for parallel operations.
     */
    public ForkJoinPool getSharedPool() {
        return sharedPool;
    }
    
    /**
     * Execute a parallel computation with optimal thread usage.
     */
    public <T> CompletableFuture<T> executeParallel(Callable<T> task) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, sharedPool);
    }
    
    /**
     * Execute multiple tasks in parallel with controlled concurrency.
     */
    public <T> List<CompletableFuture<T>> executeAllParallel(
            List<Callable<T>> tasks, int maxConcurrency) {
        
        Semaphore semaphore = new Semaphore(maxConcurrency);
        
        return tasks.stream()
            .map(task -> CompletableFuture.supplyAsync(() -> {
                try {
                    semaphore.acquire();
                    try {
                        return task.call();
                    } finally {
                        semaphore.release();
                    }
                } catch (Exception e) {
                    throw new CompletionException(e);
                }
            }, sharedPool))
            .toList();
    }
    
    private void monitorThreads() {
        try {
            int activeThreads = threadBean.getThreadCount();
            
            if (activeThreads > maxThreadCount) {
                log.warn("High thread count detected: {} threads", activeThreads);
                optimizeThreadUsage();
            }
            
            // Check for thread contention
            detectThreadContention();
            
            // Monitor thread pool health
            threadPools.forEach((name, pool) -> {
                ThreadPoolHealth health = pool.getHealth();
                if (!health.isHealthy()) {
                    log.warn("Thread pool '{}' unhealthy: {}", name, health);
                    pool.optimize();
                }
            });
            
        } catch (Exception e) {
            log.error("Error monitoring threads", e);
        }
    }
    
    private void optimizeThreadUsage() {
        // Reduce thread pool sizes if CPU usage is high
        double cpuUsage = getCpuUsage();
        
        if (cpuUsage > cpuThreshold) {
            log.info("High CPU usage detected: {}%, optimizing thread pools", cpuUsage * 100);
            
            threadPools.values().forEach(pool -> {
                pool.reduceThreads(0.8); // Reduce by 20%
            });
        }
    }
    
    private void detectThreadContention() {
        long[] threadIds = threadBean.findDeadlockedThreads();
        if (threadIds != null && threadIds.length > 0) {
            log.error("Deadlock detected! {} threads involved", threadIds.length);
            
            ThreadInfo[] threadInfos = threadBean.getThreadInfo(threadIds, true, true);
            for (ThreadInfo info : threadInfos) {
                log.error("Deadlocked thread: {}", formatThreadInfo(info));
            }
        }
        
        // Check for high contention
        ThreadInfo[] allThreads = threadBean.dumpAllThreads(false, false);
        int blockedCount = 0;
        
        for (ThreadInfo thread : allThreads) {
            if (thread.getThreadState() == Thread.State.BLOCKED) {
                blockedCount++;
            }
        }
        
        if (blockedCount > activeThreads() * 0.3) {
            log.warn("High thread contention detected: {} blocked threads", blockedCount);
        }
    }
    
    private int activeThreads() {
        return threadBean.getThreadCount();
    }
    
    private double getCpuUsage() {
        // This is a simplified CPU usage calculation
        return ((com.sun.management.OperatingSystemMXBean) 
            ManagementFactory.getOperatingSystemMXBean()).getProcessCpuLoad();
    }
    
    private String formatThreadInfo(ThreadInfo info) {
        return String.format("Thread '%s' (ID=%d, State=%s)",
            info.getThreadName(), info.getThreadId(), info.getThreadState());
    }
    
    @Scheduled(fixedDelay = 30000) // Every 30 seconds
    public void reportThreadStatistics() {
        if (log.isDebugEnabled()) {
            StringBuilder report = new StringBuilder("Thread Statistics:\n");
            
            report.append(String.format("  Total threads: %d\n", threadBean.getThreadCount()));
            report.append(String.format("  Peak threads: %d\n", threadBean.getPeakThreadCount()));
            report.append(String.format("  Daemon threads: %d\n", threadBean.getDaemonThreadCount()));
            
            threadPools.forEach((name, pool) -> {
                ThreadPoolHealth health = pool.getHealth();
                report.append(String.format("  Pool '%s': %s\n", name, health));
            });
            
            log.debug(report.toString());
        }
    }
    
    /**
     * Configuration for managed thread pools.
     */
    public static class ThreadPoolConfig {
        public final int corePoolSize;
        public final int maximumPoolSize;
        public final long keepAliveTime;
        public final TimeUnit keepAliveUnit;
        public final int queueCapacity;
        public final boolean allowCoreThreadTimeout;
        
        public ThreadPoolConfig(int corePoolSize, int maximumPoolSize, 
                              long keepAliveTime, TimeUnit keepAliveUnit,
                              int queueCapacity, boolean allowCoreThreadTimeout) {
            this.corePoolSize = corePoolSize;
            this.maximumPoolSize = maximumPoolSize;
            this.keepAliveTime = keepAliveTime;
            this.keepAliveUnit = keepAliveUnit;
            this.queueCapacity = queueCapacity;
            this.allowCoreThreadTimeout = allowCoreThreadTimeout;
        }
        
        public static ThreadPoolConfig defaultConfig() {
            int cores = Runtime.getRuntime().availableProcessors();
            return new ThreadPoolConfig(
                cores,
                cores * 2,
                60L,
                TimeUnit.SECONDS,
                100,
                true
            );
        }
        
        public static ThreadPoolConfig ioIntensiveConfig() {
            int cores = Runtime.getRuntime().availableProcessors();
            return new ThreadPoolConfig(
                cores * 2,
                cores * 4,
                120L,
                TimeUnit.SECONDS,
                500,
                true
            );
        }
        
        public static ThreadPoolConfig cpuIntensiveConfig() {
            int cores = Runtime.getRuntime().availableProcessors();
            return new ThreadPoolConfig(
                cores,
                cores,
                0L,
                TimeUnit.SECONDS,
                50,
                false
            );
        }
    }
    
    /**
     * Managed thread pool with monitoring and optimization.
     */
    private class ManagedThreadPool extends ThreadPoolExecutor {
        private final String name;
        private final AtomicLong totalTasks = new AtomicLong();
        private final AtomicLong completedTasks = new AtomicLong();
        private final AtomicLong rejectedTasks = new AtomicLong();
        private final AtomicInteger peakActiveThreads = new AtomicInteger();
        
        ManagedThreadPool(String name, ThreadPoolConfig config) {
            super(
                config.corePoolSize,
                config.maximumPoolSize,
                config.keepAliveTime,
                config.keepAliveUnit,
                config.queueCapacity > 0 
                    ? new LinkedBlockingQueue<>(config.queueCapacity)
                    : new SynchronousQueue<>(),
                new OptimizedThreadFactory(name),
                null // Set rejection handler after construction
            );
            
            this.name = name;
            this.allowCoreThreadTimeOut(config.allowCoreThreadTimeout);
            setRejectedExecutionHandler(new OptimizedRejectionHandler(name));
        }
        
        @Override
        protected void beforeExecute(Thread t, Runnable r) {
            super.beforeExecute(t, r);
            int active = getActiveCount();
            peakActiveThreads.updateAndGet(current -> Math.max(current, active));
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
            totalTasks.incrementAndGet();
            super.execute(command);
        }
        
        public ThreadPoolHealth getHealth() {
            int activeThreads = getActiveCount();
            int poolSize = getPoolSize();
            int queueSize = getQueue().size();
            double utilization = poolSize > 0 ? (double) activeThreads / poolSize : 0;
            
            boolean healthy = utilization < 0.95 && queueSize < getQueue().remainingCapacity() * 0.8;
            
            return new ThreadPoolHealth(
                name,
                healthy,
                poolSize,
                activeThreads,
                queueSize,
                totalTasks.get(),
                completedTasks.get(),
                rejectedTasks.get(),
                utilization
            );
        }
        
        public void optimize() {
            int currentMax = getMaximumPoolSize();
            int queueSize = getQueue().size();
            
            // Increase pool size if queue is building up
            if (queueSize > 10 && currentMax < maxThreadCount / threadPools.size()) {
                int newMax = Math.min(currentMax + 2, maxThreadCount / threadPools.size());
                setMaximumPoolSize(newMax);
                log.info("Increased max pool size for '{}' to {}", name, newMax);
            }
        }
        
        public void reduceThreads(double factor) {
            int currentCore = getCorePoolSize();
            int newCore = Math.max(1, (int) (currentCore * factor));
            setCorePoolSize(newCore);
            
            int currentMax = getMaximumPoolSize();
            int newMax = Math.max(newCore, (int) (currentMax * factor));
            setMaximumPoolSize(newMax);
            
            log.info("Reduced thread pool '{}' size: core {} -> {}, max {} -> {}",
                name, currentCore, newCore, currentMax, newMax);
        }
        
        private class OptimizedRejectionHandler implements RejectedExecutionHandler {
            private final String poolName;
            
            OptimizedRejectionHandler(String poolName) {
                this.poolName = poolName;
            }
            
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                rejectedTasks.incrementAndGet();
                
                if (!executor.isShutdown()) {
                    log.warn("Task rejected from pool '{}', attempting to run in caller thread", poolName);
                    
                    // Try to run in caller thread as last resort
                    if (!executor.isShutdown()) {
                        r.run();
                    }
                }
            }
        }
    }
    
    /**
     * Custom thread factory for better thread naming and configuration.
     */
    public static class OptimizedThreadFactory implements ThreadFactory {
        private final String namePrefix;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        
        public OptimizedThreadFactory(String poolName) {
            group = Thread.currentThread().getThreadGroup();
            namePrefix = poolName + "-thread-";
        }
        
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            
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
    
    /**
     * Thread pool health metrics.
     */
    public record ThreadPoolHealth(
        String name,
        boolean healthy,
        int poolSize,
        int activeThreads,
        int queueSize,
        long totalTasks,
        long completedTasks,
        long rejectedTasks,
        double utilization
    ) {
        public boolean isHealthy() {
            return healthy;
        }
        
        @Override
        public String toString() {
            return String.format(
                "Pool[size=%d, active=%d, queue=%d, tasks=%d/%d, rejected=%d, util=%.1f%%]",
                poolSize, activeThreads, queueSize, completedTasks, totalTasks, 
                rejectedTasks, utilization * 100
            );
        }
    }
}