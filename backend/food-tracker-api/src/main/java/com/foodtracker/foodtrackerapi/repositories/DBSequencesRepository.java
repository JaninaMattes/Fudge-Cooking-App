package com.foodtracker.foodtrackerapi.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Fix sequences, after data has been manually inserted.
 * Documentation: https://wiki.postgresql.org/wiki/Fixing_Sequences
 *
 * @author Janina Mattes
 */
@Service
@Transactional
public class DBSequencesRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;

    // OTHER
    private static final String SQL_UPDATE_USER_SEQUENCE = "SELECT SETVAL('ET_USERS_SEQ', COALESCE(MAX(USER_ID), 1 )) FROM ET_USERS;";
    private static final String SQL_UPDATE_USER_PROFILE_SETTINGS_SEQUENCE = "SELECT SETVAL('ET_PROFILE_SETTINGS_SEQ', COALESCE(MAX(SETTINGS_ID), 1 )) FROM ET_PROFILE_SETTINGS;";
    private static final String SQL_UPDATE_RECIPE_SEQUENCE = "SELECT SETVAL('ET_RECIPE_SEQ', COALESCE(MAX(RECIPE_ID), 1 )) FROM ET_RECIPE;";
    private static final String SQL_UPDATE_RECIPE_INGREDIENT_SEQUENCE = "SELECT SETVAL('ET_INGREDIENT_SEQ', COALESCE(MAX(INGREDIENT_ID), 1 )) FROM ET_INGREDIENT;";
    private static final String SQL_UPDATE_DIETRY_TAG_SEQUENCE = "SELECT SETVAL('ET_DIETRY_TAG_SEQ', COALESCE(MAX(TAG_ID), 1 )) FROM  ET_DIETRY_TAG;";
    private static final String SQL_UPDATE_USER_INVENTORY_LIST_SEQUENCE = "SELECT SETVAL('ET_INVENTORYLIST_SEQ', COALESCE(MAX(INVENTORYLIST_ID), 1 )) FROM ET_INVENTORYLIST;";
    private static final String SQL_UPDATE_USER_SHOPPING_LISTSEQUENCE = "SELECT SETVAL('ET_SHOPPINGLIST_SEQ', COALESCE(MAX(SHOPPINGLIST_ID), 1 )) FROM ET_SHOPPINGLIST;";
    private static final String SQL_UPDATE_USER_CUR_SHOPPING_LISTSEQUENCE = "SELECT SETVAL('ET_CURRENT_SHOPPINGLIST_SEQ', COALESCE(MAX(CUR_SHOPPINGLIST_ID), 1 )) FROM ET_CURRENT_SHOPPINGLIST;";
    private static final String SQL_UPDATE_USER_OLD_SHOPPING_LISTSEQUENCE = "SELECT SETVAL('ET_OLD_SHOPPINGLIST_SEQ', COALESCE(MAX(OLD_SHOPPINGLIST_ID), 1 )) FROM ET_OLD_SHOPPINGLIST;";
    private static final String SQL_UPDATE_PRODUCT_SEQUENCE = "SELECT SETVAL('ET_PRODUCT_SEQ', COALESCE(MAX(PRODUCT_ID), 1 )) FROM ET_PRODUCT;";
    private static final String SQL_UPDATE_PRODUCT_IMAGE_SEQUENCE = "SELECT SETVAL('ET_PRODUCT_IMAGE_SEQ', COALESCE(MAX(IMAGE_ID), 1 )) FROM ET_PRODUCT_IMAGE;";
    private static final String SQL_UPDATE_USER_IMAGE_SEQUENCE = "SELECT SETVAL('ET_USER_IMAGE_SEQ', COALESCE(MAX(IMAGE_ID), 1 )) FROM ET_USER_IMAGE;";

    public Integer correctAllDBSequences() {
        Integer result = 0;
        // DB seqeunces need to be updated
        result += correctUserSequence();
        result += correctUserSettingsSequence();
        result += correctRecipeSequence();
        result += correctIngredientsSequence();
        result += correctDietryTagSequence();
        result += correctInvListSequence();
        result += correctShopListSequence();
        result += correctCurShopSequence();
        result += correctOldShopSequence();
        result += correctProductSequence();
        result += correctProdImgSequence();
        result += correctUserImgSequence();

        return result;
    }

    private Integer correctUserSequence() {
        // Update used sequence to the max key
        Integer result = 0;
        try {
            result = jdbcTemplate.update(SQL_UPDATE_USER_SEQUENCE);
        } catch (Exception e) {
            e.getStackTrace();
        }
        return result;
    }

    private Integer correctUserSettingsSequence() {
        // Update used sequence to the max key
        Integer result = 0;
        try {
            result = jdbcTemplate.update(SQL_UPDATE_USER_PROFILE_SETTINGS_SEQUENCE);
        } catch (Exception e) {
            e.getStackTrace();
        }
        return result;
    }

    private Integer correctRecipeSequence() {
        // Update used sequence to the max key
        Integer result = 0;
        try {
            result = jdbcTemplate.update(SQL_UPDATE_RECIPE_SEQUENCE);
        } catch (Exception e) {
            e.getStackTrace();
        }
        return result;
    }

    private Integer correctIngredientsSequence() {
        // Update used sequence to the max key
        Integer result = 0;
        try {
            result = jdbcTemplate.update(SQL_UPDATE_RECIPE_INGREDIENT_SEQUENCE);
        } catch (Exception e) {
            e.getStackTrace();
        }
        return result;
    }

    private Integer correctDietryTagSequence() {
        // Update used sequence to the max key
        Integer result = 0;
        try {
            result = jdbcTemplate.update(SQL_UPDATE_DIETRY_TAG_SEQUENCE);
        } catch (Exception e) {
            e.getStackTrace();
        }
        return result;
    }

    private Integer correctInvListSequence() {
        // Update used sequence to the max key
        Integer result = 0;
        try {
            result = jdbcTemplate.update(SQL_UPDATE_USER_INVENTORY_LIST_SEQUENCE);
        } catch (Exception e) {
            e.getStackTrace();
        }
        return result;
    }

    private Integer correctShopListSequence() {
        // Update used sequence to the max key
        Integer result = 0;
        try {
            result = jdbcTemplate.update(SQL_UPDATE_USER_SHOPPING_LISTSEQUENCE);
        } catch (Exception e) {
            e.getStackTrace();
        }
        return result;
    }

    private Integer correctCurShopSequence() {
        // Update used sequence to the max key
        Integer result = 0;
        try {
            result = jdbcTemplate.update(SQL_UPDATE_USER_CUR_SHOPPING_LISTSEQUENCE);
        } catch (Exception e) {
            e.getStackTrace();
        }
        return result;
    }

    private Integer correctOldShopSequence() {
        // Update used sequence to the max key
        Integer result = 0;
        try {
            result = jdbcTemplate.update(SQL_UPDATE_USER_OLD_SHOPPING_LISTSEQUENCE);
        } catch (Exception e) {
            e.getStackTrace();
        }
        return result;
    }

    private Integer correctProductSequence() {
        // Update used sequence to the max key
        Integer result = 0;
        try {
            result = jdbcTemplate.update(SQL_UPDATE_PRODUCT_SEQUENCE);
        } catch (Exception e) {
            e.getStackTrace();
        }
        return result;
    }

    private Integer correctProdImgSequence() {
        // Update used sequence to the max key
        Integer result = 0;
        try {
            result = jdbcTemplate.update(SQL_UPDATE_PRODUCT_IMAGE_SEQUENCE);
        } catch (Exception e) {
            e.getStackTrace();
        }
        return result;
    }

    private Integer correctUserImgSequence() {
        // Update used sequence to the max key
        Integer result = 0;
        try {
            result = jdbcTemplate.update(SQL_UPDATE_USER_IMAGE_SEQUENCE);
        } catch (Exception e) {
            e.getStackTrace();
        }
        return result;
    }

}
