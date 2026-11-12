package com.foodtracker.foodtrackerapi.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link DBSequencesRepository}.
 *
 * @author Janina Mattes
 */
class DBSequencesRepositoryTest {

    private RecordingJdbcTemplate jdbc;
    private DBSequencesRepository repository;

    @BeforeEach
    void setUp() {
        jdbc = new RecordingJdbcTemplate();
        repository = new DBSequencesRepository(jdbc);
    }

    @Test
    @DisplayName("The repository corrects the sequence of each table")
    void correctsEachSequence() {
        jdbc.givesObject(42L);

        Integer corrected = repository.correctAllDBSequences();

        assertThat(corrected).isEqualTo(repository.getSequenceCount()).isEqualTo(12);
        assertThat(jdbc.calls()).hasSize(12);
    }

    @Test
    @DisplayName("Each statement reads the highest key of its table")
    void eachStatementReadsHighestKey() {
        jdbc.givesObject(42L);

        repository.correctAllDBSequences();

        assertThat(jdbc.calls()).allSatisfy(call -> {
            assertThat(call.sql()).startsWith("SELECT SETVAL(");
            assertThat(call.sql()).contains("COALESCE(MAX(");
            assertThat(call.args()).isEmpty();
        });
    }

    @Test
    @DisplayName("The repository corrects the sequences of all tables of the application")
    void correctsSequencesOfAllTables() {
        jdbc.givesObject(42L);

        repository.correctAllDBSequences();

        assertThat(jdbc.calls()).extracting(RecordingJdbcTemplate.Call::sql)
                .anySatisfy(sql -> assertThat(sql).contains("ET_USERS_SEQ"))
                .anySatisfy(sql -> assertThat(sql).contains("ET_PRODUCT_SEQ"))
                .anySatisfy(sql -> assertThat(sql).contains("ET_RECIPE_SEQ"))
                .anySatisfy(sql -> assertThat(sql).contains("ET_INGREDIENT_SEQ"))
                .anySatisfy(sql -> assertThat(sql).contains("ET_DIETRY_TAG_SEQ"))
                .anySatisfy(sql -> assertThat(sql).contains("ET_INVENTORYLIST_SEQ"))
                .anySatisfy(sql -> assertThat(sql).contains("ET_SHOPPINGLIST_SEQ"))
                .anySatisfy(sql -> assertThat(sql).contains("ET_CURRENT_SHOPPINGLIST_SEQ"))
                .anySatisfy(sql -> assertThat(sql).contains("ET_OLD_SHOPPINGLIST_SEQ"))
                .anySatisfy(sql -> assertThat(sql).contains("ET_PRODUCT_IMAGE_SEQ"))
                .anySatisfy(sql -> assertThat(sql).contains("ET_USER_IMAGE_SEQ"))
                .anySatisfy(sql -> assertThat(sql).contains("ET_PROFILE_SETTINGS_SEQ"));
    }

    @Test
    @DisplayName("The repository counts no sequence when the database is not available")
    void countsNoSequenceOnError() {
        jdbc.fails();

        assertThat(repository.correctAllDBSequences()).isZero();
        assertThat(jdbc.calls()).hasSize(12);
    }

    @Test
    @DisplayName("The repository counts no sequence when a statement gives no answer")
    void countsNoSequenceWithoutAnswer() {
        jdbc.givesObject(null);

        assertThat(repository.correctAllDBSequences()).isZero();
    }
}
