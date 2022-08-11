DROP DATABASE IF EXISTS foodtrackerdb;
DROP USER IF EXISTS foodtracker;

CREATE USER foodtracker WITH PASSWORD 'password';
CREATE DATABASE foodtrackerdb WITH template=template0 owner=foodtracker;

\connect foodtrackerdb;

ALTER DEFAULT PRIVILEGES GRANT ALL ON tables TO foodtracker;
ALTER DEFAULT PRIVILEGES GRANT ALL ON sequences TO foodtracker;

-- create table for registered user, as soon as user is logged in set is_logged_in = TRUE
-- if user wants to take a photograph of the phone gallery etc. the image can be stored in the et_user_image table
CREATE TABLE IF NOT EXISTS et_users(
                         user_id INTEGER PRIMARY KEY NOT NULL,
                         first_name VARCHAR(20) NOT NULL,
                         last_name VARCHAR(20) NOT NULL,
                         email VARCHAR(30) NOT NULL,
                         password TEXT NOT NULL,
                         is_logged_in BOOLEAN DEFAULT FALSE
);

-- create image table to store product images 
-- additionally photographs from user or product can be stored as bytes
-- More: https://giswiki.hsr.ch/PostgreSQL_-_Binary_Large_Objects
CREATE TABLE IF NOT EXISTS et_user_image(
                        image_id INTEGER PRIMARY KEY NOT NULL,
                        user_id INTEGER NOT NULL,
                        image_name VARCHAR(25) NOT NULL DEFAULT 'Profilbild',
                        image_url TEXT NOT NULL DEFAULT '',
                        image_bytes BYTEA
);
ALTER TABLE et_user_image add CONSTRAINT image_user_fk
    FOREIGN KEY(user_id) REFERENCES et_users(user_id);

-- create table profile setting to allow notifications
CREATE TABLE IF NOT EXISTS et_profile_settings(
                              settings_id INTEGER PRIMARY KEY NOT NULL,
                              user_id INTEGER NOT NULL,
                              reminder_product_expiration BOOLEAN DEFAULT FALSE,
                              suggest_recipes BOOLEAN DEFAULT FALSE,
                              push_notifications BOOLEAN DEFAULT FALSE
);
ALTER TABLE et_profile_settings add CONSTRAINT profile_settings_users_fk
    FOREIGN KEY(user_id) REFERENCES et_users(user_id);

-- create table recipe to store recipes fetched from external APIs
-- set favorite_recipe = TRUE if user has liked, or rated the recipe with > 4 stars
CREATE TABLE IF NOT EXISTS et_recipe(
                              recipe_id INTEGER PRIMARY KEY NOT NULL,
                              user_id INTEGER NOT NULL,
                              title TEXT NOT NULL,
                              recipe_label TEXT NOT NULL,
                              recipe_description TEXT NOT NULL,
                              cummulativerating DECIMAL(4,2) NOT NULL,
                              amountofratings INTEGER Not NULL,
                              cooking_time VARCHAR(50) NOT NULL,
                              difficulty VARCHAR(25) NOT NULL,
                              instruction TEXT NOT NULL,
                              nutrition_value VARCHAR(50) NOT NULL,
                              image_url TEXT NOT NULL DEFAULT "https://cdn5.vectorstock.com/i/1000x1000/07/09/chef-hat-sign-icon-cooking-symbol-vector-3750709.jpg",
                              favorite_recipe BOOLEAN DEFAULT FALSE
);
ALTER TABLE et_recipe add CONSTRAINT recipe_users_fk
    FOREIGN KEY(user_id) REFERENCES et_users(user_id);

-- create table for ingredients of one recipe
CREATE TABLE IF NOT EXISTS et_ingredient(
                              ingredient_id INTEGER PRIMARY KEY NOT NULL,
                              ingredient_name TEXT NOT NULL,
                              quantity TEXT NOT NULL
);

-- create table for n:m relation between recipe and product (ingredient)
CREATE TABLE IF NOT EXISTS et_recipe_ingredient(
                              recipe_id INTEGER REFERENCES et_recipe,
                              ingredient_id INTEGER REFERENCES et_ingredient,
                              PRIMARY KEY(recipe_id, ingredient_id)
);

-- create deitry-tag table, to store all possible dietry tags, e.g. VEGAN, PALEO, LOW CARB etc. 
CREATE TABLE IF NOT EXISTS et_dietry_tag(
                              tag_id INTEGER PRIMARY KEY NOT NULL,
                              label VARCHAR(25) NOT NULL
);

-- create user-tag table, for dietry preference filters e.g. VEGAN, PALEO, LOW CARB etc. 
CREATE TABLE IF NOT EXISTS et_user_tag(
                              user_id INTEGER REFERENCES et_users,
                              tag_id INTEGER REFERENCES et_dietry_tag,
                              PRIMARY KEY(user_id, tag_id)
);

-- create inventorylist table
CREATE TABLE IF NOT EXISTS et_inventorylist(
                              inventorylist_id INTEGER PRIMARY KEY NOT NULL,
                              user_id INTEGER NOT NULL
);
ALTER TABLE et_inventorylist add CONSTRAINT inventorylist_users_fk
    FOREIGN KEY(user_id) REFERENCES et_users(user_id);

-- create shoppinglist table, uses the is-a or gen-spec pattern
CREATE TABLE IF NOT EXISTS et_shoppinglist(
                              shoppinglist_id INTEGER PRIMARY KEY NOT NULL

);

-- create current shoppinglist table 
CREATE TABLE IF NOT EXISTS et_current_shoppinglist(
                            cur_shoppinglist_id INTEGER PRIMARY KEY NOT NULL,
                            shoppinglist_id INTEGER NOT NULL,
                            user_id INTEGER NOT NULL
);
ALTER TABLE et_current_shoppinglist add CONSTRAINT cur_shop_user_fk
    FOREIGN KEY(user_id) REFERENCES et_users(user_id);
ALTER TABLE et_current_shoppinglist add CONSTRAINT cur_shoppinglist_parent_fk
    FOREIGN KEY(cur_shoppinglist_id) REFERENCES et_shoppinglist(shoppinglist_id)
    ON UPDATE CASCADE ON DELETE NO ACTION;

-- create old shoppinglist table of previously used products
CREATE TABLE IF NOT EXISTS et_old_shoppinglist(
                            old_shoppinglist_id INTEGER PRIMARY KEY NOT NULL ,
                            shoppinglist_id INTEGER NOT NULL,
                            user_id INTEGER NOT NULL
);
ALTER TABLE et_old_shoppinglist add CONSTRAINT old_shop_user_fk
    FOREIGN KEY(user_id) REFERENCES et_users(user_id);
ALTER TABLE et_old_shoppinglist add CONSTRAINT old_shoppinglist_parent_fk
    FOREIGN KEY(old_shoppinglist_id) REFERENCES et_shoppinglist(shoppinglist_id)
    ON UPDATE CASCADE ON DELETE NO ACTION;

-- create product table for products in inventorylist, or shoppinglist, or both
CREATE TABLE IF NOT EXISTS et_product(
                              product_id INTEGER PRIMARY KEY NOT NULL,
                              cur_shoppinglist_id INTEGER,
                              old_shoppinglist_id INTEGER,
                              inventorylist_id INTEGER,
                              product_name VARCHAR(55) NOT NULL,
                              expiration_date DATE NOT NULL, 
                              quantity VARCHAR(255) NOT NULL DEFAULT '1 Piece',
                              manufacturer VARCHAR(25) NOT NULL DEFAULT '-',
                              nutrition_value VARCHAR(25) NOT NULL DEFAULT '-'
);
ALTER TABLE et_product add CONSTRAINT product_cur_shoppinglist_fk
    FOREIGN KEY(cur_shoppinglist_id) REFERENCES et_current_shoppinglist(cur_shoppinglist_id);
ALTER TABLE et_product add CONSTRAINT product_old_shoppinglist_fk
    FOREIGN KEY(old_shoppinglist_id) REFERENCES et_old_shoppinglist(old_shoppinglist_id);
ALTER TABLE et_product add CONSTRAINT product_inventorylist_fk
    FOREIGN KEY(inventorylist_id) REFERENCES et_inventorylist(inventorylist_id);

-- create product-tag table, for dietry preference filters e.g. VEGAN, PALEO, LOW CARB etc. 
CREATE TABLE IF NOT EXISTS et_product_tag(
                              product_id INTEGER REFERENCES et_product,
                              tag_id INTEGER REFERENCES et_dietry_tag,
                              PRIMARY KEY(product_id, tag_id)
);

-- create image table to store product images 
-- additionally photographs from user or product can be stored as bytes
CREATE TABLE IF NOT EXISTS et_product_image(
                        image_id INTEGER PRIMARY KEY NOT NULL,
                        product_id INTEGER NOT NULL,
                        image_name VARCHAR(25) NOT NULL DEFAULT 'Produktbild',
                        image_url TEXT NOT NULL DEFAULT 'https://cdn.pixabay.com/photo/2016/12/10/21/26/food-1898194_960_720.jpg',
                        image_bytes BYTEA
);
ALTER TABLE et_product_image add CONSTRAINT image_product_fk
    FOREIGN KEY(product_id) REFERENCES et_product(product_id);

-- automatically increment the id value start with 1
CREATE SEQUENCE IF NOT EXISTS et_users_seq MINVALUE 1 INCREMENT BY 1 CACHE 10;
CREATE SEQUENCE IF NOT EXISTS et_profile_settings_seq MINVALUE 1 INCREMENT BY 1 CACHE 10;
CREATE SEQUENCE IF NOT EXISTS et_recipe_seq MINVALUE 1 INCREMENT BY 1 CACHE 10;
CREATE SEQUENCE IF NOT EXISTS et_ingredient_seq MINVALUE 1 INCREMENT BY 1 CACHE 10;
CREATE SEQUENCE IF NOT EXISTS et_dietry_tag_seq MINVALUE 1 INCREMENT BY 1 CACHE 10;
CREATE SEQUENCE IF NOT EXISTS et_inventorylist_seq MINVALUE 1 INCREMENT BY 1 CACHE 10;
CREATE SEQUENCE IF NOT EXISTS et_shoppinglist_seq MINVALUE 1 INCREMENT BY 1 CACHE 10;
CREATE SEQUENCE IF NOT EXISTS et_current_shoppinglist_seq MINVALUE 1 INCREMENT BY 1 CACHE 10;
CREATE SEQUENCE IF NOT EXISTS et_old_shoppinglist_seq MINVALUE 1 INCREMENT BY 1 CACHE 10;
CREATE SEQUENCE IF NOT EXISTS et_product_seq MINVALUE 1 INCREMENT BY 1 CACHE 10;
CREATE SEQUENCE IF NOT EXISTS et_user_image_seq MINVALUE 1 INCREMENT BY 1 CACHE 10;
CREATE SEQUENCE IF NOT EXISTS et_product_image_seq MINVALUE 1 INCREMENT BY 1 CACHE 10;
