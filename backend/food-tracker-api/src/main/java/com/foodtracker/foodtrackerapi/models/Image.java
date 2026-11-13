package com.foodtracker.foodtrackerapi.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * An image of a user or of a product. The database stores the image data as a
 * byte array. The REST API sends the data as a Base64 string, because JSON
 * holds text only.
 * </p>
 *
 * @author Janina Mattes
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Image {

    /** Primary key in the table ET_USER_IMAGE or ET_PRODUCT_IMAGE. */
    private Integer imageId;

    /** Name of the image, for example "Profilbild". */
    private String imageName;

    /** Address of the image if the image is on an external server. */
    private String imageUrl;

    /** Image data as a Base64 string. It is {@code null} if there is no data. */
    private String base64ImageString;

    /**
     * Makes an image that has a name only.
     *
     * @param imageId   primary key of the image
     * @param imageName name of the image
     */
    public Image(Integer imageId, String imageName) {
        this(imageId, imageName, null, null);
    }

    /**
     * Makes an image that is on an external server.
     *
     * @param imageId   primary key of the image
     * @param imageName name of the image
     * @param imageUrl  address of the image
     */
    public Image(Integer imageId, String imageName, String imageUrl) {
        this(imageId, imageName, imageUrl, null);
    }
}
