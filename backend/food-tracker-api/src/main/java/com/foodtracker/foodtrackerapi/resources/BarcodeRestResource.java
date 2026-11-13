package com.foodtracker.foodtrackerapi.resources;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foodtracker.foodtrackerapi.common.config.ApiProperties;
import com.foodtracker.foodtrackerapi.models.BarcodeProduct;
import com.foodtracker.foodtrackerapi.services.BarcodeRestService;

/**
 * <p>
 * The endpoint that reads the data of a product for a scanned barcode. The
 * client decodes the barcode with the ZXing library and sends the numerical
 * code.
 * </p>
 *
 * @author Janina Mattes
 */
@RestController
@RequestMapping("/api/barcode/rest")
public class BarcodeRestResource {

    private final BarcodeRestService barcodeService;
    private final ApiProperties apiProperties;

    /**
     * Makes the resource.
     *
     * @param barcodeService service that reads the external API
     * @param apiProperties  configuration that holds the keys of the external
     *                       API
     */
    public BarcodeRestResource(BarcodeRestService barcodeService, ApiProperties apiProperties) {
        this.barcodeService = barcodeService;
        this.apiProperties = apiProperties;
    }

    /**
     * Reads the product that belongs to a barcode.
     *
     * @param barcodeCode barcode of the product
     * @return the product. All values are empty if the external API gives no
     *         result.
     */
    @GetMapping(path = "/{barcodeCode}", produces = "application/json")
    public ResponseEntity<Optional<BarcodeProduct>> getProductFromBarcode(
            @PathVariable("barcodeCode") String barcodeCode) {
        Optional<BarcodeProduct> product = barcodeService.getProduct(
                apiProperties.getEdamamAppId(),
                apiProperties.getEdamamBarcodeKey(),
                barcodeCode);
        return new ResponseEntity<>(product, HttpStatus.OK);
    }
}
