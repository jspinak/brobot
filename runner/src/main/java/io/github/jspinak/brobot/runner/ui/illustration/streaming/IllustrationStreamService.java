package io.github.jspinak.brobot.runner.ui.illustration.streaming;

import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.ui.illustration.IllustrationMetadata;
import io.github.jspinak.brobot.tools.history.configuration.IllustrationContext;

import lombok.Getter;

/**
 * Service for streaming real-time illustrations from the automation engine.
 *
 * <p>This service bridges the library's illustration system with the Desktop Runner's UI, providing
 * real-time updates as actions are executed. It handles:
 *
 * <ul>
 *   <li>Conversion from library formats (Mat) to JavaFX formats (Image)
 *   <li>Thread-safe queueing and processing
 *   <li>Performance optimization to prevent UI blocking
 *   <li>Event broadcasting for UI components
 * </ul>
 *
 * @see IllustrationStreamListener
 * @see IllustrationStreamEvent
 */
@Service
@Getter
public class IllustrationStreamService {

    private final EventBus eventBus;
    private final Queue<IllustrationStreamData> streamQueue = new ConcurrentLinkedQueue<>();
    private final ExecutorService processingExecutor = Executors.newSingleThreadExecutor();
    private final BooleanProperty streamingEnabled = new SimpleBooleanProperty(true);

    // Performance settings
    private static final int MAX_QUEUE_SIZE = 50;
    private static final long MIN_INTERVAL_MS = 100; // Minimum time between UI updates

    private long lastUpdateTime = 0;
    private Consumer<IllustrationStreamEvent> streamConsumer;

    @Autowired
    public IllustrationStreamService(EventBus eventBus) {
        this.eventBus = eventBus;
        startProcessingThread();
    }

    /**
     * Receives illustration data from the library and queues it for processing.
     *
     * @param mat the OpenCV Mat containing the illustration
     * @param actionResult the action result associated with the illustration
     * @param context the illustration context
     */
    public void receiveIllustration(
            Mat mat, ActionResult actionResult, IllustrationContext context) {
        if (!streamingEnabled.get()) {
            return;
        }

        // Create stream data
        IllustrationStreamData data =
                IllustrationStreamData.builder()
                        .id(UUID.randomUUID().toString())
                        .timestamp(LocalDateTime.now())
                        .mat(mat.clone()) // Clone to avoid memory issues
                        .actionResult(actionResult)
                        .context(context)
                        .build();

        // Add to queue with size limit
        if (streamQueue.size() >= MAX_QUEUE_SIZE) {
            streamQueue.poll(); // Remove oldest
        }
        streamQueue.offer(data);
    }

    /**
     * Sets the consumer for stream events.
     *
     * @param consumer the consumer to handle stream events
     */
    public void setStreamConsumer(Consumer<IllustrationStreamEvent> consumer) {
        this.streamConsumer = consumer;
    }

    /** Starts streaming illustrations. */
    public void startStreaming() {
        streamingEnabled.set(true);
    }

    /** Stops streaming illustrations. */
    public void stopStreaming() {
        streamingEnabled.set(false);
        streamQueue.clear();
    }

    /**
     * Gets the current queue size.
     *
     * @return number of illustrations in queue
     */
    public int getQueueSize() {
        return streamQueue.size();
    }

    /** Clears the stream queue. */
    public void clearQueue() {
        streamQueue.clear();
    }

    /** Starts the background thread for processing illustrations. */
    private void startProcessingThread() {
        processingExecutor.submit(
                () -> {
                    while (!Thread.currentThread().isInterrupted()) {
                        try {
                            processQueue();
                            Thread.sleep(50); // Check queue every 50ms
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                });
    }

    /** Processes illustrations from the queue. */
    private void processQueue() {
        IllustrationStreamData data = streamQueue.poll();
        if (data == null) {
            return;
        }

        // Rate limiting
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime < MIN_INTERVAL_MS) {
            // Re-queue if too soon
            streamQueue.offer(data);
            return;
        }

        try {
            // Convert Mat to JavaFX Image
            Image image = convertMatToImage(data.getMat());

            // Create metadata
            IllustrationMetadata metadata = createMetadata(data);

            // Create stream event
            IllustrationStreamEvent event =
                    IllustrationStreamEvent.builder()
                            .id(data.getId())
                            .timestamp(data.getTimestamp())
                            .image(image)
                            .metadata(metadata)
                            .queueSize(streamQueue.size())
                            .build();

            // Dispatch to UI thread
            Platform.runLater(
                    () -> {
                        // Notify consumer
                        if (streamConsumer != null) {
                            streamConsumer.accept(event);
                        }

                        // Broadcast event
                        eventBus.publish(
                                new io.github.jspinak.brobot.runner.events.UIUpdateEvent(
                                        this, "ILLUSTRATION_CAPTURED", event));
                    });

            lastUpdateTime = currentTime;

        } catch (Exception e) {
            // Log error but continue processing
            System.err.println("Error processing illustration: " + e.getMessage());
        } finally {
            // Clean up Mat memory
            if (data.getMat() != null) {
                data.getMat().release();
            }
        }
    }

    /**
     * Converts OpenCV Mat to JavaFX Image.
     *
     * @param mat the OpenCV Mat
     * @return JavaFX Image
     */
    private Image convertMatToImage(Mat mat) throws Exception {
        // Convert Mat to BufferedImage
        BufferedImage bufferedImage = matToBufferedImage(mat);

        // Convert BufferedImage to JavaFX Image
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }

    /**
     * Converts OpenCV Mat to BufferedImage.
     *
     * @param mat the OpenCV Mat
     * @return BufferedImage
     */
    private BufferedImage matToBufferedImage(Mat mat) throws Exception {
        int type;

        // Determine BufferedImage type based on Mat channels
        if (mat.channels() == 1) {
            type = BufferedImage.TYPE_BYTE_GRAY;
        } else if (mat.channels() == 3) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        } else if (mat.channels() == 4) {
            type = BufferedImage.TYPE_4BYTE_ABGR;
        } else {
            throw new IllegalArgumentException("Unsupported number of channels: " + mat.channels());
        }

        // Create BufferedImage
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);

        // Get data from Mat
        byte[] data = new byte[(int) (mat.total() * mat.channels())];
        mat.data().get(data);

        // Copy data to BufferedImage
        if (type == BufferedImage.TYPE_BYTE_GRAY) {
            // For grayscale images
            image.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), data);
        } else {
            // For color images, need to convert from BGR(A) to RGB(A)
            int[] pixels = new int[mat.cols() * mat.rows()];

            for (int i = 0; i < pixels.length; i++) {
                int base = i * mat.channels();
                if (mat.channels() == 3) {
                    // BGR to RGB
                    int b = data[base] & 0xFF;
                    int g = data[base + 1] & 0xFF;
                    int r = data[base + 2] & 0xFF;
                    pixels[i] = (0xFF << 24) | (r << 16) | (g << 8) | b;
                } else if (mat.channels() == 4) {
                    // BGRA to RGBA
                    int b = data[base] & 0xFF;
                    int g = data[base + 1] & 0xFF;
                    int r = data[base + 2] & 0xFF;
                    int a = data[base + 3] & 0xFF;
                    pixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
                }
            }

            image.setRGB(0, 0, mat.cols(), mat.rows(), pixels, 0, mat.cols());
        }

        return image;
    }

    /**
     * Creates metadata from stream data.
     *
     * @param data the stream data
     * @return illustration metadata
     */
    private IllustrationMetadata createMetadata(IllustrationStreamData data) {
        IllustrationMetadata.IllustrationMetadataBuilder builder =
                IllustrationMetadata.builder()
                        .id(data.getId())
                        .timestamp(data.getTimestamp())
                        .success(
                                data.getActionResult() != null
                                        && data.getActionResult().isSuccess());

        // Extract action type from context
        if (data.getContext() != null) {
            builder.actionType(data.getContext().getCurrentAction().toString());

            // Add active states
            if (data.getContext().getActiveStates() != null) {
                builder.stateName(String.join(", ", data.getContext().getActiveStates()));
            }
        }

        // Extract matches from action result
        if (data.getActionResult() != null) {
            data.getActionResult()
                    .getMatchList()
                    .forEach(
                            match -> {
                                builder.match(
                                        IllustrationMetadata.Match.builder()
                                                .x(match.x())
                                                .y(match.y())
                                                .width(match.w())
                                                .height(match.h())
                                                .similarity(match.getScore())
                                                .objectName(match.getName())
                                                .build());
                            });

            // Add performance data
            builder.performanceData(
                    IllustrationMetadata.PerformanceData.builder()
                            .executionTimeMs(data.getActionResult().getDuration().toMillis())
                            .matchesFound(data.getActionResult().getMatchList().size())
                            .averageSimilarity(
                                    data.getActionResult().getMatchList().stream()
                                            .mapToDouble(m -> m.getScore())
                                            .average()
                                            .orElse(0.0))
                            .build());
        }

        return builder.build();
    }

    /** Shuts down the service and releases resources. */
    public void shutdown() {
        streamingEnabled.set(false);
        streamQueue.clear();
        processingExecutor.shutdown();
    }
}
