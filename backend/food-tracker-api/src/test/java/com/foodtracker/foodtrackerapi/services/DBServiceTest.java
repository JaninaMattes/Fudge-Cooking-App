package com.foodtracker.foodtrackerapi.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.foodtracker.foodtrackerapi.repositories.DBSequencesRepository;

/**
 * Tests for {@link DBService}.
 *
 * @author Janina Mattes
 */
class DBServiceTest {

    @Test
    @DisplayName("The service gives the number of sequences that the repository corrected")
    void givesNumberOfCorrectedSequences() {
        DBSequencesRepository repository = mock(DBSequencesRepository.class);
        when(repository.correctAllDBSequences()).thenReturn(12);

        assertThat(new DBService(repository).updateDBSequences()).isEqualTo(12);
    }

    @Test
    @DisplayName("The service gives 0 when the repository corrected no sequence")
    void givesZeroWithoutCorrectedSequence() {
        DBSequencesRepository repository = mock(DBSequencesRepository.class);
        when(repository.correctAllDBSequences()).thenReturn(0);

        assertThat(new DBService(repository).updateDBSequences()).isZero();
    }
}
