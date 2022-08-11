package com.foodtracker.foodtrackerapi.models;

import java.util.Arrays;

/**
 * <p>
 * The Image is a POJO to represent product images and user profile images.
 * It contains the default URL String, but also a base64 encoded String that represents
 * a byte array of a PNG image, or bitmap in the Android frontend. 
 * The PostgreSQL database expects a byte array.
 * </p>
 * 
 * @author Janina Mattes
 */
public class Image {

    private Integer imageId;
    private String imageName;
    private String imageUrl;
    private String base64ImageString = null;
    
    /**
     * @param imageId
     * @param imageName
     */
    public Image(Integer imageId, String imageName) {
        this.imageId = imageId;
        this.imageName = imageName;
    }
    
    
    /**
     * @param imageId
     * @param imageName
     * @param imageUrl
     */
    public Image(Integer imageId, String imageName, String imageUrl) {
        this.imageId = imageId;
        this.imageName = imageName;
        this.imageUrl = imageUrl;
    }


    /**
     * @param imageId
     * @param imageName
     * @param imageUrl
     * @param base64ImageString
     */
    public Image(Integer imageId, String imageName, String imageUrl, String base64ImageString) {
        this.imageId = imageId;
        this.imageName = imageName;
        this.imageUrl = imageUrl;
        this.base64ImageString = base64ImageString;
    }


    /**
     * @return the imageId
     */
    public Integer getImageId() {
        return imageId;
    }

    /**
     * @param imageId the imageId to set
     */
    public void setImageId(Integer imageId) {
        this.imageId = imageId;
    }

    /**
     * @return the imageName
     */
    public String getImageName() {
        return imageName;
    }

    /**
     * @param imageName the imageName to set
     */
    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    /**
     * @return the imageUrl
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * @param imageUrl the imageUrl to set
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     * @return the base64ImageString
     */
    public String getBase64ImageString() {
        return base64ImageString;
    }

    /**
     * @param base64ImageString the base64ImageString to set
     */
    public void setImageBytes(String base64ImageString) {
        this.base64ImageString = base64ImageString;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((base64ImageString == null) ? 0 : base64ImageString.hashCode());
        result = prime * result + ((imageId == null) ? 0 : imageId.hashCode());
        result = prime * result + ((imageName == null) ? 0 : imageName.hashCode());
        result = prime * result + ((imageUrl == null) ? 0 : imageUrl.hashCode());
        return result;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Image)) {
            return false;
        }
        Image other = (Image) obj;
        if (base64ImageString == null) {
            if (other.base64ImageString != null) {
                return false;
            }
        } else if (!base64ImageString.equals(other.base64ImageString)) {
            return false;
        }
        if (imageId == null) {
            if (other.imageId != null) {
                return false;
            }
        } else if (!imageId.equals(other.imageId)) {
            return false;
        }
        if (imageName == null) {
            if (other.imageName != null) {
                return false;
            }
        } else if (!imageName.equals(other.imageName)) {
            return false;
        }
        if (imageUrl == null) {
            if (other.imageUrl != null) {
                return false;
            }
        } else if (!imageUrl.equals(other.imageUrl)) {
            return false;
        }
        return true;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    
    @Override
    public String toString() {
        return "Image [base64ImageString=" + base64ImageString + ", imageId=" + imageId + ", imageName=" + imageName
                + ", imageUrl=" + imageUrl + "]";
    }
        
}
