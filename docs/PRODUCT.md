# Product development document — Fudge, Pantry & Recipes

**Date:** 14 November 2026
**Commit of record for the original logic:**
`5839cf5c9de34e37b935c8d57b78aed3b90ace5c` (1 March 2026)

This document holds what the product must do, how much of it the code does
today, and what is still open. The description of the idea stands in
[`README.md`](../README.md); this document does not repeat it.

Read the state of a requirement like this:

| Mark | What it means |
|---|---|
| **Done** | The code does it, and a test proves it. |
| **Partly** | A part works. The row names the part that does not. |
| **Open** | The first version aimed at it, and no code does it yet. |
| **Future** | Named as a future feature. Nothing is expected today. |

---

## 1. The goal

A household throws away food that is still good, because nobody knows what is
in the refrigerator. Fudge makes that stock visible, watches each use-by date
and suggests what to cook before a product goes bad. The missing items of a
chosen recipe land on the shopping list.

**The measure of success:** fewer products reach their use-by date unused. Every
requirement in this document serves that number.

---

## 2. The user and the four moments

| Moment | What the user wants | The screen |
|---|---|---|
| After shopping | The products in the app with as little typing as possible | Inventory, add a product |
| During the week | To see what must go first | Dashboard |
| Before cooking | A recipe that follows the diet and uses what is there | Recipes, recipe detail |
| Before shopping | A list that holds what the recipes need | Shopping list |

---

## 3. Functional requirements

### FR-0 — Profile, dietary requirements and the rules of a suggestion

| # | The requirement | Acceptance criterion | State | Where in the code |
|---|---|---|---|---|
| 0.1 | Create and update a profile | A new user registers, logs in and sees their name | **Done** | `UserResource` `/register`, `/login`, `/update/userprofile` |
| 0.2 | Delete a profile | A user deletes the profile, and every row of that user goes with it | **Open** | No endpoint. A delete exists only for a product of the inventory and of a list. |
| 0.3 | Upload a custom profile image | The image survives a restart and shows on the profile | **Done** | `/update/image`, `Image`, `ImageUtils` |
| 0.4 | Sign in with another provider, for example Gmail, and import name and image **with the permission of the user** | The user sees what is imported before it is stored, and can refuse each field | **Open** | Login is email, password and a JWT. No OAuth. |
| 0.5 | Edit the name of the profile | The new name shows on every screen that names the user | **Done** | `/update/userprofile` |
| 0.6 | Set dietary requirements, and mark a requirement as **medical** | A requirement is stored with its kind. A medical one is a **veto**: it is never traded against anything. A requirement that is a taste is a **weight**, see FR-0.7. | **Partly** (14 November 2026) — the kind, the weight and the mark are stored and reach the API; the weighted choice is open | `et_dietry_tag.kind`, `et_user_tag.is_medical`, `Tag`, `/create/dietrytag` |
| 0.7 | **Weighted preferences.** The user picks up to three tags and says how important each one is, in percent, adding up to 100: *Vegan 70, Pescatarian 20, High protein 10*. | Of three suggestions, two follow the wish of 70 % and one is free for the best score. A preference never hides a recipe; it ranks. The percentages are a **target over many suggestions**, not a promise for each single one. | **Partly** (14 November 2026) — `et_user_tag.weight_percent` holds the weights and the API reads them; nothing uses them for the choice yet. See FR-R1. | `et_user_tag`, `Tag.weightPercent` |
| 0.8 | Set how often and when a notification comes, for example every day at 08:00 of the **local** time of the telephone | The notification arrives inside the set hour. With nothing set, the default of FR-D holds. | **Partly** — the switches exist; the hour stands in the code as 07:03, and the work runs once and never again | `/update/suggestion`, `/update/reminder`, `HiltApplication.setupWork`, `RecipeWorker`, `PushWorker` |
| 0.9 | Switch the automatic export of missing items on or off | With the switch on, choosing a recipe fills the shopping list without another tap | **Open** | `Settings` holds three switches, and this is not one of them. See FR-3.6. |
| 0.10 | The settings that already exist stay | Push notifications, remind before a product expires, suggest products for the shopping list | **Done** | `Settings` |
| 0.11 | *(gone into FR-0.7 on 14 November 2026.)* A share "outside the requirements" is no longer a thing of its own: a wish of 20 % for Pescatarian **is** the permission to serve fish. A recipe that follows a wish of less than 100 % still carries its reason on the screen, so the user sees why it is there. | | **Open** | — |
| 0.12 | The language of the application. **English** is the language of the product; further languages come later. | The country of the login chooses the variant — the United Kingdom gives British English, Australia gives Australian English — and the user can change it in the settings. No text stands in the code; every text stands in a file of its language. | **Open** | The application holds no setting of a language, and the demo data was German until 14 November 2026. |
| 0.13 | Day mode, night mode, or the mode of the telephone | The user chooses one of the three, and the choice holds after a restart | **Partly** — `values-night/colors.xml` holds the colours of the night, and no setting chooses it | `values-night/colors.xml` |
| 0.15 | Name an item that the user cannot eat, also one that no list foresaw | A recipe that holds the item never appears, on no screen and in no notification. A preset such as "Nut-free" uses the list of the rules file; a label that the file does not know forbids the ingredient that it names, so a user can write "Celery". | **Done** (14 November 2026) | `DietRules.medicalConflicts`, `diet-rules.properties`, `RecipeSuggestionServiceImpl` |
| 0.16 | Offer an alternative ingredient instead of hiding the recipe | A user who cannot digest lactose sees "use oat cream" on a recipe with cream, and the recipe stays | **Future** | — |
| 0.14 | The settings that a modern application holds | The settings screen holds: the account and the profile (FR-0.1 to FR-0.5), the dietary requirements (FR-0.6), the notification and its hour (FR-0.8), the language (FR-0.12), the appearance (FR-0.13), the units of measure, the data and the privacy, the version of the application, log out, and delete the account (FR-0.2) | **Open** | The profile screen holds a part of this list today. |

> **The language of the product is English.** German stood in the first
> version because it was shown in Germany, and it is gone from the demo data
> since 14 November 2026. The country of the login chooses the variant, the
> user can change it, and a language that nobody has translated falls back to
> English. A text in the source code is a defect: a text belongs in the file
> of its language, so that a translation needs no change of the code.

> **A requirement is either a veto or a weight.**
>
> | Kind | How it acts | Example |
> |---|---|---|
> | **medical** | A **veto**. No weight, no exception, never traded against an expiring product. | an allergy, coeliac disease, diabetes |
> | **a taste** | A **weight** from 1 to 100. It ranks a recipe, it hides none. | Vegan 70, Pescatarian 20, High protein 10 |
>
> Without the mark, the application treats a requirement as medical and never
> breaks it. That is the safe side.
>
> **Where a rule is in doubt, it forbids.** A suggestion that a user cannot
> eat is worse than a suggestion that never arrives. Two decisions follow
> from that rule, and both are written in `diet-rules.properties`: coconut is
> **not** rescued from a nut allergy, because some people who react to tree
> nuts react to it; and `diabetic-friendly` forbids nothing **on purpose**,
> because the carbohydrate of a portion decides and the application does not
> hold that number yet — so the requirement is visibly undecided instead of
> silently passing.
>
> **An exception is an exception, not a permission.** The key `despite` of the
> rules file rescues a false positive — "coconut milk" holds "milk",
> "buckwheat" holds "wheat". A word that one requirement forbids and rescues
> at the same time **stops the start of the application**, because such a
> contradiction would quietly serve a user what they cannot eat.
>
> One more share stands beside the weights and does not mix with them: FR-4.3
> keeps 10 % of the suggestions for recipes with **no rating**, so that a new
> recipe can be found at all.
>
> **The three tables of the tags** (14 November 2026):
>
> | Table | What it holds |
> |---|---|
> | `et_dietry_tag` | One vocabulary. `kind` says what a tag is: `diet`, `medical`, `nutrition`, `cuisine`, `meal`, `attribute`. |
> | `et_recipe_tag` | The three or four tags of a recipe, which the recipe screen shows. |
> | `et_user_tag` | What a user wants: the tag, `weight_percent`, and `is_medical`. |
>
> **The rules of a diet stay out of the database.** `diet-rules.properties`
> holds which ingredients a diet forbids, because that is reference data that
> a person reviews, that must not drift between two environments, and that a
> test reads without a database (N-14). The database holds facts only.
>
> **The rule for the screen:** a recipe carries **at most one** tag of the
> kind `diet`, and it is the most exact one that holds — `Vegan`, and never
> also `Vegetarian` or `Meat-free`. Beside it stand at most three tags of
> **different** families: nutrition, effort, cuisine or meal. Four at most.

### FR-1 — Collecting the products after shopping

| # | The requirement | Acceptance criterion | State | Where |
|---|---|---|---|---|
| 1.1 | Read a product and its details from a barcode | The scan gives the name and the nutrition value | **Done** | `BarcodeRestResource` `/api/barcode/rest/{barcode}`, Edamam |
| 1.2 | Read a barcode from a photo, when the scanner cannot | A photo of a crumpled label still gives the product | **Open** | — |
| 1.3 | Detect every product of a basket in **one shot**, on the device | One photo of the basket gives a list of labelled products | **Future** — see FR-8 | — |
| 1.4 | An expiration engine estimates the shelf life **by type of product and by storage place** when no date can be read | Each product carries a date: read, or estimated and marked as an estimate. The rules stand under this table. | **Open** | Today the date comes from the user. `EXPIRATION_WARNING_DAYS = 3` only sets the warning window. |
| 1.5 | The app works in the background and asks **only** when it is unsure or a field is missing | No screen asks for something the app could know. Every field arrives **pre-filled** with the likely value, and every field stays editable. | **Open** | `AddProductToInventoryFragment` is an empty form |

#### The expiration engine of FR-1.4

The engine must never let a user eat food that is bad. Where a rule is in
doubt, the **earlier** date wins.

| # | The rule | Why |
|---|---|---|
| 1.4.1 | The estimate belongs to the **type**, not to the single product. A cucumber keeps about 5 days, a fresh sourdough bread 3 days unfrozen. | A type is a thing one can look up once; a single product is not. |
| 1.4.2 | The shelf life of each type is read from the internet **once**, reviewed by a person and stored in a versioned file that ships with the app, for example `shelf-life.yaml`. No request at run time. | The app must not depend on an external service for a safety value, and it must work without a network. See N-14. |
| 1.4.3 | The **storage place** changes the date: freezer, refrigerator, pantry, kitchen counter. One type therefore carries several values. | A bread on the counter and a bread in the freezer are not the same product. |
| 1.4.4 | **The shorter estimate wins.** Where two rules could apply, the engine takes the earlier date. | A day too early costs a little food. A day too late costs the health of the user. |
| 1.4.5 | An estimated date is **marked as an estimate** on every screen that shows it, and the user can correct it. | The user must be able to tell a guess from a printed date. |
| 1.4.6 | An unknown type gets the shortest shelf life of its group, and the app asks the user. | FR-1.5 allows a question when the app is unsure. |

**The edge cases.** Each line needs its own test.

| The case | The rule |
|---|---|
| The package is opened | The "after opening" value counts, from the day of opening |
| Frozen, then thawed | The date starts again on the day of thawing, with the fresh value and never the frozen one |
| Moved from the pantry into the refrigerator, or the other way | The date is worked out again from the day of the move |
| Cooked food and leftovers | Three days in the refrigerator, and the user can shorten it |
| Salt, sugar, dried pasta, honey, tinned food | No date, and such a product never appears in a warning |
| A printed date that the camera read badly | A printed date counts only when it was read with high confidence. Otherwise the **earlier** of the printed date and the estimate counts. |
| Raw meat, fish, soft cheese, a raw egg dish | The group with the shortest shelf life. Without a printed date, never more than two days. |
| Loose fruit and vegetables from a market, with no package | The type decides, with the value for the kitchen counter |
| A date that already lies in the past when the product is scanned | The product is marked as bad at once, and the user confirms or corrects |
| A product that holds several parts with different dates | The earliest part decides for the whole product |
| The telephone travels into another time zone | A use-by date is a **day**, never an hour, so it does not move |

### FR-2 — The virtual inventory

| # | The requirement | Acceptance criterion | State | Where |
|---|---|---|---|---|
| 2.1 | A scanned product lands in the inventory by itself, with every field **pre-filled** | After a scan the product stands in the list with name, type, quantity, storage place and date already filled in | **Partly** — the user fills an empty form by hand | `InventoryResource` `/add`, `AddProductToInventoryFragment` |
| 2.2 | The user can change **any** field of **any** product at **any** time, also long after the app entered it | Every field of every product of the inventory is editable, and an automatic value is never locked | **Done** | `InventoryResource` `/update` |
| 2.3 | Without a confirmation the product is confirmed by itself after a time the user sets, for example 3 hours | An unconfirmed product becomes confirmed after that time, with no tap | **Open** | No state "unconfirmed" exists |
| 2.4 | The app tracks the use-by date, the type and the quantity | The dashboard counts the products that expire within the window | **Partly** — date and type work, the quantity never changes | `InventoryRepositoryImpl.getProductWithShortExpDate`, `countExpiringProducts` |
| 2.5 | The quantity falls by what a recipe used: two eggs of seven leave five | After the user cooks a recipe, the inventory holds the rest | **Open** | No code lowers a quantity |

### FR-3 — The recipe suggestion

| # | The requirement | Acceptance criterion | State | Where |
|---|---|---|---|---|
| 3.1 | **Every** suggestion follows the dietary requirements strictly | A vegan user never sees a recipe with meat, in no screen and in no notification | **Done** (14 November 2026) | `common/diet/DietRules` reads `diet-rules.properties` and judges the **ingredients** of a recipe, not its label. `RecipeSuggestionService` stands in front of the three endpoints that give recipes to a user. A requirement that the file does not name is a taste, such as "Mediterranean", and hides nothing. |
| 3.2 | A suggestion uses the products of the inventory | The ingredients of a suggestion come from the inventory of that user | **Done** | `RecipeRestService.getRecipesByExpiringProducts` |
| 3.3 | Products that expire soon come first | A product with three days left steers the suggestion | **Done** | `EXPIRATION_WARNING_DAYS = 3`, `getProductWithShortExpDate` |
| 3.4 | A suggestion covers as many products of the household as possible | Of two recipes, the one that uses more of the stock stands higher | **Partly** | `findByIngredients` can rank by that, but the `ranking` parameter is not sent |
| 3.5 | At least three recipes, and as many as the user set | The list never holds fewer than three | **Partly** — three is the fixed number | `RECIPES_PER_SEARCH = 3`; no setting for more |
| 3.6 | Choosing a recipe exports the missing items to the shopping list | Of eight ingredients, the three that the inventory lacks appear on the list | **Partly** (14 November 2026) — the API does it, the client does not call it yet | `common/utils/IngredientMatcher`, `RecipeShoppingService`, `POST /api/shoppinglist/current/add/missing` |
| 3.8 | With an empty inventory, or with nothing that needs attention, the suggestion follows the dietary requirements and the score of the recommender **alone** | A user with an empty pantry still sees at least three recipes that fit the diet | **Open** | The suggestion of the dashboard starts from the expiring products. The random path `getNRandomRecipes` knows neither the diet nor a score. |
| 3.7 | The suggestions **and** the products that expire soon reach the user as a push notification at the set hour | One notification a day. It names the number of products that expire soon and at least one recipe, and a tap opens the screen that belongs to it. | **Partly** — see the three faults under this table | `PushWorker`, `RecipeWorker`, `HiltApplication` |

> **The notification, after the work of 14 November 2026.**
>
> | The fault | Where it stands |
> |---|---|
> | `PushWorker` ran every **15 minutes**, not at an hour of the day | **Done.** One work a day, at 08:00 of the local time. `ExistingPeriodicWorkPolicy.UPDATE` replaces the old plan on a telephone that still holds it. |
> | `POST_NOTIFICATIONS` was **not** in the manifest, so Android 13 and later dropped every notification without a word | **Done.** The manifest declares it, `MainActivity` asks the user once, and `PushWorker` asks whether it may notify before it does. |
> | `RecipeWorker` was a one-time work for 07:03 and ran **once, ever** | **Done.** One work a day at 07:45, a quarter of an hour before the notification, so that the suggestions are fresh. |
> | The **hour of the user** | **Open.** Every user receives the default of 08:00. The hour needs a column in `et_profile_settings`, which holds three boolean columns today and no time. |
> | The switch `push_notifications` | **Open.** The worker notifies whenever products expire, and does not read the switch yet. |
> | The **content** of the notification | **Open.** It names the number of expiring products. FR-3.7 also asks for at least one recipe. |

### FR-4 — Bookmark and like

| # | The requirement | Acceptance criterion | State | Where |
|---|---|---|---|---|
| 4.1 | Bookmark a recipe and see the bookmarks together | A bookmark survives a restart and shows in one list | **Partly** | `FavoritesFragment`, `RecipeRepositoryImpl` |
| 4.2 | Like or rate a recipe | A like or a mark changes the rank of that recipe for that user | **Open** | `/api/recipe/rate` exists. The client never calls it. |
| 4.3 | A higher rating makes a recipe appear more often, and **a share of the suggestions carries no rating at all**, so that a new recipe can be found | Over twenty suggestions, about two hold no rating. The share is the default of FR-D. | **Open** | — |
| 4.5 | The screen of a recipe shows its tags | A user who opens a recipe sees its three or four tags, the diet first | **Open** — the data stands in `et_recipe_tag` since 14 November 2026; no endpoint reads it and no layout shows it | `et_recipe_tag` |
| 4.4 | The app counts how often a recipe was **viewed**, next to the rating, the like and the bookmark | A view counts once per user and per opening, and it feeds FR-R1 | **Open** | `et_recipe` holds `cummulativerating` and `amountofratings`, and no counter of views |

### FR-5 — Share a recipe with others

**Future.** A recipe leaves the app as a link or a text, and the receiver sees
the ingredients and the steps. The share **inside** the app — to one person or
to many people of the following list — belongs to FR-10.

### FR-6 — Search and filter

| # | The requirement | Acceptance criterion | State | Where |
|---|---|---|---|---|
| 6.1 | Search for a recipe | A word of the title finds the recipe | **Partly** | `SearchListFragment`, `top_search_menu.xml` |
| 6.2 | Filter by **rating**, by **meal type**, by category and by diet | A filter narrows the list, and the screen names the active filters and the number of results that remain | **Open** | — |
| 6.3 | Find a user by a **profile tag** or by a name, and filter by user | Every user carries a tag that is unique, and a search for the tag finds the profile | **Future** | — |

The meal types — quick meal, breakfast, lunch, dinner, snack, dinner party
and the rest — are researched once and stored in a versioned file that ships
with the app, for example `meal-types.yaml`. That is the same pattern as the
shelf life of FR-1.4.2, and the same requirement N-14 holds for both.

### FR-7 — Write an own recipe, with photos and videos

**Future.** The user writes a recipe, adds pictures and a video, and the recipe
behaves like any other recipe of the app.

### FR-8 — One-shot detection of the products of a basket

**Future.** A model on the device detects every product of one photo, and reads
the ingredients and the use-by date from a printed or a stuck-on label. It must
run on the device, so that no picture of a kitchen leaves the telephone.

### FR-9 — An own store for barcodes, ingredients and recipes

**Future.** Today PostgreSQL holds the users, the inventory, the lists and the
tags, while every recipe and every barcode comes from Spoonacular or Edamam at
the moment of the request. Those APIs cost money, hold a rate limit and can
fail. The store makes the app independent of them, and it is the condition for
a useful offline mode.

### FR-10 — Publish, share and follow

**Future.** A user makes an own recipe public, or shares it with one person or
with many people of their following list. A follower learns of a new recipe in
two ways: a push notification, and a ring around the picture of the profile, as
a social app shows a new story. The ring disappears once the follower has seen
the recipe.

The app is meant to carry a social character. FR-5, FR-10, FR-11 and FR-R2
therefore form **one** piece of work: they all need an account that lives on a
server, a following list and a feed. None of them can be built alone.

### FR-11 — Follow a user

**Future.** A user follows another user and learns of a new recipe of that user.

### FR-12 — Suggestions for the shopping list out of the habits of the user

**Future.** The knowledge comes from **our own** database, not from an external
service: what a user buys, how often, and in which amount.

A shopping list holds two kinds of item, and the app learns from both:

1. what the app took out of a recipe that the user chose for today or tomorrow,
2. what the user wrote on the list by hand.

**The example.** A user buys 500 g of tofu every week. Before the next weekly
list the app looks at how much tofu the inventory still holds, compares that
with what the recipes of the week need, and offers **500 g of tofu**. The user
confirms it, changes the amount, or refuses it. A refusal is a signal as well,
and the model learns from it.

The switch `suggestProductsForShoppingList` of `Settings` is the hook that
exists already. FR-9 is the condition: without an own store of the products
there is nothing to learn from.

### FR-R1 — The recommender of recipes (future)

One score decides which recipe a user sees, out of hard rules and soft signals.

| The input | How it counts |
|---|---|
| The dietary requirements | **A hard rule.** A recipe that breaks them is never suggested, with the single exception of FR-0.11, and then it is marked. |
| The products of the inventory, the ones that expire first | A hard preference. See FR-3.2 and FR-3.3. |
| The rating of a recipe | A higher rating gives a higher rank |
| The likes and the bookmarks of this user | They raise what is similar to them |
| How often this user **viewed** a recipe | FR-4.4 |
| What **similar users** liked | The similarity comes from FR-R2 |
| A share of recipes with **no** rating | The default of FR-D, so that a new recipe can be found at all |

With an empty inventory the first two inputs fall away, and the score alone
decides. That is FR-3.8.

### FR-R2 — The recommender of profiles (future)

**Future.** The app compares the profiles of its users: what they liked, what
they cooked, which dietary requirements they hold. Out of that comes a
similarity, and the app suggests the profiles that stand closest to the user,
so that the user can look at them and follow them.

The same similarity feeds FR-R1: a recipe that a close profile liked ranks
higher. The two recommenders are therefore **two** systems with one shared
measure of similarity, and neither replaces the other.

### FR-D — The default values

A user who sets nothing must still get a useful application. Every setting
therefore holds a default, and the default must be useful on the first day.

| The setting | The default | Why this value |
|---|---|---|
| Notifications | **on**, as soon as the user grants the permission of the system | A pantry that says nothing is forgotten. The user can switch it off. |
| The hour of the notification | **08:00 of the local time** | The morning leaves the whole day to act, and the user can shop after work. |
| Days of warning before the use-by date | **3 days** | The value of the first version, `EXPIRATION_WARNING_DAYS`. |
| Recipes in a suggestion | **3** | The minimum of FR-3.5, and the value of `RECIPES_PER_SEARCH`. |
| Share of random suggestions | **10 %**, inside the dietary requirements | The example of the product owner. |
| Automatic export of the missing items | **on** | The README describes it as the normal way. |
| Time until a product confirms itself | **3 hours** | The example of the product owner. |
| Dietary requirements | **none**, so every recipe fits | The application must not guess a diet. |
| Share of recipes **outside** the dietary requirements (FR-0.11) | **0 %** | A requirement can be medical. The user must ask for an exception. |
| Share of recipes with **no rating** (FR-4.3) | **10 %** | Without it a new recipe is never found. |
| The storage place of a new product | the usual place of its type out of the shelf-life file, else the pantry | FR-1.4.3 needs a place before it can give a date. |
| An estimated use-by date | always marked as an estimate | FR-1.4.5. The user must see what is a guess. |
| Suggestions for the shopping list (FR-12) | **off** | It needs the habits of the user first. |
| The language | the variant of the country of the login, else **English** | English is the language of the product, and a country gives the variant without a question. |
| The appearance | **the mode of the telephone** | The user has already chosen there, so the application does not ask again. |
| The units of measure | **metric**, and imperial for a country that uses it | A recipe of 500 gr means nothing to a user who weighs in ounces. |

Two rules about a default:

- **One place.** A default lives in the column of `et_profile_settings`, and
  the client reads it from the API. A constant in the client that disagrees
  with the column is an error.
- **The time zone travels with the hour.** The hour is the local time of the
  telephone. A notification that a server sends later must know that zone.

Today the three columns of `et_profile_settings` default to `FALSE`, so a new
user gets nothing at all. That changes with the column of the hour.

---

## 4. Design requirements

### D-13 — The user experience

The app must feel as if it makes the everyday task easier: healthy food and
less waste with less effort. Simple, clean, and still engaging.

| Criterion | How it is measured |
|---|---|
| No manual | A new user reaches their first product in the inventory in under two minutes, with no help |
| The important thing first | The products that expire soon stand on the first screen, above everything else |
| Each screen has one job | A screen says in its title what the user can do there |
| A known pattern | Bottom navigation, a list, a detail screen, a form. Nothing invented. |

### D-14 — The user interface

Simple, calm, modern, and fitting for healthy food. Important information must
be easy to see: the products that expire soon, and the dietary requirements.

| Criterion | How it is measured |
|---|---|
| One scale of distances | Every distance comes from `dimens.xml`, in steps of 4dp |
| A token, never a raw colour | A layout names `@color/text_primary`. Rule 5 of `CLAUDE.md`. |
| Contrast | Text against its background holds at least 4.5:1 |
| A state is never colour alone | `ExpiryStatus` carries a word as well as a colour, for a colour-blind user |
| Touch targets | At least 48dp |
| Dark mode | `values-night/colors.xml` holds its own values |

> **One decision is open, and it belongs to the product owner.**
> On 1 March the palette was pastel: sage `#9EDBB6`, cream `#F5E6CA`, dark
> green `#2F4F4F`. The design of record today,
> `frontend/doc/design/ui_design_ideas_002.png`, uses one strong emerald
> `#16A34A` on white. This requirement asks for "a pastel palette or calming".
> Emerald on white is modern and calm, and it is not pastel.
> Because every layout uses tokens, the choice costs a change in four files
> (`colors.xml`, `values-night/colors.xml`, `styles.xml`, `themes.xml`) and in
> the drawables — and **no change in the 51 layout files**. It must be decided
> before the layouts are rebuilt, not after.

---

## 5. Non-functional requirements

> **The rule of every change.** Every new feature and every change carries its
> own tests, and those tests cover the **edge cases** and not only the path
> that works. A change without such a test is not finished, and a review sends
> it back. Where this document holds a table of edge cases — the engine of
> FR-1.4 is the first of them — each line of that table needs its own test.

| # | The requirement | Acceptance criterion | State |
|---|---|---|---|
| N-1 | No error is swallowed | Every failure writes one log line with the ID of the request | **Done** — `Attempt`, `RequestLogFilter`, `AppLog` |
| N-2 | A request can be followed across both parts | One `X-Request-Id` stands in the log of the client and of the API | **Done** |
| N-3 | **Every new feature and every change carries tests that cover the edge cases, not only the happy path.** A change without a test is not finished. | The backend holds the gate at 100 % of the instructions and of the branches; a new file carries no finding; a review refuses a change whose tests walk the happy path alone | **Partly** — the client has 64 tests, no gate and no test of a ViewModel |
| N-4 | No secret in the source | Each key comes from the environment | **Done** — `ApiProperties`. `Constants.java` with `"xxxx"` is deleted. |
| N-5 | An endpoint of a user needs a token | Without a valid token an endpoint answers 401 | **Open** — `TokenService` and `AuthFilter` exist with tests, and the bean is **not registered**, so no endpoint needs a token |
| N-6 | A log line holds no personal data | No body, no `Authorization`, no `Cookie` | **Done** |
| N-7 | An import from another provider needs consent | The user sees and confirms each field before it is stored | **Open** — belongs to FR-0.4 |
| N-8 | The failure of an external API does not break a screen | A screen shows an empty state and a text, never a crash | **Partly** — the service gives an empty list; the screen shows nothing |
| N-9 | Independence from an external API | The app works when Spoonacular and Edamam answer nothing | **Open** — FR-9 |
| N-10 | The platform | Android API 27 to 34, Java 17, PostgreSQL, the API in Docker on port 80 | **Done** |
| N-11 | Speed | A screen shows its data in under one second on a middle-class telephone; a detection on the device under two seconds | **Open** — never measured |
| N-12 | Offline | The inventory and the shopping list are readable without a network | **Open** — every screen reads the API |
| N-14 | Reference data ships with the app and needs no network: the rules of the diet (FR-3.1), the shelf life of each type (FR-1.4.2) and the meal types (FR-6.2) live in a versioned file in the repository, read once from the internet and then reviewed by a person | A build without a network holds every value, and the file names its version and its source | **Partly** — `diet-rules.properties` holds its version and its date; the shelf life and the meal types are open |
| N-15 | A date that the engine estimates is never later than a date that was printed on the product, and the engine prefers the earlier of two possible dates | Every edge case of FR-1.4 holds a test | **Open** |
| N-13 | Maintainable | One place for each rule; Checkstyle, ktlint, detekt and Lint clean; each public type documented | **Done** — the two baselines hold 37 and 452 old findings, and they must fall |

---

## 6. How far the code stands from the original logic

The question: did the work of 11 to 14 November 2026 change how the product
behaves? The commit of record is `5839cf5` of 1 March 2026.

| The rule | 1 March 2026 | 14 November 2026 | Verdict |
|---|---|---|---|
| Recipes per search | `3`, written into the method | `RECIPES_PER_SEARCH = 3` | same |
| The window of the warning | 3 days | `EXPIRATION_WARNING_DAYS = 3` | same |
| Random recipes | 10 | `DEFAULT_RECIPE_AMOUNT = 10` | same |
| The diet in the query | not sent | not sent | same gap |
| The settings of a user | three switches | the same three | same |
| The quantity after cooking | never lowered | never lowered | same |
| The screens | 19 fragments | 19 fragments | same |
| The bottom navigation | shopping, inventory, recipes, dashboard | the same four | same |
| The 18 errors of `CLAUDE.md` §5.1 | endpoints stopped with an exception or gave wrong data | corrected, each with a test | **better** |
| The palette | pastel: sage, cream, dark green | emerald on white | **changed on purpose — D-14** |

**The answer: the business logic did not move.** Every number, every screen and
every rule of the first version stands. What changed is that the broken parts
of those rules now work, and that the colours follow a newer design. The one
open question is the palette of D-14.

---

## 7. The order of the work

| # | The work | Why it comes here |
|---|---|---|
| 1 | Decide D-14, pastel or emerald | It blocks the rebuilding of the 51 layouts. The tokens make the decision cheap today and expensive later. |
| 2 | FR-3.1 — the diet in the query | One parameter, and it is the central promise of the product. A vegan user must never see meat. |
| 3 | FR-3.7, FR-0.8 and FR-D — the daily notification at the set hour | One line in the manifest, one periodic work at the right hour, one column for the hour. Without it the product has no way to reach the user who does not open the app, and that is the moment the whole idea depends on. |
| 4 | N-5 — register the `AuthFilter` | Today every endpoint answers without a token. The client needs a change at the same time. |
| 5 | FR-3.6 and FR-0.9 — the automatic export of the missing items | The README promises it, and no code does it. |
| 6 | FR-2.5 — the quantity falls when a recipe is cooked | Without it the inventory drifts away from the refrigerator, and every suggestion after that is wrong. |
| 7 | The layouts of the design, and then the 452 and 37 findings | After 1. |
| 8 | FR-9 — the own store | It carries FR-12 and N-9 and N-12, and it stops the cost of the external APIs. |
| 9 | FR-1.4 and N-14 — the expiration engine, its file of shelf lives and its edge cases; then FR-1.5, FR-2.1, FR-2.3 and FR-0.7 — the automatic parts of the collecting | Each one needs a rule that somebody must define: how long does a product keep, how long until a confirmation. |
| 10 | FR-4.2, FR-6.2 — the like and the filter | Small, and they need no new structure. |
| 11 | FR-1.3 and FR-8 — the model on the device | The largest piece. It needs a model, a licence and a measurement on a real telephone. |
| 12 | FR-5, FR-7, FR-10, FR-11, FR-R2 — the social part as **one** piece | They all need an account on a server, a following list and a feed. Alone, none of them works. |
| 13 | FR-12 and FR-R1 — the two models | They need the own store of FR-9 and the counters of FR-4.4 first. A model without data learns nothing. |

**Opened by the work of 14 November 2026.** Each of these fits between the
items above, and none of them blocks the demonstration.

| The work | Why it is open |
|---|---|
| **The weighted choice of FR-0.7**: slots by weight, the urgency of the pantry, a reason on each suggestion | The data is there — `weight_percent` and `is_medical`. Nothing reads it for the choice yet, so a preference still acts like a veto and the behaviour of the application did not change. This is the first half of FR-R1 and needs no comparison between users. |
| **FR-4.5**: an endpoint that gives the tags of a recipe, and the chips on the screen | `et_recipe_tag` holds the data since today. `Recipe` carries no tags, and no layout shows them. |
| A model `DietaryPreference` instead of three fields on `Tag` | `Tag` carries `weightPercent` and `medical`, because the row of `et_user_tag` **is** the wish. It works, and it mixes the vocabulary with the wish of a user. |
| An own vocabulary for the tags of a **product** | A product carries dietary labels today, so the cheese of the demo data carries "Vegan". A product needs food groups: dairy, meat, fish, vegetable, fruit, grain. No screen shows a product tag, so nothing is visibly wrong yet. |
| `et_recipe.recipe_label` becomes redundant | A free text that lied once (`Boeuf Bourguignon` carried `Vegetarisch`). Once FR-4.5 shows the tags, the column can go. |
| **A contradiction in `diet-rules.properties`** | `vegetarian.allows` names `prawn, shrimp, crab, lobster, gelatin, lard`, and `vegetarian.forbids` forbids exactly those. An exception wins over a prohibition, so a vegetarian would be served prawns. Two steps are open: rename the key `allows` to `despite`, so nobody reads it as a list of permissions, and stop the start of the application when one word stands in both lists of one requirement. |

---

## 8. Where things are

| What | Where |
|---|---|
| The knowledge base of the code | [`CLAUDE.md`](../CLAUDE.md) |
| The open technical tasks | [`CLAUDE.md`](../CLAUDE.md) §7 |
| The design of record | `frontend/doc/design/ui_design_ideas_002.png` |
| The design system | `frontend/android-feedme-app/app/src/main/res/values/` |
| The endpoints | [`README.md`](../README.md) §REST API |
