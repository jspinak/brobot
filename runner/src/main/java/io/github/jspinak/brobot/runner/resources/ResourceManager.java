package io.github.jspinak.brobot.runner.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.annotation.PreDestroy;

import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Component
@Data
@EqualsAndHashCode(exclude = {"managedResources", "resourceTracker"})
public class ResourceManager {
    private final EventBus eventBus;
    private final CopyOnWriteArrayList<AutoCloseable> managedResources =
            new CopyOnWriteArrayList<>();
    private final Map<Object, String> resourceTracker = new WeakHashMap<>();
    private final AtomicInteger resourceCount = new AtomicInteger(0);

    public ResourceManager(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public <T extends AutoCloseable> T registerResource(T resource, String description) {
        managedResources.add(resource);
        resourceTracker.put(resource, description);
        int count = resourceCount.incrementAndGet();
        eventBus.publish(
                LogEvent.debug(
                        this,
                        String.format("Resource registered: %s (total: %d)", description, count),
                        "Resources"));
        return resource;
    }

    public void releaseResource(AutoCloseable resource) {
        try {
            String description =
                    resourceTracker.getOrDefault(resource, resource.getClass().getSimpleName());
            resource.close();
            managedResources.remove(resource);
            int count = resourceCount.decrementAndGet();
            eventBus.publish(
                    LogEvent.debug(
                            this,
                            String.format(
                                    "Resource released: %s (remaining: %d)", description, count),
                            "Resources"));
        } catch (Exception e) {
            eventBus.publish(
                    LogEvent.error(
                            this, "Failed to release resource: " + e.getMessage(), "Resources", e));
        }
    }

    public void releaseMatResources(Mat... mats) {
        for (Mat mat : mats) {
            if (mat != null && !mat.isNull()) {
                try {
                    mat.release();
                    eventBus.publish(
                            LogEvent.debug(this, "OpenCV Mat resource released", "Resources"));
                } catch (Exception e) {
                    eventBus.publish(
                            LogEvent.error(
                                    this,
                                    "Failed to release Mat resource: " + e.getMessage(),
                                    "Resources",
                                    e));
                }
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        eventBus.publish(
                LogEvent.info(
                        this,
                        "Starting resource cleanup of " + managedResources.size() + " resources...",
                        "Resources"));

        // Clone the list to avoid concurrent modification issues
        List<AutoCloseable> resourcesToClose = new ArrayList<>(managedResources);

        // Close in reverse order (LIFO)
        for (int i = resourcesToClose.size() - 1; i >= 0; i--) {
            AutoCloseable resource = resourcesToClose.get(i);
            try {
                String description =
                        resourceTracker.getOrDefault(resource, resource.getClass().getSimpleName());
                resource.close();
                managedResources.remove(resource);
                eventBus.publish(
                        LogEvent.debug(this, "Resource cleaned up: " + description, "Resources"));
            } catch (Exception e) {
                eventBus.publish(
                        LogEvent.error(
                                this,
                                "Error during resource cleanup: " + e.getMessage(),
                                "Resources",
                                e));
            }
        }

        // Ensure all resources are cleared, even if individual remove operations failed
        managedResources.clear();
        resourceCount.set(0);

        eventBus.publish(
                LogEvent.info(
                        this,
                        "Resource cleanup completed, remaining resources: "
                                + managedResources.size(),
                        "Resources"));
    }

    public int getResourceCount() {
        return resourceCount.get();
    }
}
