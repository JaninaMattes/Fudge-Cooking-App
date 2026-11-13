package com.foodtracker.foodtrackerapi.common.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * <p>
 * Runs an operation that can fail, writes a log entry if it fails and returns a
 * fallback value. Use it instead of a {@code try/catch} block that only prints
 * a stack trace.
 * </p>
 *
 * <p>
 * The database layer and the REST clients of this application must not stop the
 * caller when one single operation fails. Before this class, each method had its
 * own {@code try/catch} block. Many of these blocks called
 * {@code e.getStackTrace()}, which does nothing. The error was lost. This class
 * keeps the behavior (a fallback value) but makes the error visible in the log.
 * </p>
 *
 * <p>
 * Example:
 * </p>
 *
 * <pre>
 * return Attempt.orDefault("count the expiring products", 0,
 *         () -&gt; jdbcTemplate.queryForObject(SQL, Integer.class, userId));
 * </pre>
 *
 * @author Janina Mattes
 */
public final class Attempt {

    private static final Logger LOG = LogManager.getLogger(Attempt.class);

    private Attempt() {
        // Utility class. Do not make an instance of it.
    }

    /**
     * An operation that gives a result and can throw a checked exception.
     *
     * @param <T> type of the result
     */
    @FunctionalInterface
    public interface FailableSupplier<T> {

        /**
         * Does the operation.
         *
         * @return the result of the operation
         * @throws Exception if the operation fails
         */
        T get() throws Exception;
    }

    /**
     * An operation that gives no result and can throw a checked exception.
     */
    @FunctionalInterface
    public interface FailableRunnable {

        /**
         * Does the operation.
         *
         * @throws Exception if the operation fails
         */
        void run() throws Exception;
    }

    /**
     * Runs the operation and gives its result. If the operation throws, this
     * method writes a log entry and gives the fallback value.
     *
     * @param <T>         type of the result
     * @param description short text for the log entry, for example
     *                    "create a product"
     * @param fallback    value to give if the operation fails
     * @param operation   operation to run
     * @return the result of the operation, or the fallback value
     */
    public static <T> T orDefault(String description, T fallback, FailableSupplier<T> operation) {
        try {
            T result = operation.get();
            return result != null ? result : fallback;
        } catch (Exception e) {
            LOG.error("Failed to {}: {}", description, e.getMessage(), e);
            return fallback;
        }
    }

    /**
     * Runs the operation. If the operation throws, this method writes a log
     * entry and gives control back to the caller.
     *
     * @param description short text for the log entry
     * @param operation   operation to run
     * @return {@code true} if the operation was successful
     */
    public static boolean run(String description, FailableRunnable operation) {
        try {
            operation.run();
            return true;
        } catch (Exception e) {
            LOG.error("Failed to {}: {}", description, e.getMessage(), e);
            return false;
        }
    }
}
