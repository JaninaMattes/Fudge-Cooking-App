package com.foodtracker.foodtrackerapi.resources;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

import com.foodtracker.foodtrackerapi.Constants;
import com.foodtracker.foodtrackerapi.models.BarcodeProduct;
import com.foodtracker.foodtrackerapi.services.BarcodeRestService;

/**
 * Retrieve barcode information from Edamam API by scanned numerical barcode code.
 * @author Janina Mattes
 */
@RestController
@RequestMapping("/api/barcode/rest")
public class BarcodeRestResource {
    
    @Autowired
    BarcodeRestService barcodeService;

    @GetMapping(path = "/{barcodeCode}", produces = "application/json")
    public ResponseEntity<Optional<BarcodeProduct>> getProductFromBarcode(@PathVariable("barcodeCode") String barcodeCode) {
        Optional<BarcodeProduct> product = barcodeService.getProduct(Constants.EDAMAM_APP_ID, Constants.EDAMAM_BARCODE_API_KEY, barcodeCode);
        return new ResponseEntity<>(product, HttpStatus.OK);
    }
}
