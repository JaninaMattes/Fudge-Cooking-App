package com.foodtracker.foodtrackerapi.services;

import com.foodtracker.foodtrackerapi.repositories.DBSequencesRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DBService {
    
    @Autowired
    DBSequencesRepository dbRepository;

    public Integer updateDBSequences() {
        Integer result = 0;
        try{
            result = dbRepository.correctAllDBSequences();
        }catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }
}
