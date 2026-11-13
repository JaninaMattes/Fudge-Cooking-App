package com.foodtracker.foodtrackerapi.repositories;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.foodtracker.foodtrackerapi.common.utils.Attempt;

/**
 * <p>
 * Sets each sequence of the database to the highest key that its table holds.
 * This is necessary after somebody puts rows into the database by hand, because
 * such rows do not move the sequence. The next insert of the application would
 * then use a key that is already in use.
 * </p>
 *
 * <p>
 * See the
 * <a href="https://wiki.postgresql.org/wiki/Fixing_Sequences">PostgreSQL
 * documentation</a>.
 * </p>
 *
 * @author Janina Mattes
 */
@Repository
@Transactional
public class DBSequencesRepository {

    /**
     * All sequences and the key column that each sequence counts. The class had
     * one method for each line of this list before. All the methods were equal.
     */
    private static final List<String> SQL_CORRECT_SEQUENCES = Collections.unmodifiableList(Arrays.asList(
            sql("ET_USERS_SEQ", "USER_ID", "ET_USERS"),
            sql("ET_PROFILE_SETTINGS_SEQ", "SETTINGS_ID", "ET_PROFILE_SETTINGS"),
            sql("ET_RECIPE_SEQ", "RECIPE_ID", "ET_RECIPE"),
            sql("ET_INGREDIENT_SEQ", "INGREDIENT_ID", "ET_INGREDIENT"),
            sql("ET_DIETRY_TAG_SEQ", "TAG_ID", "ET_DIETRY_TAG"),
            sql("ET_INVENTORYLIST_SEQ", "INVENTORYLIST_ID", "ET_INVENTORYLIST"),
            sql("ET_SHOPPINGLIST_SEQ", "SHOPPINGLIST_ID", "ET_SHOPPINGLIST"),
            sql("ET_CURRENT_SHOPPINGLIST_SEQ", "CUR_SHOPPINGLIST_ID", "ET_CURRENT_SHOPPINGLIST"),
            sql("ET_OLD_SHOPPINGLIST_SEQ", "OLD_SHOPPINGLIST_ID", "ET_OLD_SHOPPINGLIST"),
            sql("ET_PRODUCT_SEQ", "PRODUCT_ID", "ET_PRODUCT"),
            sql("ET_PRODUCT_IMAGE_SEQ", "IMAGE_ID", "ET_PRODUCT_IMAGE"),
            sql("ET_USER_IMAGE_SEQ", "IMAGE_ID", "ET_USER_IMAGE")));

    private final JdbcTemplate jdbcTemplate;

    /**
     * Makes the repository.
     *
     * @param jdbcTemplate connection to the database
     */
    public DBSequencesRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Sets all sequences of the database to the highest key of their table.
     *
     * @return the number of sequences that the method corrected
     */
    public Integer correctAllDBSequences() {
        int corrected = 0;
        for (String statement : SQL_CORRECT_SEQUENCES) {
            // SETVAL is a function, so the statement is a query and not an update.
            boolean success = Attempt.orDefault("correct a database sequence", null,
                    () -> jdbcTemplate.queryForObject(statement, Long.class)) != null;
            if (success) {
                corrected++;
            }
        }
        return corrected;
    }

    /**
     * Gives the number of sequences that the repository corrects.
     *
     * @return the number of sequences
     */
    public int getSequenceCount() {
        return SQL_CORRECT_SEQUENCES.size();
    }

    private static String sql(String sequence, String keyColumn, String table) {
        return String.format("SELECT SETVAL('%s', COALESCE(MAX(%s), 1)) FROM %s;",
                sequence, keyColumn, table);
    }
}
