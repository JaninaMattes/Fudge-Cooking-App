package com.foodtracker.foodtrackerapi.models;

import java.util.ArrayList;

public class User {

    private Integer userId;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Settings userSettings = null;
    private Image userImage = null;
    private ArrayList<Tag> dietaryPreferences = new ArrayList<>();

    public User(Integer userId, String firstName, String lastName, String email, String password) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
    }

    /**
     * @param userId
     * @param firstName
     * @param lastName
     * @param email
     * @param password
     * @param userSettings
     * @param userImage
     * @param dietaryPreferences
     */
    public User(Integer userId, String firstName, String lastName, String email, String password, Settings userSettings,
            Image userImage, ArrayList<Tag> dietaryPreferences) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.userSettings = userSettings;
        this.userImage = userImage;
        this.dietaryPreferences = dietaryPreferences;
    }


    /**
     * @return the userId
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    /**
     * @return the firstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @param firstName the firstName to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * @return the lastName
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @param lastName the lastName to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the userSettings
     */
    public Settings getUserSettings() {
        return userSettings;
    }

    /**
     * @param userSettings the userSettings to set
     */
    public void setUserSettings(Settings userSettings) {
        this.userSettings = userSettings;
    }

    /**
     * @return the userImage
     */
    public Image getUserImage() {
        return userImage;
    }

    /**
     * @param userImage the userImage to set
     */
    public void setUserImage(Image userImage) {
        this.userImage = userImage;
    }

    /**
     * @return the dietaryPreferences
     */
    public ArrayList<Tag> getDietaryPreferences() {
        return dietaryPreferences;
    }

    /**
     * @param dietaryPreferences the dietaryPreferences to set
     */
    public void setDietaryPreferences(ArrayList<Tag> dietaryPreferences) {
        this.dietaryPreferences = dietaryPreferences;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dietaryPreferences == null) ? 0 : dietaryPreferences.hashCode());
        result = prime * result + ((email == null) ? 0 : email.hashCode());
        result = prime * result + ((firstName == null) ? 0 : firstName.hashCode());
        result = prime * result + ((lastName == null) ? 0 : lastName.hashCode());
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        result = prime * result + ((userId == null) ? 0 : userId.hashCode());
        result = prime * result + ((userImage == null) ? 0 : userImage.hashCode());
        result = prime * result + ((userSettings == null) ? 0 : userSettings.hashCode());
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
        if (!(obj instanceof User)) {
            return false;
        }
        User other = (User) obj;
        if (dietaryPreferences == null) {
            if (other.dietaryPreferences != null) {
                return false;
            }
        } else if (!dietaryPreferences.equals(other.dietaryPreferences)) {
            return false;
        }
        if (email == null) {
            if (other.email != null) {
                return false;
            }
        } else if (!email.equals(other.email)) {
            return false;
        }
        if (firstName == null) {
            if (other.firstName != null) {
                return false;
            }
        } else if (!firstName.equals(other.firstName)) {
            return false;
        }
        if (lastName == null) {
            if (other.lastName != null) {
                return false;
            }
        } else if (!lastName.equals(other.lastName)) {
            return false;
        }
        if (password == null) {
            if (other.password != null) {
                return false;
            }
        } else if (!password.equals(other.password)) {
            return false;
        }
        if (userId == null) {
            if (other.userId != null) {
                return false;
            }
        } else if (!userId.equals(other.userId)) {
            return false;
        }
        if (userImage == null) {
            if (other.userImage != null) {
                return false;
            }
        } else if (!userImage.equals(other.userImage)) {
            return false;
        }
        if (userSettings == null) {
            if (other.userSettings != null) {
                return false;
            }
        } else if (!userSettings.equals(other.userSettings)) {
            return false;
        }
        return true;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    
    @Override
    public String toString() {
        return "User [dietaryPreferences=" + dietaryPreferences + ", email=" + email + ", firstName=" + firstName
                + ", lastName=" + lastName + ", password=" + password + ", userId=" + userId + ", userImage="
                + userImage + ", userSettings=" + userSettings + "]";
    }

}
