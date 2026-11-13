package com.foodtracker.foodtrackerapi.resources;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foodtracker.foodtrackerapi.services.DBService;

/**
 * <p>
 * The endpoint that corrects the sequences of the database. An administrator
 * needs it after a manual import of data.
 * </p>
 *
 * @author Janina Mattes
 */
@RestController
@RequestMapping("/api/db")
public class DBResource {

    private final DBService service;

    /**
     * Makes the resource.
     *
     * @param service service that corrects the sequences
     */
    public DBResource(DBService service) {
        this.service = service;
    }

    /**
     * Sets all sequences of the database to the highest key of their table.
     *
     * @return the number of sequences that the operation corrected
     */
    @GetMapping("/init")
    public ResponseEntity<Integer> initDatabaseSequences() {
        Integer result = service.updateDBSequences();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
