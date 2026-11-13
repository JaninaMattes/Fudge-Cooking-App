package com.foodtracker.foodtrackerapi.repositories;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import com.foodtracker.foodtrackerapi.common.utils.Attempt;

/**
 * <p>
 * The database operations that all repositories of this application need. Each
 * repository had its own copy of these operations before.
 * </p>
 *
 * <p>
 * All methods give a default value when the database operation fails. The
 * caller is a REST endpoint, and the client must receive an answer. The error
 * goes into the log.
 * </p>
 *
 * @author Janina Mattes
 */
abstract class AbstractJdbcRepository {

    /** Value that the methods give when a key is not available. */
    protected static final Integer NO_ID = 0;

    /** Connection to the database. */
    protected final JdbcTemplate jdbcTemplate;

    /**
     * Makes the repository.
     *
     * @param jdbcTemplate connection to the database
     */
    protected AbstractJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Sets the values of an {@code INSERT} statement.
     */
    @FunctionalInterface
    protected interface StatementBinder {

        /**
         * Sets the values.
         *
         * @param statement statement to fill
         * @throws SQLException if a value does not fit the column
         */
        void bind(PreparedStatement statement) throws SQLException;
    }

    /**
     * Puts one row into the database and gives the key that the sequence made.
     *
     * @param description short text for the log entry, for example
     *                    "create a product"
     * @param sql         {@code INSERT} statement
     * @param keyColumn   name of the key column, for example "PRODUCT_ID"
     * @param binder      sets the values of the statement
     * @return the new key, or {@link #NO_ID} if the insert failed
     */
    protected Integer insertAndGetKey(String description, String sql, String keyColumn,
            StatementBinder binder) {
        return Attempt.orDefault(description, NO_ID, () -> {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement statement = connection.prepareStatement(sql,
                        Statement.RETURN_GENERATED_KEYS);
                binder.bind(statement);
                return statement;
            }, keyHolder);
            return readKey(keyHolder, keyColumn);
        });
    }

    /**
     * Runs an {@code UPDATE}, an {@code INSERT} or a {@code DELETE} statement.
     *
     * @param description short text for the log entry
     * @param sql         statement to run
     * @param args        values of the statement
     * @return the number of rows that changed, or {@link #NO_ID} if the
     *         statement failed
     */
    protected Integer update(String description, String sql, Object... args) {
        return Attempt.orDefault(description, NO_ID, () -> jdbcTemplate.update(sql, args));
    }

    /**
     * Reads one whole number, for example a key or a count.
     *
     * @param description short text for the log entry
     * @param sql         {@code SELECT} statement that gives one number
     * @param args        values of the statement
     * @return the number, or {@link #NO_ID} if the statement gives no row
     */
    protected Integer queryForId(String description, String sql, Object... args) {
        return Attempt.orDefault(description, NO_ID,
                () -> jdbcTemplate.queryForObject(sql, Integer.class, args));
    }

    private Integer readKey(KeyHolder keyHolder, String keyColumn) {
        Map<String, Object> keys = keyHolder.getKeys();
        if (keys == null) {
            return NO_ID;
        }
        Object key = keys.get(keyColumn);
        return key instanceof Number ? ((Number) key).intValue() : NO_ID;
    }
}
