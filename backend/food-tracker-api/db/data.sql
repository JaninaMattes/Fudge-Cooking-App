\connect foodtrackerdb;

CREATE EXTENSION pgcrypto;

INSERT INTO public.et_users (user_id, first_name, last_name, email, password, is_logged_in)
VALUES  
        (1, 'John', 'Smith', 'john.smith@testmail.com', crypt('testpassword', gen_salt('bf')), FALSE),
        (2, 'Annabelle', 'Smith', 'annabelle.smith@testmail.com', crypt('testpassword', gen_salt('bf')), FALSE);

INSERT INTO public.et_user_image(image_id, user_id, image_name, image_url, image_bytes)
VALUES 
        (1, 1, 'Profilbild', 'https://cdn.pixabay.com/photo/2017/08/12/18/31/male-2634974_960_720.jpg', NULL),
        (2, 2, 'Profilbild', 'https://cdn.pixabay.com/photo/2015/03/03/18/58/woman-657753_960_720.jpg', NULL);

INSERT INTO public.et_profile_settings (settings_id, user_id, reminder_product_expiration, suggest_recipes, push_notifications)
VALUES
        (1, 1, TRUE, TRUE, TRUE),
        (2, 2, TRUE, TRUE, TRUE);

INSERT INTO public.et_recipe (recipe_id, user_id, title, recipe_label, recipe_description, cummulativerating, amountofratings, cooking_time, difficulty, instruction, nutrition_value, image_url, favorite_recipe)
VALUES 
        (1, 1, 'Wildschweinbraten', 'Beliebt', 'Traditionelle Deutsche Küche', 4.7, 205, '60 min', 'einfach', 'Den Wildschweinbraten mit Salz, Pfeffer, Rosmarin, Thymian, den Knoblauchwürfelchen und dem Öl einreiben. Über Nacht zugedeckt im Kühlschrank oder einem kühlen Raum durchziehen lassen. Am nächsten Tag alles zusammen in einen Bräter (Fassungsvermögen 6 l) geben. Die Zwiebel- und Karottenwürfelchen dazugeben, den Bräter auf die Herdplatte stellen und das Fleisch mit den Zwiebeln und Karotten ringsum scharf anbraten. Das Fleisch dabei öfters wenden. Mit dem Rotwein und der Fleischbrühe oder dem Wildfond ablöschen und aufgießen. Die Tomatenstückchen dazugeben. Das Fleisch auf der Oberseite mit dem Honig einstreichen.', '854 kcal', 'https://cdn.pixabay.com/photo/2017/08/07/07/18/table-2600954_960_720.jpg', FALSE),
        (2, 2, 'Einfacher Zwiebelkuchen', 'Vegetarisch', 'Traditionelle Ungarische Küche', 4.9, 35, '45 min', 'einfach', 'Mehl, Hefe, Zucker und Salz in eine Schüssel geben. In die Mitte eine Mulde drücken und das kalte Wasser hineingeben. Die weiche Butter zufügen und alles zu einem Teig kneten. Den Teig ca. 30 Minuten ruhen lassen.', '600 kcal', 'https://cdn.pixabay.com/photo/2020/02/11/10/19/a-cake-of-bread-4839047_960_720.jpg', FALSE),
        (3, 2, 'Boeuf Bourguignon', 'Vegetarisch', 'Mit Pilzen in Rotwein geschmort', 4.5, 15, '45 min', 'mittel', '2 EL Öl in einem schweren Topf erhitzen. Das Fleisch darin portionsweise bei starker Hitze scharf anbraten. Fleisch jeweils herausnehmen und in ein Sieb geben, abtropfenden Fleischsaft dabei in einer Schüssel auffangen.', '460 kcal', 'https://images.eatsmarter.de/sites/default/files/styles/576x432/public/boeuf-bourguignon-smarter-5127-2.jpg', TRUE),
        (4, 1, 'Spaghettikürbis mit Hackfleisch', 'Beliebt', 'Eiweißreich und Low Carb', 4.8, 35, '45 min', 'mittel', '2 EL Öl in einem schweren Topf erhitzen. Das Fleisch darin portionsweise bei starker Hitze scharf anbraten. Fleisch jeweils herausnehmen und in ein Sieb geben, abtropfenden Fleischsaft dabei in einer Schüssel auffangen.', '250 kcal', 'https://images.eatsmarter.de/sites/default/files/styles/576x432/public/spaghettikuerbis-mit-hackfleisch-und-feta-671685-2.jpg', TRUE);

INSERT INTO public.et_ingredient (ingredient_id, ingredient_name, quantity)
VALUES 
        (1, 'Water', '100 ml'),
        (2, 'Salt', '1 tsp'),
        (3, 'Bread flour', '500 gr'),
        (4, 'sugar', '1 tsp'),
        (5, 'oil', '25 ml'),
        (6, 'eggs', '3 pieces'),
        (7, 'pepper', '2 tsp'),
        (8, 'baking powder', '1 package'),
        (9, 'Wild boar', '500 gr'),
        (10, 'Onions', '3 pieces'),
        (11, 'Potatoes', '1 Kg'),
        (12, 'Flour', '500 gr'),
        (13, 'Mushrooms', '500 gr'),
        (14, 'Garlic', '1 piece'),
        (15, 'Red wine', '250 gr'),
        (16, 'Beef', '500 gr'),
        (17, 'Pumpkin', '1 kg'),
        (18, 'Minced meat', '500 gr');

INSERT INTO public.et_recipe_ingredient (recipe_id, ingredient_id)
VALUES 
        (1, 1),
        (1, 2),
        (1, 3),
        (1, 4),
        (1, 5),
        (1, 6),
        (1, 7),
        (1, 8),
        (1, 9),
        (1, 11),
        (1, 10),
        (2, 1),
        (2, 2),
        (2, 3),
        (2, 4),
        (2, 5),
        (2, 6),
        (2, 7),
        (2, 8),
        (2, 12),
        (2, 10),
        (3, 10),
        (3, 13),
        (3, 14),
        (3, 15),
        (3, 16),
        (3, 4),
        (3, 7),
        (4, 14),
        (4, 7),
        (4, 13),
        (4, 18),
        (4, 17),
        (4, 16);

INSERT INTO public.et_dietry_tag (tag_id, label)
VALUES 
        (1, 'Vegan'),
        (2, 'Vegetarian'),
        (3, 'Mediterranean'),
        (4, 'Italian'),
        (5, 'Mediterranean'),
        (6, 'Low Carb'),
        (7, 'Fast Food'),
        (8, 'Asian'),
        (9, 'Paleo'),
        (10, 'Low Fat'),
        (11, 'Low Salt'),
        (12, 'No Pig'),
        (13, 'Kosher'),
        (14, 'Low sugar'),
        (15, 'Balanced'),
        (16, 'Fitness'),
        (17, 'Meat');

INSERT INTO public.et_user_tag (user_id, tag_id)
VALUES 
        (1, 1),
        (1, 2),
        (1, 3),
        (1, 6),
        (1, 17),
        (2, 1),
        (2, 5),
        (2, 10),
        (2, 16);

INSERT INTO public.et_inventorylist (inventorylist_id, user_id)
VALUES 
        (1, 1),
        (2, 2);

INSERT INTO public.et_shoppinglist(shoppinglist_id)
VALUES 
        (1),
        (2);

INSERT INTO public.et_current_shoppinglist(cur_shoppinglist_id, shoppinglist_id, user_id)
VALUES 
        (1, 1, 1),
        (2, 2, 2);

INSERT INTO public.et_old_shoppinglist(old_shoppinglist_id, shoppinglist_id, user_id)
VALUES 
        (1, 1, 1),
        (2, 2, 2);

INSERT INTO public.et_product (product_id, cur_shoppinglist_id, old_shoppinglist_id, inventorylist_id, product_name, expiration_date, quantity, manufacturer, nutrition_value)
VALUES 
        (1, NULL, 1, 1, 'Cheese', TO_DATE('08.02.2022', 'DD.MM.YYYY'), '1 Package', '-', '-'),
        (2, NULL, 1, 1, 'Sausage', TO_DATE('06.02.2022', 'DD.MM.YYYY'), '1 Package', '-', '-'),
        (3, NULL, 1, 1, 'Bread', TO_DATE('08.02.2022', 'DD.MM.YYYY'), '1 Package', '-', '-'),
        (4, NULL, 1, 1, 'Strawberry Jam', TO_DATE('25.01.2022', 'DD.MM.YYYY'), '1 Glas', '-', '-'),
        (5, NULL, 2, 1, 'Butter', TO_DATE('10.02.2022', 'DD.MM.YYYY'), '1 Package', '-', '-'),
        (6, NULL, 2, 2, 'Pizza', TO_DATE('10.02.2022', 'DD.MM.YYYY'), '1 Package', '-', '-'),
        (7, NULL, 2, 2, 'Mixed vegetables', TO_DATE('12.02.2022', 'DD.MM.YYYY'), '1 Package', '-', '-'),
        (8, NULL, 1, 2, 'Carrots', TO_DATE('10.02.2022', 'DD.MM.YYYY'), '1 Package', '-', '-'),
        (9, NULL, 2, 2, 'Red Wine', TO_DATE('24.02.2022', 'DD.MM.YYYY'), '1 Package', '-', '-'),
        (10, 2, NULL, NULL, 'Toast', TO_DATE('10.02.2022', 'DD.MM.YYYY'), '1 Package', '-', '-'),
        (11, 2, NULL, NULL, 'Muesli', TO_DATE('18.02.2022', 'DD.MM.YYYY'), '1 Package', '-', '-'),
        (12, 2, NULL, NULL, 'Noodles', TO_DATE('24.02.2022', 'DD.MM.YYYY'), '1 Package', '-', '-'),
        (13, 2, NULL, NULL, 'Spaghetti', TO_DATE('25.02.2022', 'DD.MM.YYYY'), '1 Package', '-', '-'),
        (14, 2, NULL, NULL, 'Orange Juice', TO_DATE('07.02.2022', 'DD.MM.YYYY'), '1 Package', '-', '-'),
        (15, 1, NULL,  NULL, 'Kale', TO_DATE('14.02.2022', 'DD.MM.YYYY'), '1 Package', '-', '-'),
        (16, 1, NULL, NULL, 'Banana', TO_DATE('08.02.2022', 'DD.MM.YYYY'), '1 Kg', '-', '-'),
        (17, 1, NULL, NULL, 'Apple', TO_DATE('10.02.2022', 'DD.MM.YYYY'), '1 Package', '-', '-'),
        (18, 1, NULL, NULL, 'Pistazien', TO_DATE('26.02.2022', 'DD.MM.YYYY'), '1 Package', '-', '-'),
        (19, NULL, 1, 1, 'Nuts', TO_DATE('10.02.2022', 'DD.MM.YYYY'), '1 Package', '-', '-'),
        (20, NULL, 1, 1, 'Cream Cheese', TO_DATE('06.02.2022', 'DD.MM.YYYY'), '1 Package', '-', '-'),
        (21, NULL, 1, 1, 'Chicken', TO_DATE('06.02.2022', 'DD.MM.YYYY'), '1 Package', '-', '-'),
        (22, NULL, 1, 1, 'Salmon', TO_DATE('06.02.2022', 'DD.MM.YYYY'), '1 Package', '-', '-'),
        (23, NULL, 2, 1, 'Pizza', TO_DATE('15.02.2022', 'DD.MM.YYYY'), '1 Package', '-', '-'),
        (24, NULL, 2, 2, 'Sushi', TO_DATE('10.02.2022', 'DD.MM.YYYY'), '1 Package', '-', '-'),
        (25, NULL, 2, 2, 'Green Curry', TO_DATE('06.02.2022', 'DD.MM.YYYY'), '1 Package', '-', '-'),
        (26, NULL, 2, 2, 'Garlic', TO_DATE('19.02.2022', 'DD.MM.YYYY'), '1 Package', '-', '-'),
        (27, NULL, 2, 2, 'Strawberries', TO_DATE('10.02.2022', 'DD.MM.YYYY'), '1 Package', '-', '-'),
        (28, 2, NULL, NULL, 'Baklava', TO_DATE('22.02.2022', 'DD.MM.YYYY'), '1 Package', '-', '-'),
        (29, 2, NULL, NULL, 'White Wine', TO_DATE('30.03.2022', 'DD.MM.YYYY'), '1 Package', '-', '-'),
        (30, 2, NULL, NULL, 'Lemon', TO_DATE('08.02.2022', 'DD.MM.YYYY'), '1 Package', '-', '-'),
        (31, 2, NULL, NULL, 'Garlic', TO_DATE('06.02.2022', 'DD.MM.YYYY'), '1 Package', '-', '-'),
        (32, 1, NULL, NULL, 'Onions', TO_DATE('09.02.2022', 'DD.MM.YYYY'), '1 Package', '-', '-'),
        (33, 1, NULL, NULL, 'Cream Cheese', TO_DATE('06.02.2022', 'DD.MM.YYYY'), '1 Package', '-', '-'),
        (34, 1, NULL, NULL, 'Eggs', TO_DATE('07.02.2022', 'DD.MM.YYYY'), '1 Package', '-', '-'),
        (35, 1, NULL, NULL, 'Flour', TO_DATE('31.03.2022', 'DD.MM.YYYY'), '1 Package', '-', '-'),
        (36, 1, NULL, NULL, 'Cat Food', TO_DATE('22.05.2022', 'DD.MM.YYYY'), '1 Package', '-', '-'),
        (37, NULL, NULL, 1, 'Rice', TO_DATE('07.02.2022', 'DD.MM.YYYY'), '1 Package', '-', '-'),
        (38, NULL, NULL, 1, 'Steak', TO_DATE('08.02.2022', 'DD.MM.YYYY'), '1 Package', '-', '-');

INSERT INTO public.et_product_tag (product_id, tag_id)
VALUES 
        (1, 1),
        (1, 2),
        (1, 3),
        (2, 2),
        (2, 4),
        (3, 3),
        (4, 4),
        (5, 5),
        (6, 6),
        (7, 7),
        (8, 8),
        (9, 9),
        (10, 10),
        (11, 11),
        (12, 12),
        (13, 13),
        (14, 14),
        (15, 5),
        (16, 16),
        (17, 1),
        (18, 2),
        (19, 3),
        (20, 4),
        (21, 5),
        (22, 6),
        (23, 7),
        (24, 8),
        (25, 9),
        (26, 10),
        (27, 11),
        (28, 12),
        (29, 13),
        (30, 14),
        (31, 15),
        (32, 16),
        (33, 1),
        (34, 12),
        (35, 3),
        (36, 4),
        (36, 16),
        (37, 1),
        (38, 17);

INSERT INTO public.et_product_image (image_id, product_id, image_name, image_url, image_bytes)
VALUES 
        (1, 1, 'Produktbild', 'https://cdn.pixabay.com/photo/2017/01/11/19/56/cheese-1972744_960_720.jpg', NULL),
        (2, 2, 'Produktbild', 'https://cdn.pixabay.com/photo/2017/08/15/20/16/salami-2645403_960_720.jpg', NULL),
        (3, 3, 'Produktbild', 'https://cdn.pixabay.com/photo/2016/03/26/18/23/bread-1281053_960_720.jpg', NULL),
        (4, 4, 'Produktbild', 'https://cdn.pixabay.com/photo/2015/06/27/17/22/jam-823653_960_720.jpg', NULL),
        (5, 5, 'Produktbild', 'https://cdn.pixabay.com/photo/2018/02/25/07/15/food-3179853_960_720.jpg', NULL),
        (6, 6, 'Produktbild', 'https://cdn.pixabay.com/photo/2017/09/30/15/10/plate-2802332_960_720.jpg', NULL),
        (7, 7, 'Produktbild', 'https://cdn.pixabay.com/photo/2016/08/11/08/04/vegetables-1584999_960_720.jpg', NULL),
        (8, 8, 'Produktbild', 'https://cdn.pixabay.com/photo/2017/02/28/20/59/carrots-2106825_960_720.jpg', NULL),
        (9, 9, 'Produktbild', 'https://cdn.pixabay.com/photo/2016/10/22/20/34/wines-1761613_960_720.jpg', NULL),
        (10, 10, 'Produktbild', 'https://cdn.pixabay.com/photo/2026/11/29/04/00/bread-1867208_960_720.jpg', NULL),
        (11, 11, 'Produktbild', 'https://cdn.pixabay.com/photo/2028/02/18/21/37/muesli-3091299_960_720.jpg', NULL),
        (12, 12, 'Produktbild', 'https://cdn.pixabay.com/photo/2020/12/13/10/00/pasta-2093_960_720.jpg', NULL),
        (13, 13, 'Produktbild', 'https://cdn.pixabay.com/photo/2020/12/13/10/00/pasta-2093_960_720.jpg', NULL),
        (14, 14, 'Produktbild', 'https://cdn.pixabay.com/photo/2027/05/21/16/52/juice-2331722_960_720.jpg', NULL),
        (15, 15, 'Produktbild', 'https://cdn.pixabay.com/photo/2028/10/03/21/57/cabbage-3722498_960_720.jpg', NULL),
        (16, 16, 'Produktbild', 'https://cdn.pixabay.com/photo/2028/09/24/20/12/bananas-3700718_960_720.jpg', NULL),
        (17, 17, 'Produktbild', 'https://cdn.pixabay.com/photo/2026/10/27/22/52/apples-1776744_960_720.jpg', NULL),
        (18, 18, 'Produktbild', 'https://cdn.pixabay.com/photo/2028/03/13/20/08/pistachios-3223610_960_720.jpg', NULL),
        (19, 19, 'Produktbild', 'https://cdn.pixabay.com/photo/2018/03/13/20/08/pistachios-3223610_960_720.jpg', NULL),
        (20, 20, 'Produktbild', 'https://cdn.pixabay.com/photo/2015/02/10/02/42/cheese-630511_960_720.jpg', NULL),
        (21, 21, 'Produktbild', 'https://cdn.pixabay.com/photo/2020/05/22/12/16/chicken-drumsticks-5205207_960_720.jpg', NULL),
        (22, 22, 'Produktbild', 'https://cdn.pixabay.com/photo/2016/03/05/19/02/salmon-1238248_960_720.jpg', NULL),
        (23, 23, 'Produktbild', 'https://cdn.pixabay.com/photo/2018/04/11/03/13/food-3309418_960_720.jpg', NULL),
        (24, 24, 'Produktbild', 'https://cdn.pixabay.com/photo/2018/08/03/08/33/food-3581341_960_720.jpg', NULL),
        (25, 25, 'Produktbild', 'https://cdn.pixabay.com/photo/2021/07/04/13/23/green-curry-6386360_960_720.jpg', NULL),
        (26, 26, 'Produktbild', 'https://cdn.pixabay.com/photo/2013/06/16/13/53/garlic-139659_960_720.jpg', NULL),
        (27, 27, 'Produktbild', 'https://cdn.pixabay.com/photo/2017/11/18/17/09/strawberries-2960533_960_720.jpg', NULL),
        (28, 28, 'Produktbild', 'https://cdn.pixabay.com/photo/2020/05/11/15/06/food-5158702_960_720.jpg', NULL),
        (29, 29, 'Produktbild', 'https://cdn.pixabay.com/photo/2026/10/22/20/34/wines-1761613_960_720.jpg', NULL),
        (30, 30, 'Produktbild', 'https://cdn.pixabay.com/photo/2027/02/05/12/31/lemons-2039830_960_720.jpg', NULL),
        (31, 31, 'Produktbild', 'https://cdn.pixabay.com/photo/2023/06/16/13/53/garlic-139659_960_720.jpg', NULL),
        (32, 32, 'Produktbild', 'https://cdn.pixabay.com/photo/2026/03/05/19/14/onions-1238332_960_720.jpg', NULL),
        (33, 33, 'Produktbild', 'https://cdn.pixabay.com/photo/2026/06/03/14/32/cheese-tray-1433504_960_720.jpg', NULL),
        (34, 34, 'Produktbild', 'https://cdn.pixabay.com/photo/2026/03/09/09/18/eggs-1245719_960_720.jpg', NULL),
        (35, 35, 'Produktbild', 'https://cdn.pixabay.com/photo/2025/02/14/19/20/bake-599521_960_720.jpg', NULL),
        (36, 36, 'Produktbild', 'https://cdn.pixabay.com/photo/2020/02/03/23/04/cat-food-4739337_960_720.jpg', NULL),
        (37, 37, 'Produktbild', 'https://cdn.pixabay.com/photo/2021/01/16/09/05/meal-5921491_960_720.jpg', NULL),
        (38, 38, 'Produktbild', 'https://cdn.pixabay.com/photo/2018/02/08/15/02/meat-3139641_960_720.jpg', NULL);