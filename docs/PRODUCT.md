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
| 0.6 | Set dietary requirements, for example low-carb, diabetic, vegan | The requirements are stored and **every** suggestion follows them | **Partly** — stored, never applied. See FR-3.1. | `/create/dietrytag`, `/update/dietrytag`, `Tag`, `UserFoodPrefListFragment` |
| 0.7 | Set a share of random suggestions, for example 10 %, which still follows the diet | Over 20 suggestions, about 2 are random, and none breaks the diet | **Open** | `getNRandomRecipes` can give random recipes, but no share and no diet |
| 0.8 | Set how often and when a notification comes, for example every day at 08:00 of the **local** time of the telephone | The notification arrives inside the set hour. With nothing set, the default of FR-D holds. | **Partly** — the switches exist; the hour stands in the code as 07:03, and the work runs once and never again | `/update/suggestion`, `/update/reminder`, `HiltApplication.setupWork`, `RecipeWorker`, `PushWorker` |
| 0.9 | Switch the automatic export of missing items on or off | With the switch on, choosing a recipe fills the shopping list without another tap | **Open** | `Settings` holds three switches, and this is not one of them. See FR-3.6. |
| 0.10 | The settings that already exist stay | Push notifications, remind before a product expires, suggest products for the shopping list | **Done** | `Settings` |
| 0.11 | Allow a share of suggestions that lies **outside** the dietary requirements, for example 10 % of the time | Every such recipe carries a clear mark, "outside your requirements", and the share stays at 0 % until the user sets it | **Open** | — |

> **Three shares, and they must never be confused.**
>
> | The share | What it mixes in | Default |
> |---|---|---|
> | FR-0.7 | **random** recipes that still follow the diet — variety | 10 % |
> | FR-4.3 | recipes with **no rating** — discovery of something new | 10 % |
> | FR-0.11 | recipes that **break** the diet — only when the user asks | 0 % |
>
> **A dietary requirement can be medical.** An allergy, coeliac disease or
> diabetes is not a taste. FR-0.11 must **never** apply to a requirement that
> the user marked as medical, so a dietary requirement needs that mark
> (FR-0.6). Without the mark, the app treats every requirement as medical and
> never breaks it.

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
| 3.1 | **Every** suggestion follows the dietary requirements strictly | A vegan user never sees a recipe with meat, in no screen and in no notification | **Open** | The query sends `apiKey`, `ingredients` and `number`, and no `diet` and no `intolerances`. The commit of 1 March sent no diet either, so this is a gap and not a regression. |
| 3.2 | A suggestion uses the products of the inventory | The ingredients of a suggestion come from the inventory of that user | **Done** | `RecipeRestService.getRecipesByExpiringProducts` |
| 3.3 | Products that expire soon come first | A product with three days left steers the suggestion | **Done** | `EXPIRATION_WARNING_DAYS = 3`, `getProductWithShortExpDate` |
| 3.4 | A suggestion covers as many products of the household as possible | Of two recipes, the one that uses more of the stock stands higher | **Partly** | `findByIngredients` can rank by that, but the `ranking` parameter is not sent |
| 3.5 | At least three recipes, and as many as the user set | The list never holds fewer than three | **Partly** — three is the fixed number | `RECIPES_PER_SEARCH = 3`; no setting for more |
| 3.6 | Choosing a recipe exports the missing items to the shopping list | Of eight ingredients, the three that the inventory lacks appear on the list | **Open** | No code compares the ingredients of a recipe with the inventory. The README promises this. |
| 3.8 | With an empty inventory, or with nothing that needs attention, the suggestion follows the dietary requirements and the score of the recommender **alone** | A user with an empty pantry still sees at least three recipes that fit the diet | **Open** | The suggestion of the dashboard starts from the expiring products. The random path `getNRandomRecipes` knows neither the diet nor a score. |
| 3.7 | The suggestions **and** the products that expire soon reach the user as a push notification at the set hour | One notification a day. It names the number of products that expire soon and at least one recipe, and a tap opens the screen that belongs to it. | **Partly** — see the three faults under this table | `PushWorker`, `RecipeWorker`, `HiltApplication` |

> **The notification does not work today, for three reasons.**
>
> 1. `PushWorker` runs as periodic work **every 15 minutes**, and not at an
>    hour of the day. It reads no setting of the user, so the switch
>    `push_notifications` changes nothing.
> 2. `POST_NOTIFICATIONS` is **not** in `AndroidManifest.xml`. The build
>    targets API 34, so Android 13 and later drop the notification without a
>    word. The notification of the expiring products reaches no new telephone.
> 3. `RecipeWorker` is a one-time work for 07:03 of the local time, written
>    into `HiltApplication`. With `ExistingWorkPolicy.KEEP` it runs once and
>    never again, and it shows the user nothing: it only fetches data.
>
> The hour of the user needs a column in `et_profile_settings`, which holds
> three boolean columns today and no time.

### FR-4 — Bookmark and like

| # | The requirement | Acceptance criterion | State | Where |
|---|---|---|---|---|
| 4.1 | Bookmark a recipe and see the bookmarks together | A bookmark survives a restart and shows in one list | **Partly** | `FavoritesFragment`, `RecipeRepositoryImpl` |
| 4.2 | Like or rate a recipe | A like or a mark changes the rank of that recipe for that user | **Open** | `/api/recipe/rate` exists. The client never calls it. |
| 4.3 | A higher rating makes a recipe appear more often, and **a share of the suggestions carries no rating at all**, so that a new recipe can be found | Over twenty suggestions, about two hold no rating. The share is the default of FR-D. | **Open** | — |
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
| N-14 | Reference data ships with the app and needs no network: the shelf life of each type (FR-1.4.2) and the meal types (FR-6.2) live in a versioned file in the repository, read once from the internet and then reviewed by a person | A build without a network holds every value, and the file names its version and its source | **Open** |
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

---

## 8. Where things are

| What | Where |
|---|---|
| The knowledge base of the code | [`CLAUDE.md`](../CLAUDE.md) |
| The open technical tasks | [`CLAUDE.md`](../CLAUDE.md) §7 |
| The design of record | `frontend/doc/design/ui_design_ideas_002.png` |
| The design system | `frontend/android-feedme-app/app/src/main/res/values/` |
| The endpoints | [`README.md`](../README.md) §REST API |
