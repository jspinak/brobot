package io.github.jspinak.brobot.runner.util;

import lombok.Data;

import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Utility class for JavaFX thread handling.
 * Simplifies executing tasks on the JavaFX Application Thread and in background threads.
 */
@Data
public class FxThreadUtils {
    private static final Logger logger = LoggerFactory.getLogger(FxThreadUtils.class);

    private static final ExecutorService executor = Executors.newCachedThreadPool(r -> {
        Thread thread = new Thread(r, "FxThreadUtils-Worker");
        thread.setDaemon(true);
        return thread;
    });

    /**
     * Private constructor to prevent instantiation.
     */
    private FxThreadUtils() {
        // Utility class
    }

    /**
     * Runs a task on the JavaFX Application Thread and waits for completion.
     *
     * @param action The task to run
     * @param <T> The result type
     * @return The task result
     * @throws InterruptedException If the thread is interrupted
     * @throws ExecutionException If an error occurs during execution
     */
    public static <T> T runAndWait(Callable<T> action) throws InterruptedException, ExecutionException {
        if (Platform.isFxApplicationThread()) {
            try {
                return action.call();
            } catch (Exception e) {
                throw new ExecutionException(e);
            }
        }

        final CompletableFuture<T> future = new CompletableFuture<>();

        Platform.runLater(() -> {
            try {
                future.complete(action.call());
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });

        return future.get();
    }

    /**
     * Runs a task on the JavaFX Application Thread and waits for completion.
     *
     * @param action The task to run
     * @throws InterruptedException If the thread is interrupted
     * @throws ExecutionException If an error occurs during execution
     */
    public static void runAndWait(Runnable action) throws InterruptedException, ExecutionException {
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

        final CompletableFuture<Void> future = new CompletableFuture<>();

        Platform.runLater(() -> {
            try {
                action.run();
                future.complete(null);
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });

        future.get();
    }

    /**
     * Runs a task on the JavaFX Application Thread.
     *
     * @param action The task to run
     * @return A CompletableFuture that completes when the task is done
     */
    public static CompletableFuture<Void> runAsync(Runnable action) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        Platform.runLater(() -> {
            try {
                action.run();
                future.complete(null);
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });

        return future;
    }

    /**
     * Runs a task on the JavaFX Application Thread.
     *
     * @param action The task to run
     * @param <T> The result type
     * @return A CompletableFuture that completes with the task result
     */
    public static <T> CompletableFuture<T> runAsync(Callable<T> action) {
        CompletableFuture<T> future = new CompletableFuture<>();

        Platform.runLater(() -> {
            try {
                future.complete(action.call());
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });

        return future;
    }

    /**
     * Runs a task on a background thread.
     *
     * @param task The task to run
     * @return A CompletableFuture that completes with the task result
     * @param <T> The result type
     */
    public static <T> CompletableFuture<T> runInBackground(Callable<T> task) {
        CompletableFuture<T> future = new CompletableFuture<>();

        executor.submit(() -> {
            try {
                T result = task.call();
                future.complete(result);
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });

        return future;
    }

    /**
     * Runs a task on a background thread.
     *
     * @param task The task to run
     * @return A CompletableFuture that completes when the task is done
     */
    public static CompletableFuture<Void> runInBackground(Runnable task) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        executor.submit(() -> {
            try {
                task.run();
                future.complete(null);
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });

        return future;
    }

    /**
     * Runs a task on a background thread and updates the UI when done.
     *
     * @param backgroundTask The task to run in the background
     * @param uiConsumer The consumer that processes the result on the UI thread
     * @param <T> The result type
     * @return A CompletableFuture that completes when both background and UI tasks are done
     */
    public static <T> CompletableFuture<Void> runBackgroundWithUI(
            Supplier<T> backgroundTask, Consumer<T> uiConsumer) {

        return runInBackground(backgroundTask::get)
                .thenAcceptAsync(result -> {
                    Platform.runLater(() -> uiConsumer.accept(result));
                }, executor);
    }

    /**
     * Runs a task on a background thread and updates the UI when done.
     * Allows for different return types in the background and UI tasks.
     *
     * @param backgroundTask The task to run in the background
     * @param uiMapper The function that processes the result on the UI thread
     * @param <T> The background task result type
     * @param <R> The UI task result type
     * @return A CompletableFuture that completes with the final result
     */
    public static <T, R> CompletableFuture<R> runBackgroundWithUIMapper(
            Supplier<T> backgroundTask, Function<T, R> uiMapper) {

        CompletableFuture<R> future = new CompletableFuture<>();

        runInBackground(backgroundTask::get)
                .thenAccept(result -> {
                    Platform.runLater(() -> {
                        try {
                            R mappedResult = uiMapper.apply(result);
                            future.complete(mappedResult);
                        } catch (Throwable t) {
                            future.completeExceptionally(t);
                        }
                    });
                })
                .exceptionally(ex -> {
                    future.completeExceptionally(ex);
                    return null;
                });

        return future;
    }

    /**
     * Updates the value of a JavaFX property on the JavaFX Application Thread.
     *
     * @param property The property to update
     * @param value The new value
     * @param <T> The property value type
     */
    public static <T> void updateProperty(javafx.beans.property.Property<T> property, T value) {
        if (Platform.isFxApplicationThread()) {
            property.setValue(value);
        } else {
            Platform.runLater(() -> property.setValue(value));
        }
    }

    /**
     * Shuts down the executor service.
     */
    public static void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}