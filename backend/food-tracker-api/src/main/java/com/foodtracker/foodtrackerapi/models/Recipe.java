package com.foodtracker.foodtrackerapi.models;

import java.util.ArrayList;

public class Recipe {

    private Integer recipeId;
    private String title;
    private String recipeLabel;
    private String recipeDescription;
    private Float cummulativeRating;
    private Integer amountOfRatings;
    private String cookingTime;
    private String difficulty;
    private String instructions;
    private String nutritionValue;
    private String imageUrl; 
    private ArrayList<Ingredient> ingredients = new ArrayList<Ingredient>();

    public Recipe(){}
    
    /**
     * @param recipeId
     * @param title
     * @param recipeLabel
     * @param recipeDescription
     * @param cummulativeRating
     * @param amountOfRatings
     * @param cookingTime
     * @param difficulty
     * @param instructions
     */
    public Recipe(Integer recipeId, String title, String recipeLabel, String recipeDescription, Float cummulativeRating,
            Integer amountOfRatings, String cookingTime, String difficulty, String instructions) {
        this.recipeId = recipeId;
        this.title = title;
        this.recipeLabel = recipeLabel;
        this.recipeDescription = recipeDescription;
        this.cummulativeRating = cummulativeRating;
        this.amountOfRatings = amountOfRatings;
        this.cookingTime = cookingTime;
        this.difficulty = difficulty;
        this.instructions = instructions;
    }


    /**
     * @param recipeId
     * @param title
     * @param recipeLabel
     * @param recipeDescription
     * @param cummulativeRating
     * @param amountOfRatings
     * @param cookingTime
     * @param difficulty
     * @param instructions
     * @param nutritionValue
     * @param imageUrl
     * @param ingredients
     */
    public Recipe(Integer recipeId, String title, String recipeLabel, String recipeDescription, Float cummulativeRating,
            Integer amountOfRatings, String cookingTime, String difficulty, String instructions, String nutritionValue,
            String imageUrl, ArrayList<Ingredient> ingredients) {
        this.recipeId = recipeId;
        this.title = title;
        this.recipeLabel = recipeLabel;
        this.recipeDescription = recipeDescription;
        this.cummulativeRating = cummulativeRating;
        this.amountOfRatings = amountOfRatings;
        this.cookingTime = cookingTime;
        this.difficulty = difficulty;
        this.instructions = instructions;
        this.nutritionValue = nutritionValue;
        this.imageUrl = imageUrl;
        this.ingredients = ingredients;
    }

    
    public Integer getRecipeId() {
        return recipeId;
    }


    /**
     * @param recipeId the recipeId to set
     */
    public void setRecipeId(Integer recipeId) {
        this.recipeId = recipeId;
    }


    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }


    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }


    /**
     * @return the recipeLabel
     */
    public String getRecipeLabel() {
        return recipeLabel;
    }


    /**
     * @param recipeLabel the recipeLabel to set
     */
    public void setRecipeLabel(String recipeLabel) {
        this.recipeLabel = recipeLabel;
    }


    /**
     * @return the recipeDescription
     */
    public String getRecipeDescription() {
        return recipeDescription;
    }


    /**
     * @param recipeDescription the recipeDescription to set
     */
    public void setRecipeDescription(String recipeDescription) {
        this.recipeDescription = recipeDescription;
    }


    /**
     * @return the cummulativeRating
     */
    public Float getCummulativeRating() {
        return cummulativeRating;
    }


    /**
     * @param cummulativeRating the cummulativeRating to set
     */
    public void setCummulativeRating(Float cummulativeRating) {
        this.cummulativeRating = cummulativeRating;
    }


    /**
     * @return the amountOfRatings
     */
    public Integer getAmountOfRatings() {
        return amountOfRatings;
    }


    /**
     * @param amountOfRatings the amountOfRatings to set
     */
    public void setAmountOfRatings(Integer amountOfRatings) {
        this.amountOfRatings = amountOfRatings;
    }


    /**
     * @return the cookingTime
     */
    public String getCookingTime() {
        return cookingTime;
    }


    /**
     * @param cookingTime the cookingTime to set
     */
    public void setCookingTime(String cookingTime) {
        this.cookingTime = cookingTime;
    }


    /**
     * @return the difficulty
     */
    public String getDifficulty() {
        return difficulty;
    }


    /**
     * @param difficulty the difficulty to set
     */
    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }


    /**
     * @return the instructions
     */
    public String getInstructions() {
        return instructions;
    }


    /**
     * @param instructions the instructions to set
     */
    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }


    /**
     * @return the nutritionValue
     */
    public String getNutritionValue() {
        return nutritionValue;
    }


    /**
     * @param nutritionValue the nutritionValue to set
     */
    public void setNutritionValue(String nutritionValue) {
        this.nutritionValue = nutritionValue;
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
     * @return the ingredients
     */
    public ArrayList<Ingredient> getIngredients() {
        return ingredients;
    }


    /**
     * @param ingredients the ingredients to set
     */
    public void setIngredients(ArrayList<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((amountOfRatings == null) ? 0 : amountOfRatings.hashCode());
        result = prime * result + ((cookingTime == null) ? 0 : cookingTime.hashCode());
        result = prime * result + ((cummulativeRating == null) ? 0 : cummulativeRating.hashCode());
        result = prime * result + ((difficulty == null) ? 0 : difficulty.hashCode());
        result = prime * result + ((imageUrl == null) ? 0 : imageUrl.hashCode());
        result = prime * result + ((ingredients == null) ? 0 : ingredients.hashCode());
        result = prime * result + ((instructions == null) ? 0 : instructions.hashCode());
        result = prime * result + ((nutritionValue == null) ? 0 : nutritionValue.hashCode());
        result = prime * result + ((recipeDescription == null) ? 0 : recipeDescription.hashCode());
        result = prime * result + ((recipeId == null) ? 0 : recipeId.hashCode());
        result = prime * result + ((recipeLabel == null) ? 0 : recipeLabel.hashCode());
        result = prime * result + ((title == null) ? 0 : title.hashCode());
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
        if (!(obj instanceof Recipe)) {
            return false;
        }
        Recipe other = (Recipe) obj;
        if (amountOfRatings == null) {
            if (other.amountOfRatings != null) {
                return false;
            }
        } else if (!amountOfRatings.equals(other.amountOfRatings)) {
            return false;
        }
        if (cookingTime == null) {
            if (other.cookingTime != null) {
                return false;
            }
        } else if (!cookingTime.equals(other.cookingTime)) {
            return false;
        }
        if (cummulativeRating == null) {
            if (other.cummulativeRating != null) {
                return false;
            }
        } else if (!cummulativeRating.equals(other.cummulativeRating)) {
            return false;
        }
        if (difficulty == null) {
            if (other.difficulty != null) {
                return false;
            }
        } else if (!difficulty.equals(other.difficulty)) {
            return false;
        }
        if (imageUrl == null) {
            if (other.imageUrl != null) {
                return false;
            }
        } else if (!imageUrl.equals(other.imageUrl)) {
            return false;
        }
        if (ingredients == null) {
            if (other.ingredients != null) {
                return false;
            }
        } else if (!ingredients.equals(other.ingredients)) {
            return false;
        }
        if (instructions == null) {
            if (other.instructions != null) {
                return false;
            }
        } else if (!instructions.equals(other.instructions)) {
            return false;
        }
        if (nutritionValue == null) {
            if (other.nutritionValue != null) {
                return false;
            }
        } else if (!nutritionValue.equals(other.nutritionValue)) {
            return false;
        }
        if (recipeDescription == null) {
            if (other.recipeDescription != null) {
                return false;
            }
        } else if (!recipeDescription.equals(other.recipeDescription)) {
            return false;
        }
        if (recipeId == null) {
            if (other.recipeId != null) {
                return false;
            }
        } else if (!recipeId.equals(other.recipeId)) {
            return false;
        }
        if (recipeLabel == null) {
            if (other.recipeLabel != null) {
                return false;
            }
        } else if (!recipeLabel.equals(other.recipeLabel)) {
            return false;
        }
        if (title == null) {
            if (other.title != null) {
                return false;
            }
        } else if (!title.equals(other.title)) {
            return false;
        }
        return true;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    
    @Override
    public String toString() {
        return "Recipe [amountOfRatings=" + amountOfRatings + ", cookingTime=" + cookingTime + ", cummulativeRating="
                + cummulativeRating + ", difficulty=" + difficulty + ", imageUrl=" + imageUrl + ", ingredients="
                + ingredients + ", instructions=" + instructions + ", nutritionValue=" + nutritionValue
                + ", recipeDescription=" + recipeDescription + ", recipeId=" + recipeId + ", recipeLabel=" + recipeLabel
                + ", title=" + title + "]";
    }
    
}
