# Changes

The newest change stands on top. Each entry names the requirement of
[`PRODUCT.md`](PRODUCT.md) that it serves, and each change carries its own
tests, with the edge cases and not only the happy path (N-3).

---

## 14 November 2026

### The build of the client speaks to Gradle 9 (no requirement, a trap)

Every build of the client ended with *"Deprecated Gradle features were used in
this build, making it incompatible with Gradle 9.0."* The call came from
**kapt**, inside the Kotlin plugin 1.9.24, so no setting of this project could
silence it.

- `kapt` is gone, **KSP** took its place. Hilt and `androidx.hilt` both work
  with KSP.
- The **Glide processor is gone**, not moved: no file of the project used the
  generated Glide API.
- `app/build.gradle`, `build.gradle`, `gradle.properties` and
  `gradle/libs.versions.toml` hold the change.

**Proof:** `make frontend-verify` — ktlint, detekt and Android Lint clean, 64
unit tests, both APK files written, and the line about Gradle 9 is gone.

The wrapper now holds **Gradle 9.9.1**, and the build succeeds on it: the call
of kapt that this change removed is a call that Gradle 9 no longer holds. Two
notices remain, and neither comes from a file of this project:

| The notice | Where it comes from | When it bites |
|---|---|---|
| `Configuration.setVisible(boolean)` | the ktlint plugin 12.1.1, `createKtlintConfiguration` | Gradle 11 |
| `ReportingExtension.file(String)` | the same plugin | Gradle 10 |

A newer ktlint plugin removes both. **Note also** that AGP 8.5.2 is not an
officially supported partner of Gradle 9; it works here, and task B7 of
`CLAUDE.md` is the place to raise AGP and Kotlin to a tested pair.

### A guard before a build (a trap)

`make backend-verify` failed with 14 `NoClassDefFoundError` of its **own**
classes, which reads like an error of the code. The cause was a full disk.

- New `make disk-check`, with `DISK_MIN_MB` at 2048. It checks the volume of
  the project **and** the volume of the home directory, because Maven and
  Gradle write their caches there.
- `backend-verify` runs it first.

### The demo data counts from today (FR-3.3, the condition of every demo)

Every one of the 38 products of `db/data.sql` held a date of February 2022, so
the demo showed a pantry four years past its date, and the colours of the
dashboard looked broken although the code was right.

- Each date is now `CURRENT_DATE ± INTERVAL '… days'`.
- The pantry of `john.smith` holds two products that are bad, four inside the
  warning window of three days and the rest fresh.
- Onions and eggs moved into the pantry, so a recipe needs only the rest.
- The demo user no longer holds **Vegan and Meat** at the same time.
- `Boeuf Bourguignon` no longer carries the label `Vegetarisch`.
- Two vegetarian recipes are new, so a vegetarian user still receives the
  three recipes of FR-3.5.

**Tests:** `DemoDataTest`, 6 tests — no date may be written into the file, the
pantry shows every state at once, three recipes are vegetarian, and no user
holds two requirements that contradict each other.

### A smoke test of the API (the condition of the demo, work item 2)

New `make api-smoke` and `tools/demo/api-smoke.sh`: it asks the running API the
three questions that every screen of the demo depends on — can the demo user
log in, does the pantry hold products that expire soon, do at least three
recipes come back. Each answer is one line, and the script ends with a value
that a pipeline can read.

### FR-3.6 — the missing items of a recipe reach the shopping list

The README promised it and no code did it: choosing a recipe left the shopping
list untouched.

| New file | What it holds |
|---|---|
| `common/utils/IngredientMatcher.java` | The one place that compares the name of an ingredient with the name of a product. It ignores case and spaces, reads one "s" at the end as a plural, leaves out a product whose date has passed, and gives each missing ingredient once. |
| `services/RecipeShoppingService.java` and its implementation | Reads the recipe and the pantry, asks the matcher, and writes each missing ingredient onto the current list. |
| `resources/RecipeShoppingResource.java` | `POST /api/shoppinglist/current/add/missing` with the body `{"userId": 1, "recipeId": 7}`, and the answer says how many products reached the list. |

No class of the old code changed, so no old test moved.

**Tests:** 20 new tests. `IngredientMatcherTest` holds the kitchen cases — a
plural, a capital letter, spaces, a product that is already bad, a product
without a date, the same ingredient twice, an empty pantry, a pantry that is
null, a row without a name. `RecipeShoppingServiceImplTest` holds the cases of
the service — a recipe that does not exist, a user without a pantry, a product
that the list refuses, an ingredient without an amount, a recipe without
ingredients.

**Still open for this requirement:** the client does not call the new endpoint
yet. Until it does, the step is visible in the API only.

### The tags of a recipe, and a weight for every wish (FR-0.6, FR-0.7, FR-4.5)

A recipe had **no** tag table. It carried one free text column,
`recipe_label`, and that column lied: `Boeuf Bourguignon` was labelled
`Vegetarisch`. A user requirement was a plain link, with no way to say "I aim
to eat vegan, and I like fish now and then".

**The smallest change that carries the model.** Nothing is renamed, nothing is
removed, and every SQL statement of the Java code keeps working.

| Change in `db/schema.sql` | What it carries |
|---|---|
| `et_dietry_tag.kind` | One vocabulary, and `kind` says what a tag is: `diet`, `medical`, `nutrition`, `cuisine`, `meal`, `attribute`. |
| `et_user_tag.weight_percent`, `et_user_tag.is_medical` | What a user wants and how much. A **medical** requirement is a veto: it carries no weight and is never traded against an expiring product. A taste is a weight. |
| `et_recipe_tag` (new) | The three or four tags of a recipe, for the recipe screen. |

**In `db/data.sql`:** the vocabulary now reads like the real world — the
duplicate `Mediterranean` became `Pescatarian`, `Fast Food` became `Quick
meal`, the vague `Fitness` and `Balanced` became `High protein` and `High
fibre`, `No Pig` became `No pork`, and twelve tags are new (the free-from
ones, `Climate-friendly`, `Seasonal`, `Under 30 minutes`, the meals and two
cuisines). Each of the six recipes carries **three** tags, at most one of them
a diet and always the most exact one. John Smith now holds *Vegan 70,
Pescatarian 20, High protein 10*; Annabelle holds *Lactose-free* as a medical
veto with no weight, beside *Vegetarian 100*.

**In the code:** `Tag` carries `kind`, `weightPercent` and `medical`, the row
mapper of the user tags reads them, and `RecipeSuggestionServiceImpl` names a
medical requirement as a veto in its log.

**What did not change: the behaviour.** Every requirement that the rules know
still acts as a veto, so no user suddenly sees a recipe that they did not see
yesterday. The weighted choice — two vegan recipes and one that uses the steak
which expires in two days — is the next step, and `docs/PRODUCT.md` §7 holds
it with five other open points.

**Tests:** 611 now (606 before). `DemoDataTest` grew five guards: every recipe
carries three or four tags, a recipe carries at most one diet, every tag of a
recipe stands in the vocabulary, the weights of one user add up to 100, and a
medical requirement carries no weight. The sixth is the important one: **a
recipe that carries a diet really follows it**, checked with the same
`DietRules` that the application uses, so a mislabelled recipe cannot return.

> **One trap found on the way.** `DietRules` turns the label of a tag into the
> key of a rule, so "No pork" becomes `no-pork`. Renaming the tag without
> renaming the key makes the requirement **silently become a taste** that
> hides nothing. `DietRulesTest` caught it. Trap 20 of `CLAUDE.md`.

### The application speaks English (FR-0.12, new requirement)

German stood in the data because the first version was shown in Germany. The
product is English, with further languages later, and the variant follows the
country of the login.

- The six recipes, their labels, their descriptions, their steps, the
  difficulty and the names of the images are English.
- `ShoppingListServiceImpl` wrote `"Produktbild"` as the name of a new image;
  it writes `"Product image"` now, and three tests follow it.
- New requirements in `PRODUCT.md`: **FR-0.12** the language, **FR-0.13** day
  and night mode, **FR-0.14** the settings that a modern application holds,
  with their defaults in FR-D.
- New rule 9 of `CLAUDE.md`: every text of the application is English, and a
  text belongs in the file of its language, never in the code.

**Tests:** `DemoDataTest.theDemoDataSpeaksEnglish` fails when a German letter
or one of the old German words returns.

### FR-3.1 — a suggestion follows the dietary requirements

A vegan user was served wild boar: nothing compared a recipe with the
requirements of a user, and the label of a recipe could not do it either —
`Boeuf Bourguignon` carried the label `Vegetarisch`.

The decision judges the **ingredients**, not the label.

| New file | What it holds |
|---|---|
| `src/main/resources/diet-rules.properties` | Which ingredients each requirement forbids, and the exceptions that keep "coconut milk" vegan and "eggplant" an aubergine. It names its version and the date of its review, and it ships with the application (N-14). |
| `common/diet/DietRules.java` | Reads the file and answers two questions: is this requirement a hard rule, and which ingredients of this recipe break it. It reuses `IngredientMatcher.normalise`, so the form of a food name stays in one place. |
| `common/diet/DietRulesConfiguration.java` | Reads the file once at the start. A file that is missing stops the start, because a suggestion that cannot read the rules would promise a diet it does not keep. |
| `services/RecipeSuggestionService` and its implementation | Reads the requirements of the user and lets the rules decide. It writes a log entry for every recipe that it leaves out. |

`RecipeRestResource` now passes all three of its lists through the service:
the random list of the recipe screen, the list that matches the products which
expire soon, and the list of the best marks.

**A decision that belongs to the product owner:** a requirement that the file
does **not** name is a taste, not a rule. "Mediterranean", "Low Carb",
"Fitness" and "Balanced" therefore hide no recipe; they will rank one recipe
above another once FR-R1 exists. Without that line a low-carb user would lose
every recipe with flour, and the demo user would see one recipe instead of
three.

**Tests:** 23 new tests. `DietRulesTest` holds the hard cases — a forbidden
word inside another word ("minced meat" is meat), an exception that wins
("coconut milk", "eggplant", "peanut butter"), a label with a space ("No
Pig"), several requirements at once, a vegetarian who may eat an egg while a
vegan may not, a requirement that forbids nothing, a file that is not on the
class path, and every list that can be null.
`RecipeSuggestionServiceImplTest` holds the service — a user that nobody can
read, a user without requirements, a requirement without a label, a row
without a recipe, and a list that is null.

### The sum of the day

| Measure | Before | After |
|---|---|---|
| Tests of the backend | 557 | **611** |
| Coverage of the backend | 100 % | 100 % (the gate holds) |
| Checkstyle, SpotBugs | clean | clean |
| Deprecation lines of the client build | 1 | **0** |
| Tests of the client | 64 | 64 |
