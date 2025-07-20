package io.github.jspinak.brobot.runner.ui.icons.services;

import javafx.application.Platform;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Service for rendering icons on JavaFX canvas.
 * Handles thread safety and snapshot generation.
 */
@Slf4j
@Service
public class IconRendererService {
    
    /**
     * Render configuration.
     */
    public static class RenderConfiguration {
        private Color defaultStrokeColor = Color.web("#007ACC");
        private Color defaultFillColor = Color.web("#007ACC");
        private double defaultLineWidth = 2.0;
        private Color backgroundColor = Color.TRANSPARENT;
        private boolean antialiasing = true;
        private long renderTimeout = 5000; // milliseconds
        
        public static RenderConfigurationBuilder builder() {
            return new RenderConfigurationBuilder();
        }
        
        public static class RenderConfigurationBuilder {
            private RenderConfiguration config = new RenderConfiguration();
            
            public RenderConfigurationBuilder defaultStrokeColor(Color color) {
                config.defaultStrokeColor = color;
                return this;
            }
            
            public RenderConfigurationBuilder defaultFillColor(Color color) {
                config.defaultFillColor = color;
                return this;
            }
            
            public RenderConfigurationBuilder defaultLineWidth(double width) {
                config.defaultLineWidth = width;
                return this;
            }
            
            public RenderConfigurationBuilder backgroundColor(Color color) {
                config.backgroundColor = color;
                return this;
            }
            
            public RenderConfigurationBuilder antialiasing(boolean enable) {
                config.antialiasing = enable;
                return this;
            }
            
            public RenderConfigurationBuilder renderTimeout(long timeout) {
                config.renderTimeout = timeout;
                return this;
            }
            
            public RenderConfiguration build() {
                return config;
            }
        }
    }
    
    private RenderConfiguration configuration = RenderConfiguration.builder().build();
    
    /**
     * Sets the render configuration.
     */
    public void setConfiguration(RenderConfiguration configuration) {
        this.configuration = configuration;
        log.debug("Icon renderer configured");
    }
    
    /**
     * Renders an icon using the provided drawing function.
     */
    public CompletableFuture<Image> renderIcon(int size, IconDrawer drawer) {
        CompletableFuture<Image> future = new CompletableFuture<>();
        
        if (Platform.isFxApplicationThread()) {
            try {
                Image icon = renderIconInternal(size, drawer);
                future.complete(icon);
            } catch (Exception e) {
                log.error("Error rendering icon", e);
                future.completeExceptionally(e);
            }
        } else {
            Platform.runLater(() -> {
                try {
                    Image icon = renderIconInternal(size, drawer);
                    future.complete(icon);
                } catch (Exception e) {
                    log.error("Error rendering icon", e);
                    future.completeExceptionally(e);
                }
            });
        }
        
        // Apply timeout
        return future.orTimeout(configuration.renderTimeout, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Renders an icon synchronously (must be on FX thread).
     */
    public Image renderIconSync(int size, IconDrawer drawer) {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException("renderIconSync must be called on JavaFX thread");
        }
        
        return renderIconInternal(size, drawer);
    }
    
    /**
     * Creates a placeholder icon.
     */
    public Image createPlaceholderIcon(int size) {
        WritableImage image = new WritableImage(size, size);
        // Return empty transparent image as placeholder
        return image;
    }
    
    /**
     * Internal icon rendering logic.
     */
    private Image renderIconInternal(int size, IconDrawer drawer) {
        Canvas canvas = new Canvas(size, size);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        // Set up graphics context
        setupGraphicsContext(gc, size);
        
        // Let the drawer draw the icon
        drawer.drawIcon(gc, size);
        
        // Create snapshot
        return createSnapshot(canvas);
    }
    
    /**
     * Sets up the graphics context with default settings.
     */
    private void setupGraphicsContext(GraphicsContext gc, int size) {
        // Clear background
        gc.clearRect(0, 0, size, size);
        
        // Set default properties
        gc.setLineWidth(configuration.defaultLineWidth);
        gc.setStroke(configuration.defaultStrokeColor);
        gc.setFill(configuration.defaultFillColor);
        
        // Apply antialiasing settings
        if (configuration.antialiasing) {
            gc.setImageSmoothing(true);
        }
    }
    
    /**
     * Creates a snapshot of the canvas.
     */
    private Image createSnapshot(Canvas canvas) {
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(configuration.backgroundColor);
        
        WritableImage image = canvas.snapshot(params, null);
        log.trace("Created snapshot for icon, size: {}x{}", image.getWidth(), image.getHeight());
        
        return image;
    }
    
    /**
     * Functional interface for icon drawing.
     */
    @FunctionalInterface
    public interface IconDrawer {
        void drawIcon(GraphicsContext gc, int size);
    }
    
    /**
     * Renders multiple icons in batch.
     */
    public CompletableFuture<Map<String, Image>> renderBatch(Map<String, IconRequest> requests) {
        CompletableFuture<Map<String, Image>> future = new CompletableFuture<>();
        Map<String, Image> results = new ConcurrentHashMap<>();
        
        Platform.runLater(() -> {
            try {
                for (Map.Entry<String, IconRequest> entry : requests.entrySet()) {
                    String key = entry.getKey();
                    IconRequest request = entry.getValue();
                    Image icon = renderIconInternal(request.size, request.drawer);
                    results.put(key, icon);
                }
                future.complete(results);
            } catch (Exception e) {
                log.error("Error in batch rendering", e);
                future.completeExceptionally(e);
            }
        });
        
        return future;
    }
    
    /**
     * Icon request for batch rendering.
     */
    public static class IconRequest {
        private final int size;
        private final IconDrawer drawer;
        
        public IconRequest(int size, IconDrawer drawer) {
            this.size = size;
            this.drawer = drawer;
        }
        
        public int getSize() { return size; }
        public IconDrawer getDrawer() { return drawer; }
    }
    
    /**
     * Gets the current configuration.
     */
    public RenderConfiguration getConfiguration() {
        return configuration;
    }
}