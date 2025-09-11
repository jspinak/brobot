package io.github.jspinak.brobot.runner.performance.thread;

/**
 * Thread pool health metrics and status information.
 *
 * <p>This record provides a snapshot of a thread pool's current state including size, activity, and
 * performance metrics.
 *
 * @since 1.0.0
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
        double utilization,
        long avgTaskDuration,
        int largestPoolSize) {

    /**
     * Check if the pool is healthy.
     *
     * @return true if healthy
     */
    public boolean isHealthy() {
        return healthy;
    }

    /**
     * Calculate task success rate.
     *
     * @return success rate as percentage
     */
    public double getSuccessRate() {
        if (totalTasks == 0) {
            return 100.0;
        }
        return ((totalTasks - rejectedTasks) * 100.0) / totalTasks;
    }

    /**
     * Check if pool is under heavy load.
     *
     * @return true if utilization > 80% or queue is filling up
     */
    public boolean isUnderLoad() {
        return utilization > 0.8 || (queueSize > 0 && activeThreads == poolSize);
    }

    /**
     * Get a health status description.
     *
     * @return status description
     */
    public String getStatus() {
        if (!healthy) {
            return "UNHEALTHY";
        } else if (isUnderLoad()) {
            return "UNDER_LOAD";
        } else if (utilization < 0.2) {
            return "IDLE";
        } else {
            return "HEALTHY";
        }
    }

    @Override
    public String toString() {
        return String.format(
                "Pool[name=%s, status=%s, size=%d, active=%d, queue=%d, "
                        + "tasks=%d/%d, rejected=%d, util=%.1f%%, avgDuration=%dms]",
                name,
                getStatus(),
                poolSize,
                activeThreads,
                queueSize,
                completedTasks,
                totalTasks,
                rejectedTasks,
                utilization * 100,
                avgTaskDuration);
    }

    /**
     * Create a health snapshot indicating an unhealthy state.
     *
     * @param name pool name
     * @param reason reason for unhealthy state
     * @return unhealthy health snapshot
     */
    public static ThreadPoolHealth unhealthy(String name, String reason) {
        return new ThreadPoolHealth(name, false, 0, 0, 0, 0, 0, 0, 0.0, 0, 0);
    }
}
