package com.foodtracker.foodtrackerapi.resources;

import com.foodtracker.foodtrackerapi.services.DBService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Helper endpoint to set DB sequence to current state.
 * @author Janina Mattes
 */
@RestController
@RequestMapping("/api/db")
public class DBResource {
    
    @Autowired
    DBService service;

    @GetMapping("/init")
    public ResponseEntity<Integer> testEndpoint() {
        Integer result = service.updateDBSequences();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
