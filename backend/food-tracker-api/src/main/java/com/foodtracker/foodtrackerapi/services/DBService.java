package com.foodtracker.foodtrackerapi.services;

import org.springframework.stereotype.Service;

import com.foodtracker.foodtrackerapi.repositories.DBSequencesRepository;

/**
 * <p>
 * Corrects the sequences of the database. An administrator needs this operation
 * after a manual import of data.
 * </p>
 *
 * @author Janina Mattes
 */
@Service
public class DBService {

    private final DBSequencesRepository dbRepository;

    /**
     * Makes the service.
     *
     * @param dbRepository repository that runs the SETVAL statements
     */
    public DBService(DBSequencesRepository dbRepository) {
        this.dbRepository = dbRepository;
    }

    /**
     * Sets all sequences of the database to the highest key of their table.
     *
     * @return the number of sequences that the operation corrected
     */
    public Integer updateDBSequences() {
        return dbRepository.correctAllDBSequences();
    }
}
