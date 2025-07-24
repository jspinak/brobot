---
sidebar_position: 10
title: 'Enhanced Illustration Features'
---

# Enhanced Illustration Features

The Desktop Runner extends Brobot's core illustration capabilities with advanced visualization, analytics, and sharing features designed for comprehensive automation analysis and documentation.

## Overview

The enhanced illustration system in Desktop Runner provides:

- **Interactive Viewer** - Zoom, pan, and layer control for detailed analysis
- **Real-time Streaming** - Live visualization during automation execution
- **Analytics Dashboard** - Performance metrics and pattern analysis
- **Web Gallery** - Shareable HTML galleries for documentation
- **Export & Sharing** - Multiple export formats and sharing options

## Interactive Illustration Viewer

### Features

The interactive viewer provides rich visualization capabilities:

```java
// Using the illustration viewer
IllustrationViewer viewer = new IllustrationViewer();

// Load an illustration with metadata
viewer.loadIllustration(image, metadata);

// Control layers
viewer.addLayer(new IllustrationLayer("Search Regions", 1));
viewer.addLayer(new IllustrationLayer("Matches", 2));
viewer.addLayer(new IllustrationLayer("Actions", 3));

// Zoom and pan programmatically
viewer.setZoomLevel(2.0); // 200% zoom
viewer.setPanX(100);
viewer.setPanY(50);

// Export current view
viewer.export(exportedImage -> {
    // Handle exported image
    saveImage(exportedImage);
});
```

### Layer Management

Layers enable composition of complex visualizations:

```java
IllustrationLayer matchLayer = new IllustrationLayer("Matches", 2);
matchLayer.setDrawingConsumer(gc -> {
    // Custom drawing logic
    gc.setStroke(Color.MAGENTA);
    gc.setLineWidth(3.0);
    matches.forEach(match -> 
        gc.strokeRect(match.getX(), match.getY(), 
                     match.getWidth(), match.getHeight())
    );
});
matchLayer.setOpacity(0.8); // Semi-transparent
viewer.addLayer(matchLayer);
```

### User Interactions

- **Mouse Controls**:
  - Left-click drag: Pan the view
  - Scroll wheel: Zoom in/out around cursor
  - Double-click: Fit illustration to canvas
  
- **Keyboard Shortcuts**:
  - `+`/`-`: Zoom in/out
  - Arrow keys: Pan view
  - `Space`: Toggle layer visibility
  - `Esc`: Reset view

## Real-time Illustration Streaming

### Stream Service Configuration

```java
@Configuration
public class StreamConfig {
    
    @Bean
    public IllustrationStreamService streamService(EventBus eventBus) {
        IllustrationStreamService service = new IllustrationStreamService(eventBus);
        
        // Configure stream consumer
        service.setStreamConsumer(event -> {
            // Process stream events
            updateUI(event.getImage(), event.getMetadata());
        });
        
        return service;
    }
}
```

### Stream Panel Usage

The stream panel provides live visualization during automation:

```java
// Create stream panel
IllustrationStreamPanel streamPanel = new IllustrationStreamPanel(
    streamService, eventBus);

// Configure filtering
streamPanel.setFilter(FilterMode.FAILURES_ONLY);

// Control streaming
streamPanel.pause(); // Pause updates
streamPanel.resume(); // Resume updates
streamPanel.clearHistory(); // Clear history
```

### Performance Optimization

The streaming system includes automatic performance optimization:

- **Queue Management**: Limits queue size to prevent memory issues
- **Rate Limiting**: Minimum interval between UI updates
- **Priority Processing**: High-priority events (errors) processed first
- **Automatic Throttling**: Reduces update frequency under high load

## Analytics Dashboard

### Dashboard Components

The analytics dashboard provides comprehensive insights:

1. **Summary Metrics**
   - Total illustrations generated
   - Overall success rate
   - Average processing time
   - Queue health status

2. **Charts and Visualizations**
   - Throughput over time (line chart)
   - Action distribution (pie chart)
   - Success rates by action (bar chart)
   - Performance trends (multi-series line chart)

3. **Detailed Tables**
   - Action statistics with counts and timings
   - Quality metrics with trends
   - Session summaries

### Using the Analytics Service

```java
@Service
public class AnalyticsExample {
    
    @Autowired
    private IllustrationAnalyticsService analyticsService;
    
    public void recordEvent(IllustrationStreamEvent event) {
        // Record illustration event
        analyticsService.recordIllustrationEvent(event);
        
        // Get current metrics
        AnalyticsSnapshot snapshot = analyticsService.getCurrentSnapshot();
        
        // Access specific metrics
        double successRate = snapshot.getOverallSuccessRate();
        double avgProcessingTime = snapshot.getAverageProcessingTime();
        Map<String, Double> successByAction = snapshot.getSuccessRatesByAction();
        
        // Monitor performance
        if (snapshot.getCurrentQueueSize() > 50) {
            log.warn("High queue size: {}", snapshot.getCurrentQueueSize());
        }
    }
}
```

### Custom Analytics

Extend analytics with custom metrics:

```java
// Track custom metrics
analyticsService.recordCustomMetric("custom_quality_score", 0.95);

// Query historical data
List<AnalyticsSnapshot> history = analyticsService.getHistoricalSnapshots(
    LocalDateTime.now().minusHours(1), 
    LocalDateTime.now()
);

// Export analytics data
analyticsService.exportMetrics("analytics_report.csv");
```

## Web-based Illustration Gallery

### Gallery Service Features

The gallery service provides persistent storage and organization:

```java
@Service
public class GalleryUsage {
    
    @Autowired
    private IllustrationGalleryService galleryService;
    
    public void saveToGallery(Image image, IllustrationMetadata metadata) {
        // Save illustration
        IllustrationEntity entity = galleryService.saveIllustration(
            image, metadata, "session-123");
        
        // Add tags for organization
        galleryService.addTags(entity.getId(), 
            Set.of("login-flow", "critical", "regression"));
        
        // Search illustrations
        List<IllustrationEntity> results = galleryService.searchIllustrations(
            SearchCriteria.builder()
                .actionTypes(Set.of("CLICK", "TYPE"))
                .successOnly(false) // Find failures
                .startDate(LocalDateTime.now().minusDays(7))
                .tags(Set.of("critical"))
                .maxResults(50)
                .build()
        );
    }
}
```

### Web Gallery Export

Generate static HTML galleries for sharing:

```java
// Export session as web gallery
Path galleryPath = galleryService.exportWebGallery(
    "session-123", 
    "/path/to/export/gallery"
);

// The gallery includes:
// - Responsive grid layout
// - Filtering by action type and status
// - Lightbox for full-size viewing
// - Search functionality
// - Timeline view
```

### Gallery Management

```java
// Get gallery statistics
GalleryStatistics stats = galleryService.getStatistics();
System.out.println("Total illustrations: " + stats.getTotalIllustrations());
System.out.println("Storage used: " + stats.getStorageUsedMB() + " MB");

// Cleanup old illustrations
galleryService.cleanupOldIllustrations(); // Respects max gallery size

// Bulk operations
List<Long> illustrationIds = Arrays.asList(1L, 2L, 3L);
galleryService.bulkAddTags(illustrationIds, Set.of("reviewed"));
galleryService.bulkDelete(illustrationIds);
```

## Export and Sharing Features

### Export Formats

The export service supports multiple formats:

1. **Image Formats**
   - PNG (original quality)
   - JPEG (compressed)

2. **Document Formats**
   - PDF (printable reports)
   - Markdown (documentation)

3. **Archive Formats**
   - ZIP (batch export with metadata)

4. **Future Formats**
   - Video (action sequence compilation)
   - GIF (animated sequences)

### Export Service Usage

```java
@Service
public class ExportExample {
    
    @Autowired
    private IllustrationExportService exportService;
    
    public void exportIllustrations() {
        // Single export
        Path exported = exportService.exportSingle(
            illustrationId, 
            ExportFormat.PNG, 
            "/path/to/output.png"
        );
        
        // Batch export as ZIP
        List<Long> ids = Arrays.asList(1L, 2L, 3L, 4L, 5L);
        Path zipPath = exportService.exportBatch(ids, "/path/to/archive.zip");
        
        // Export as Markdown report
        Path mdPath = exportService.exportAsMarkdown(
            "session-123", 
            "/path/to/report.md", 
            true // Include embedded images
        );
        
        // Generate PDF report
        List<IllustrationEntity> illustrations = galleryService.getSessionIllustrations("session-123");
        Path pdfPath = exportService.exportAsPdf(illustrations, Paths.get("/path/to/report.pdf"));
    }
}
```

### Sharing Capabilities

```java
// Create shareable link
String shareLink = exportService.createShareableLink("session-123");
// Returns: http://localhost:8080/gallery/share/uuid

// Configure sharing options
ShareOptions options = ShareOptions.builder()
    .expirationHours(48)
    .passwordProtected(true)
    .password("secure123")
    .allowDownload(true)
    .build();

String secureLink = exportService.createShareableLink("session-123", options);
```

## Integration with Core Library

### Bridging Library and Runner

The Desktop Runner seamlessly integrates with the library's illustration system:

```java
@Component
public class IllustrationBridge {
    
    @Autowired
    private IllustrationStreamService streamService;
    
    @EventListener
    public void onLibraryIllustration(LibraryIllustrationEvent event) {
        // Convert library format to runner format
        Mat mat = event.getMat();
        ActionResult result = event.getActionResult();
        IllustrationContext context = event.getContext();
        
        // Forward to stream service
        streamService.receiveIllustration(mat, result, context);
    }
}
```

### Configuration Synchronization

Keep library and runner configurations in sync:

```java
@Configuration
public class IllustrationSyncConfig {
    
    @Bean
    public IllustrationConfig illustrationConfig() {
        // This config is used by both library and runner
        return IllustrationConfig.builder()
            .globalEnabled(true)
            .actionEnabled(Action.FIND, true)
            .actionEnabled(Action.CLICK, true)
            .samplingRate(Action.MOVE, 0.1)
            .qualityThreshold(0.8)
            .batchConfig(BatchConfig.builder()
                .maxBatchSize(20)
                .flushInterval(Duration.ofSeconds(5))
                .build())
            .build();
    }
}
```

## Best Practices

### Performance Optimization

1. **Use Appropriate Sampling Rates**
   ```java
   // High-frequency actions need lower sampling
   config.samplingRate(Action.MOVE, 0.05); // 5% sampling
   config.samplingRate(Action.FIND, 0.5);  // 50% sampling
   ```

2. **Enable Batching for Bulk Operations**
   ```java
   config.batchConfig(BatchConfig.builder()
       .maxBatchSize(50)
       .flushOnStateTransition(true)
       .build());
   ```

3. **Monitor Resource Usage**
   ```java
   if (analyticsService.getCurrentSnapshot().getCurrentQueueSize() > 100) {
       streamService.stopStreaming();
       // Process queue before resuming
   }
   ```

### Quality Management

1. **Set Appropriate Quality Thresholds**
   ```java
   config.qualityThreshold(0.75); // Only illustrate high-quality matches
   ```

2. **Use Context Filters**
   ```java
   config.contextFilter("important_only", context -> 
       context.isFirstExecution() || 
       !context.getLastActionResult().isSuccess() ||
       context.getPriority() == Priority.HIGH
   );
   ```

### Organization and Documentation

1. **Use Meaningful Tags**
   ```java
   galleryService.addTags(illustrationId, Set.of(
       "feature-login",
       "test-regression", 
       "priority-high",
       "sprint-42"
   ));
   ```

2. **Regular Cleanup**
   ```java
   // Schedule periodic cleanup
   @Scheduled(cron = "0 0 2 * * ?") // 2 AM daily
   public void cleanupGallery() {
       galleryService.cleanupOldIllustrations();
   }
   ```

3. **Export Documentation**
   ```java
   // Generate weekly reports
   @Scheduled(cron = "0 0 0 ? * MON") // Weekly on Monday
   public void generateWeeklyReport() {
       exportService.exportAsMarkdown(
           getCurrentWeekSession(),
           "reports/week-" + LocalDate.now() + ".md",
           true
       );
   }
   ```

## Troubleshooting

### Common Issues

1. **High Memory Usage**
   - Reduce `maxQueueSize` in stream service
   - Enable more aggressive batching
   - Lower quality threshold to reduce illustrations

2. **Slow UI Updates**
   - Increase `minIntervalMs` in stream service
   - Enable adaptive sampling
   - Reduce number of visible layers

3. **Export Failures**
   - Check disk space for large exports
   - Verify file permissions
   - Ensure illustrations exist in gallery

### Debug Mode

Enable detailed logging for troubleshooting:

```properties
# application.properties
logging.level.io.github.jspinak.brobot.runner.ui.illustration=DEBUG
brobot.illustration.debug=true
brobot.illustration.save-failed-exports=true
```

The enhanced illustration features in Desktop Runner provide powerful tools for visualizing, analyzing, and documenting your automation workflows, making it easier to debug issues, demonstrate functionality, and maintain high-quality automation solutions.