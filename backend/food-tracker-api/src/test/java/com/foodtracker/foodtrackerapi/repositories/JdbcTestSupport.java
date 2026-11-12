package com.foodtracker.foodtrackerapi.repositories;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.KeyHolder;

/**
 * <p>
 * Helpers for the tests of the repositories. The tests work with a mock of the
 * {@code JdbcTemplate}, so they need no database. Each helper covers one part
 * of the work that a test must do more than once.
 * </p>
 *
 * @author Janina Mattes
 */
final class JdbcTestSupport {

    private JdbcTestSupport() {
        // Utility class. Do not make an instance of it.
    }

    /**
     * Lets the mock answer an {@code INSERT} with a generated key. The mock
     * also runs the code that fills the statement, so that the test covers it.
     *
     * @param jdbcTemplate mock of the database connection
     * @param keys         keys that the database gives back. An empty map
     *                     stands for an insert without a key.
     * @return the statement that the repository filled
     */
    static PreparedStatement answerInsertWithKeys(JdbcTemplate jdbcTemplate,
            Map<String, Object> keys) {
        PreparedStatement statement = mock(PreparedStatement.class);
        Answer<Integer> answer = invocation -> {
            PreparedStatementCreator creator = invocation.getArgument(0);
            KeyHolder keyHolder = invocation.getArgument(1);
            java.sql.Connection connection = mock(java.sql.Connection.class);
            when(connection.prepareStatement(anyString(), any(int.class))).thenReturn(statement);
            creator.createPreparedStatement(connection);
            keyHolder.getKeyList().add(keys);
            return 1;
        };
        when(jdbcTemplate.update(any(PreparedStatementCreator.class), any(KeyHolder.class)))
                .thenAnswer(answer);
        return statement;
    }

    /**
     * Lets the mock answer an {@code INSERT} with one generated key.
     *
     * @param jdbcTemplate mock of the database connection
     * @param keyColumn    name of the key column
     * @param keyValue     value of the key
     * @return the statement that the repository filled
     */
    static PreparedStatement answerInsertWithKey(JdbcTemplate jdbcTemplate, String keyColumn,
            Object keyValue) {
        return answerInsertWithKeys(jdbcTemplate,
                Collections.singletonMap(keyColumn, keyValue));
    }

    /**
     * Reads the SQL statement that the repository used for an {@code INSERT}.
     *
     * @param jdbcTemplate mock of the database connection
     * @return the SQL statement
     * @throws SQLException if the mock cannot answer
     */
    static String capturedInsertSql(JdbcTemplate jdbcTemplate) throws SQLException {
        ArgumentCaptor<PreparedStatementCreator> creator = ArgumentCaptor
                .forClass(PreparedStatementCreator.class);
        org.mockito.Mockito.verify(jdbcTemplate).update(creator.capture(), any(KeyHolder.class));
        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        java.sql.Connection connection = mock(java.sql.Connection.class);
        when(connection.prepareStatement(sql.capture(), any(int.class)))
                .thenReturn(mock(PreparedStatement.class));
        creator.getValue().createPreparedStatement(connection);
        return sql.getValue();
    }

    /**
     * Makes a mock of a result set that gives the given values.
     *
     * @param values values of the row. The key is the name of the column.
     * @return the mock of the result set
     * @throws SQLException if the mock cannot answer
     */
    static ResultSet rowWith(Map<String, Object> values) throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        for (Map.Entry<String, Object> value : values.entrySet()) {
            String column = value.getKey();
            Object content = value.getValue();
            if (content instanceof Integer) {
                when(resultSet.getInt(column)).thenReturn((Integer) content);
            } else if (content instanceof Float) {
                when(resultSet.getFloat(column)).thenReturn((Float) content);
            } else if (content instanceof Boolean) {
                when(resultSet.getBoolean(column)).thenReturn((Boolean) content);
            } else if (content instanceof byte[]) {
                when(resultSet.getBytes(column)).thenReturn((byte[]) content);
            } else if (content instanceof java.sql.Date) {
                when(resultSet.getDate(column)).thenReturn((java.sql.Date) content);
            } else {
                when(resultSet.getString(column)).thenReturn((String) content);
            }
        }
        return resultSet;
    }

    /**
     * Reads the row mapper that the repository gave to
     * {@code JdbcTemplate.query} and uses it on one row. The test can then
     * prove that the mapper reads the correct columns.
     *
     * @param <T>        type of the model object
     * @param mapper     row mapper of the repository
     * @param columns    values of the row
     * @return the model object that the mapper made
     * @throws SQLException if the mock cannot answer
     */
    static <T> T mapRow(RowMapper<T> mapper, Map<String, Object> columns) throws SQLException {
        return mapper.mapRow(rowWith(columns), 1);
    }
}
