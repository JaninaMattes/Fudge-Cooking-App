package com.foodtracker.foodtrackerapi.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.KeyHolder;

/**
 * <p>
 * A test double of the {@code JdbcTemplate}. It records each statement that a
 * repository runs and gives the answer that the test set before. The tests of
 * the repositories therefore need no database.
 * </p>
 *
 * <p>
 * The class is a test double and not a mock, because Mockito compares each
 * value of a variable argument list on its own. A stub for such a method is
 * long and difficult to read. This class keeps the tests short.
 * </p>
 *
 * @author Janina Mattes
 */
class RecordingJdbcTemplate extends JdbcTemplate {

    /** One statement that a repository ran. */
    static final class Call {

        private final String sql;
        private final List<Object> args;

        Call(String sql, Object... args) {
            this.sql = sql;
            this.args = args != null ? Arrays.asList(args) : Collections.emptyList();
        }

        /**
         * Gives the SQL statement.
         *
         * @return the SQL statement
         */
        String sql() {
            return sql;
        }

        /**
         * Gives the values of the statement.
         *
         * @return the values, in the order of the statement
         */
        List<Object> args() {
            return args;
        }

        @Override
        public String toString() {
            return sql + " " + args;
        }
    }

    /** All statements that the repository ran, in order. */
    private final List<Call> calls = new ArrayList<>();

    /** The statement that the last {@code INSERT} filled. */
    private final PreparedStatement statement = Mockito.mock(PreparedStatement.class);

    /** Row mapper of the last read operation. */
    private RowMapper<?> lastRowMapper;

    /** Answer of the next {@code queryForObject}. */
    private Object singleResult;

    /** Answer of the next {@code query}. */
    private List<?> listResult = Collections.emptyList();

    /** Answer of the next {@code update}. */
    private int updateResult = 1;

    /** Keys that the next {@code INSERT} gives back. */
    private Map<String, Object> generatedKeys = Collections.emptyMap();

    /** Error that each statement throws. It is {@code null} by default. */
    private RuntimeException failure;

    RecordingJdbcTemplate() {
        super();
    }

    // --- Settings of the test --------------------------------------------

    /**
     * Sets the answer of the next {@code queryForObject}.
     *
     * @param result object that the database gives back
     * @return this double, so that a test can chain the settings
     */
    RecordingJdbcTemplate givesObject(Object result) {
        this.singleResult = result;
        return this;
    }

    /**
     * Sets the answer of the next {@code query}.
     *
     * @param result rows that the database gives back
     * @return this double, so that a test can chain the settings
     */
    RecordingJdbcTemplate givesRows(List<?> result) {
        this.listResult = result;
        return this;
    }

    /**
     * Sets the number of rows that the next {@code update} changes.
     *
     * @param changedRows number of rows
     * @return this double, so that a test can chain the settings
     */
    RecordingJdbcTemplate givesChangedRows(int changedRows) {
        this.updateResult = changedRows;
        return this;
    }

    /**
     * Sets the keys that the next {@code INSERT} gives back.
     *
     * @param keys keys of the new row. The key of the map is the name of the
     *             column.
     * @return this double, so that a test can chain the settings
     */
    RecordingJdbcTemplate givesKeys(Map<String, Object> keys) {
        this.generatedKeys = keys;
        return this;
    }

    /**
     * Sets one key that the next {@code INSERT} gives back.
     *
     * @param column name of the key column
     * @param value  value of the key
     * @return this double, so that a test can chain the settings
     */
    RecordingJdbcTemplate givesKey(String column, Object value) {
        return givesKeys(Collections.singletonMap(column, value));
    }

    /**
     * Lets each statement throw. The test can then prove that the repository
     * gives a default value and does not stop the caller.
     *
     * @return this double, so that a test can chain the settings
     */
    RecordingJdbcTemplate fails() {
        return failsWith(new IllegalStateException("the database is not available"));
    }

    /**
     * Lets each statement throw the given error.
     *
     * @param error error that each statement throws
     * @return this double, so that a test can chain the settings
     */
    RecordingJdbcTemplate failsWith(RuntimeException error) {
        this.failure = error;
        return this;
    }

    // --- Results for the test --------------------------------------------

    /**
     * Gives all statements that the repository ran.
     *
     * @return the statements, in order
     */
    List<Call> calls() {
        return calls;
    }

    /**
     * Gives the statement that the repository ran last.
     *
     * @return the last statement
     */
    Call lastCall() {
        return calls.get(calls.size() - 1);
    }

    /**
     * Gives the values of the statement that the repository ran last.
     *
     * @return the values, in the order of the statement
     */
    List<Object> lastArgs() {
        return lastCall().args();
    }

    /**
     * Gives the SQL statement that the repository ran last.
     *
     * @return the last SQL statement
     */
    String lastSql() {
        return lastCall().sql();
    }

    /**
     * Gives the statement object that the last {@code INSERT} filled. The test
     * can prove with it that the repository wrote the values in the correct
     * order.
     *
     * @return the statement object
     */
    PreparedStatement statement() {
        return statement;
    }

    /**
     * Uses the row mapper of the last read operation on one row.
     *
     * @param <T>     type of the model object
     * @param columns values of the row. The key is the name of the column.
     * @return the model object that the mapper made
     * @throws SQLException if the result set cannot answer
     */
    @SuppressWarnings("unchecked")
    <T> T mapRow(Map<String, Object> columns) throws SQLException {
        return (T) ((RowMapper<T>) lastRowMapper).mapRow(resultSetWith(columns), 1);
    }

    /**
     * Makes a result set that gives the values of one row.
     *
     * @param values values of the row. The key is the name of the column.
     * @return the result set
     * @throws SQLException if the result set cannot answer
     */
    static ResultSet resultSetWith(Map<String, Object> values) throws SQLException {
        ResultSet resultSet = Mockito.mock(ResultSet.class);
        for (Map.Entry<String, Object> value : values.entrySet()) {
            String column = value.getKey();
            Object content = value.getValue();
            if (content instanceof Integer) {
                Mockito.when(resultSet.getInt(column)).thenReturn((Integer) content);
            } else if (content instanceof Float) {
                Mockito.when(resultSet.getFloat(column)).thenReturn((Float) content);
            } else if (content instanceof Boolean) {
                Mockito.when(resultSet.getBoolean(column)).thenReturn((Boolean) content);
            } else if (content instanceof byte[]) {
                Mockito.when(resultSet.getBytes(column)).thenReturn((byte[]) content);
            } else if (content instanceof java.sql.Date) {
                Mockito.when(resultSet.getDate(column)).thenReturn((java.sql.Date) content);
            } else {
                Mockito.when(resultSet.getString(column)).thenReturn((String) content);
            }
        }
        return resultSet;
    }

    // --- The methods that the repositories use ---------------------------

    @Override
    public int update(String sql, Object... args) {
        calls.add(new Call(sql, args));
        throwIfTestWantsAnError();
        return updateResult;
    }

    @Override
    public int update(PreparedStatementCreator creator, KeyHolder keyHolder) {
        calls.add(new Call("INSERT"));
        throwIfTestWantsAnError();
        try {
            Connection connection = Mockito.mock(Connection.class);
            Mockito.when(connection.prepareStatement(Mockito.anyString(), Mockito.anyInt()))
                    .thenReturn(statement);
            creator.createPreparedStatement(connection);
            keyHolder.getKeyList().add(generatedKeys);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        return 1;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T queryForObject(String sql, Class<T> requiredType, Object... args) {
        calls.add(new Call(sql, args));
        throwIfTestWantsAnError();
        return (T) singleResult;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T queryForObject(String sql, Class<T> requiredType) {
        calls.add(new Call(sql));
        throwIfTestWantsAnError();
        return (T) singleResult;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        calls.add(new Call(sql));
        this.lastRowMapper = rowMapper;
        throwIfTestWantsAnError();
        return (List<T>) listResult;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        calls.add(new Call(sql, args));
        this.lastRowMapper = rowMapper;
        throwIfTestWantsAnError();
        return (T) singleResult;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        calls.add(new Call(sql, args));
        this.lastRowMapper = rowMapper;
        throwIfTestWantsAnError();
        return (List<T>) listResult;
    }

    private void throwIfTestWantsAnError() {
        if (failure != null) {
            throw failure;
        }
    }
}
