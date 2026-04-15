package com.hotel.util;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import java.util.concurrent.Callable;

/**
 * A generic, reusable JavaFX {@link Service} that runs any database or I/O
 * operation on a background daemon thread and delivers the result safely back
 * to the JavaFX Application Thread through the standard Task lifecycle
 * callbacks: {@code setOnSucceeded}, {@code setOnFailed}, {@code setOnCancelled}.
 *
 * <p>Usage example:
 * <pre>{@code
 *   AsyncLoader<List<Reservation>> loader = new AsyncLoader<>(resDAO::findAll);
 *   loader.setOnSucceeded(e -> table.getItems().setAll(loader.getValue()));
 *   loader.setOnFailed(e -> showError(loader.getException()));
 *   loader.start();
 * }</pre>
 *
 * @param <T> the type of result produced by the background operation
 */
public class AsyncLoader<T> extends Service<T> {

    private final Callable<T> loader;

    /**
     * @param loader a {@link Callable} whose {@code call()} method will run
     *               entirely off the JavaFX Application Thread
     */
    public AsyncLoader(Callable<T> loader) {
        this.loader = loader;
    }

    /**
     * Called by the Service framework each time the service is (re)started.
     * Returns a fresh {@link Task} that delegates to the stored {@link Callable}.
     */
    @Override
    protected Task<T> createTask() {
        return new Task<>() {
            @Override
            protected T call() throws Exception {
                return loader.call();
            }
        };
    }
}
