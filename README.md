<div id="top"></div>

<div align="center">
  <img src="assets/images/logo.png" alt="Fudge" width="100%">

  <h1>Fudge &mdash; Pantry &amp; Recipes</h1>

  <p>
    <strong>Track the food that you have. Avoid foodwaste. Cook it before it is bad.</strong>
  </p>

  <p>
    It is an Android app called Fudge, featuring a Spring Boot REST API. Fudge takes stock of your kitchen after you’ve been shopping, scans products and their details using their barcodes (where available), or uses image recognition (a future feature) to automatically recognise items such as fruit and vegetables. Fudge knows the use-by dates of various products and tracks them throughout your everyday life. Based on your dietary requirements and the frequency you suggested, Fudge suggests recipes that use as many of the products as possible that are already in your home or are about to go off. Missing items can be automatically exported to a shopping list when a recipe is selected. This makes food waste in the household more visible, ensures products are used up early, saves time spent searching for suitable recipes, and helps you save money on your next grocery shopping.

  </p>

  <p>
    <a href="#architecture">Architecture</a> ·
    <a href="#quick-start">Quick start</a> ·
    <a href="#development">Development</a> ·
    <a href="#tests-and-quality">Tests</a> ·
    <a href="#design-system">Design</a>
  </p>
</div>

<p align="center">
  <img alt="Backend" src="https://img.shields.io/badge/backend-Spring%20Boot%204.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">
  <img alt="Frontend" src="https://img.shields.io/badge/frontend-Android%20%7C%20Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white">
  <img alt="Tests" src="https://img.shields.io/badge/tests-557%20backend%20%2B%2064%20android-success?style=for-the-badge">
  <img alt="Coverage" src="https://img.shields.io/badge/backend%20coverage-100%25-success?style=for-the-badge">
  <img alt="License" src="https://img.shields.io/badge/license-MIT-blue?style=for-the-badge">
</p>

---

## Table of contents

1. [What the application does](#what-the-application-does)
2. [Design prototype](#design-prototype)
3. [Architecture](#architecture)
4. [Quick start](#quick-start)
5. [Development](#development)
6. [Tests and quality](#tests-and-quality)
7. [Design system](#design-system)
8. [REST API](#rest-api)
9. [Configuration](#configuration)
10. [Log and trace of an error](#log-and-trace-of-an-error)
11. [Demo video](#demo-video)
12. [Repository](#repository)
13. [The names in this repository](#the-names-in-this-repository)
14. [Contributing](#contributing)
15. [License](#license)

---

## What the application does

Each year a household throws away food that is still good, because nobody
knows what is in the refrigerator. Fudge solves this problem in three steps:

| Step | What the user does | What the application does |
|---|---|---|
| **1. Collect** | Scans the barcode of a product | Reads the name and the nutrition value from the Edamam food database |
| **2. Watch** | Opens the inventory list | Shows each product with the colour of its state: green is fresh, yellow is this week, red is bad |
| **3. Cook** | Opens the recipe suggestions | Asks the Spoonacular API for recipes that use the products that expire soon |

Other functions: two shopping lists (one for the next purchase, one as a
template of the last purchase), a profile with food preferences such as
"Vegan", and a notification before a product expires.

<p align="right">(<a href="#top">back to top</a>)</p>

---

## Design prototype

<div align="center">
  <img src="assets/images/figma.png" alt="The screens of the Figma prototype" width="100%">
</div>

<div align="center">

[**Open the interactive Figma prototype**](https://www.figma.com/proto/J7pup60VT1Uw12lk1U4hGL/FeedMe-App?node-id=1-4&starting-point-node-id=1%3A4)

</div>

> The prototype shows the user flow and the structure of the screens. The
> colours of the application come from this design. See
> [Design system](#design-system).

<p align="right">(<a href="#top">back to top</a>)</p>

---

## Architecture

The two parts of the project have the same idea: the layer of the user
interface knows nothing about the database, and the layer of the data knows
nothing about the screens. A change in one layer therefore stops at the border
of that layer.

```
┌──────────────────────────────── Android client ───────────────────────────────┐
│                                                                               │
│   Fragment  ──reads──▶  ViewModel  ──calls──▶  Repository  ──calls──▶ Retrofit│
│   (view)        LiveData  (state)                (data)                       │
│      ▲                                              │                         │
│      └──────────────── Hilt gives each part ────────┘                         │
└───────────────────────────────────────┬───────────────────────────────────────┘
                                        │ HTTPS, JSON
┌───────────────────────────────────────▼───────────────────────────────────────┐
│                            Spring Boot REST API                               │
│                                                                               │
│   Resource  ──calls──▶  Service  ──calls──▶  Repository  ──SQL──▶ PostgreSQL  │
│   (REST)                (rules)              (JdbcTemplate)                   │
│        │                                                                      │
│        └──▶ RequestMapper (JSON to model)      JsonHttpClient ──▶ Edamam,     │
│                                                                   Spoonacular │
└───────────────────────────────────────────────────────────────────────────────┘
```

<div align="center">
  <img src="assets/images/architecture.png" alt="Architecture of the application" width="100%">
</div>

### The parts of the backend

| Package | Task |
|---|---|
| `resources` | REST endpoints. They read the body of the request and give an answer. |
| `services` | Rules of the application, for example "count the expiring products only when the user gave permission". |
| `repositories` | SQL statements. Each repository works on one part of the database. |
| `models` | The objects of the application. Lombok writes the access methods. |
| `common/mapper` | `RequestMapper` turns the JSON body into a model. All three controllers use it. |
| `common/rest` | `JsonHttpClient` sends the requests to the external APIs. A test gives a stub answer. |
| `common/utils` | `Attempt` turns an error into a log entry and a default value. `DateUtils`, `ImageUtils`, `WordUtils`. |
| `common/config` | `ApiProperties` reads the credentials from the environment. |

### Built with

**Android client**
Kotlin 1.9 · Android Gradle Plugin 8.5 · Gradle 8.9 · Java 17 ·
MVVM with ViewModel and LiveData · Jetpack Navigation 2.7 · Hilt 2.51 ·
Retrofit 2.11 and OkHttp 4.12 · Coroutines 1.8 · Glide and Picasso ·
ZXing for the barcode · WorkManager 2.9 · ViewBinding · Material Components 1.12

All versions are in one place:
[`gradle/libs.versions.toml`](frontend/android-feedme-app/gradle/libs.versions.toml).

**REST API**
Java 17 · Spring Boot 4.1 · Spring JDBC · PostgreSQL 13 · jjwt 0.12 ·
BCrypt · Lombok · Maven 3.9 (wrapper) · Docker

**Quality**
JUnit 5 · Mockito · AssertJ · JaCoCo · Checkstyle · SpotBugs (backend) ·
JUnit 4 · Truth · Robolectric · Espresso · ktlint · detekt · Android Lint
(frontend)

<p align="right">(<a href="#top">back to top</a>)</p>

---

## Quick start

### What you need

| Program | Version | For |
|---|---|---|
| Java | 17 | both parts |
| Docker | 20 or later | PostgreSQL database |
| Android Studio | Koala or later | Android client |
| Android SDK | API 34 (the application runs from API 27) | Android client |
| GNU Make | 3.81 or later | the commands below |

Gradle needs the place of the Android SDK. Android Studio writes it into
`frontend/android-feedme-app/local.properties`. On the command line, set the
variable instead:

```bash
export ANDROID_HOME=$HOME/Library/Android/sdk   # macOS
export ANDROID_HOME=$HOME/Android/Sdk           # Linux
make frontend-sdk-check                         # tells if the path is correct
```

### Three commands

```bash
git clone https://github.com/JaninaMattes/Fudge-Cooking-App.git
cd Fudge-Cooking-App

make api-up            # 1. PostgreSQL and the REST API in Docker, on port 80
make emulator-start    # 2. starts the emulator fudge-34
make demo-video        # 3. APK, the steps of the demo, the video and a GIF
```

`make demo-check` says which part is missing, and `make help` shows all
commands. `make api-down` stops the two containers; `CLEAN=1 make api-down`
also drops the data of the database.

The database reads `db/schema.sql` and `db/data.sql` at its first start, so the
demo has a user:

| Email | Password |
|---|---|
| `john.smith@testmail.com` | `testpassword` |

A **debug** build of the client talks to `http://10.0.2.2:80`. That address is
the host of the emulator, so the client reaches the API of `make api-up`. A
**release** build talks to the server of the university. Both values stand in
the block `buildTypes` of
[`app/build.gradle`](frontend/android-feedme-app/app/build.gradle).

<p align="right">(<a href="#top">back to top</a>)</p>

---

## Development

All work goes through the `Makefile`, so a command is the same on your machine
and in the pipeline.

| Command | What it does |
|---|---|
| `make help` | Shows all commands |
| `make build` | Builds both parts |
| `make test` | Runs the tests of both parts |
| `make lint` | Runs the style check of both parts |
| `make coverage` | Writes the coverage reports |
| `make verify` | Runs every check that the pipeline runs |
| `make clean` | Removes the files of the last build |

Each command also works for one part only: `make backend-test`,
`make frontend-lint`, and so on.

### The pipeline

Two workflows of GitHub Actions run on each push and on each pull request:

| Workflow | Steps |
|---|---|
| [`backend.yml`](.github/workflows/backend.yml) | Checkstyle → tests → coverage gate → SpotBugs → JAR file |
| [`frontend.yml`](.github/workflows/frontend.yml) | ktlint → detekt → Android Lint → unit tests → coverage → APK; the main branch also runs the tests of the user interface on an emulator and keeps the video |

The pipeline stops the build when the coverage falls under 95 % of the
instructions or under 90 % of the branches.

<p align="right">(<a href="#top">back to top</a>)</p>

---

## Tests and quality

### Backend

```
557 tests · 100 % of the instructions · 100 % of the branches · 0 findings of SpotBugs
```

```bash
make backend-verify     # style, tests, coverage gate and static analysis
make backend-coverage   # writes target/site/jacoco/index.html
```

The tests need **no database and no network**:

- A **test double** (`RecordingJdbcTemplate`) answers the SQL statements of
  the repositories and records what each repository sent. The tests therefore
  prove the order of the values of each statement.
- A **stub** of `JsonHttpClient` answers the external APIs with a JSON text of
  the test.
- A **small HTTP server** of the Java library tests the real HTTP client.
- **MockMvc** sends a request to each REST endpoint and reads the answer.

### Frontend

```
64 unit tests · ktlint clean · detekt clean · Android Lint clean · APK builds
```

```bash
make frontend-test      # unit tests on the development machine
make frontend-lint      # ktlint, detekt and the Android Lint
make frontend-format    # corrects the style of the Kotlin code
make frontend-ui-test   # tests on an emulator or on a telephone
```

The unit tests of the client need no emulator, because the functions of
`ui/common/utils` now use `java.text` and not `android.icu`.

Two of the checks work with a list of accepted findings, because the first
version of the application had many small style problems:

| File | Holds |
|---|---|
| `config/detekt/baseline-app.xml` | 37 structural findings of detekt |
| `app/lint-baseline.xml` | 452 findings of the Android Lint, mostly resources that nobody uses |

The list is a contract: a **new** file must stay free of findings. To remove a
line, correct the code and write the list again with
`./gradlew :app:detektBaseline`.

<p align="right">(<a href="#top">back to top</a>)</p>

---

## Design system

The design follows
[`frontend/doc/design/ui_design_ideas_002.png`](frontend/doc/design/ui_design_ideas_002.png):
a **white screen**, large pictures with round corners, near-black text and one
**emerald green** for each action. The picture of the food is the first thing
that the user sees, so the interface around it stays quiet and uses colour
only where colour carries a meaning.

| Token | Colour | Where |
|---|---|---|
| `brand_primary` | ![#16A34A](https://place-hold.it/16x16/16A34A/16A34A) `#16A34A` | Round button, active tab, link "See all", selected category |
| `surface_background` | ![#FFFFFF](https://place-hold.it/16x16/FFFFFF/FFFFFF) `#FFFFFF` | Screen and cards |
| `surface_input` | ![#F4F5F6](https://place-hold.it/16x16/F4F5F6/F4F5F6) `#F4F5F6` | Search field, category that is not selected |
| `text_primary` | ![#15181B](https://place-hold.it/16x16/15181B/15181B) `#15181B` | Title and name |
| `text_secondary` | ![#8E9399](https://place-hold.it/16x16/8E9399/8E9399) `#8E9399` | Helper text, for example "By David Charles" |
| `rating_star` | ![#FFB800](https://place-hold.it/16x16/FFB800/FFB800) `#FFB800` | Stars of a rating |

> The first design in `assets/images/figma.png` used a sage green header. The
> application no longer follows it. `ui_design_ideas_002.png` is the design of
> record.

### The colour of a state

The user reads the state of the food from the colour, and does not have to
compare the dates:

| State | Colour | When |
|---|---|---|
| **Fresh** | ![#16A34A](https://place-hold.it/16x16/16A34A/16A34A) green | More than 7 days |
| **This week** | ![#E8A020](https://place-hold.it/16x16/E8A020/E8A020) yellow | 4 to 7 days |
| **Use soon** | ![#F06A28](https://place-hold.it/16x16/F06A28/F06A28) orange | 1 to 3 days, or today |
| **Expired** | ![#E02B2B](https://place-hold.it/16x16/E02B2B/E02B2B) red | The date is in the past |

The rule is in
[`ExpiryStatus`](frontend/android-feedme-app/app/src/main/java/com/mobilesystems/feedme/ui/common/utils/ExpiryStatus.kt),
and a unit test proves each border of the rule.

### The files of the design system

| File | Holds |
|---|---|
| `res/values/colors.xml` | The palette and the semantic tokens |
| `res/values-night/colors.xml` | The same tokens for the dark mode |
| `res/values/dimens.xml` | Distances with a step of 4dp, the type scale, the corner radii |
| `res/values/styles.xml` | The styles of the text, of the buttons, of the cards and of the chips |
| `res/values/themes.xml` | The themes of the application |

A layout uses a **token** (`@color/text_primary`) and not a raw colour
(`@color/black`). A change of the brand therefore needs one change in
`colors.xml` and no change in the 51 layout files. The names of the first
version stay in the file and point to the tokens, so that the old layouts keep
working.

<p align="right">(<a href="#top">back to top</a>)</p>

---

## REST API

Base address: `http://<host>:80/api`

| Method | Path | What it does |
|---|---|---|
| `POST` | `/users/login` | Logs a user in and gives a JWT token |
| `POST` | `/users/register` | Makes a user with the empty lists |
| `POST` | `/users/email` | Tells if an email address is free |
| `GET` | `/users/{userId}` | Reads the profile of a user |
| `PUT` | `/users/update/...` | Changes the profile, the image or a permission |
| `GET` | `/products/all/{userId}` | Reads the inventory list |
| `POST` | `/products/add` | Puts a product on the inventory list |
| `PUT` | `/products/update` | Changes a product |
| `DELETE` | `/products/delete` | Takes a product off the list |
| `GET` | `/products/count/expiring/{userId}` | Counts the products that expire soon |
| `GET` | `/shoppinglist/current/{userId}` | Reads the current shopping list |
| `GET` | `/shoppinglist/old/{userId}` | Reads the old shopping list |
| `POST` | `/shoppinglist/current/create` | Makes a product on the current list |
| `GET` | `/recipe/all/{userId}` | Reads the recipes of a user |
| `GET` | `/recipe/rest/poll/{userId}` | Loads new recipes from Spoonacular |
| `GET` | `/barcode/rest/{barcode}` | Reads a product from its barcode |
| `GET` | `/dashboard/shortexp/{userId}` | Reads the products of the start screen |
| `GET` | `/db/init` | Corrects the sequences of the database |

`make backend-docs` writes the full Javadoc to
`backend/food-tracker-api/target/site/apidocs/index.html`.

<p align="right">(<a href="#top">back to top</a>)</p>

---

## Configuration

The application reads each value from
an environment variable and uses a default value only for the local machine. 

```bash
export FT_DB_URL=jdbc:postgresql://localhost:5432/foodtrackerdb
export FT_DB_USER=foodtracker
export FT_DB_PASSWORD=<your password>

export FT_API_SECRET_KEY=<a long text for the JWT tokens>
export FT_SPOONACULAR_KEY=<your key>
export FT_EDAMAM_APP_ID=<your application ID>
export FT_EDAMAM_BARCODE_KEY=<your key>
```

See
[`application.properties`](backend/food-tracker-api/src/main/resources/application.properties)
and
[`ApiProperties`](backend/food-tracker-api/src/main/java/com/foodtracker/foodtrackerapi/common/config/ApiProperties.java).

The Android client reads the address of the API from
`common/config/Constants.kt`.

<p align="right">(<a href="#top">back to top</a>)</p>

---

## Log and trace of an error

Both parts write their errors to one place, and one ID connects the two logs.

**The ID of the request.** The client gives each request the header
`X-Request-Id` with eight characters. The API writes that ID into every line of
its own log and sends the ID back in the answer. A user who reports a problem
needs the ID only; the ID leads to each line of that request.

**The log of the API.**

| Value | What it does | Default |
|---|---|---|
| `FT_LOG_LEVEL_APP` | Level of the classes of the project | `DEBUG` |
| `FT_LOG_LEVEL_SPRING` | Level of Spring | `INFO` |
| `FT_LOG_FILE` | File of the log | `<temporary folder>/food-tracker-api.log` |

```
2026-11-13 16:58:36.004 INFO  [3f9a1c7e] c.f.f.c.logging.RequestLogFilter - GET /api/users/1 answered 200 after 24 ms
2026-11-13 16:58:37.114 ERROR [a1b2c3d4] c.f.f.common.utils.Attempt       - Failed to read the inventory list: relation "et_product" does not exist
```

`RequestLogFilter` writes the first line for each request, and `Attempt` writes
a line for each operation of the data layer that failed. `ApiExceptionHandler`
answers an error of an endpoint with the status, a short text and the ID of the
request; the detail of the error stays in the log, because a message can hold
the name of a table.

**The log of the client.** Every line stands under the name `Fudge`:

```bash
adb logcat -s Fudge
```

`NetworkLogInterceptor` writes one line for each request in **each** build, so
a user of a release build can send a useful log. The body of a request reaches
Logcat in a **debug** build only, and the headers `Authorization` and `Cookie`
never reach it.

<p align="right">(<a href="#top">back to top</a>)</p>

---

## Demo video

The project records a walk through the application on an **emulator**. No
telephone is necessary.

```bash
make demo-check              # says which part is missing
make api-up                  # the API with data, so the screens are not empty
make emulator-start          # the emulator fudge-34
make demo-video              # with Maestro: the video shows the name of each step
MODE=adb make demo-video     # with the Android tools only
```

**What the demo needs once:**

```bash
make emulator-create                                   # emulator and image, about 1.5 GB
curl -fsSL "https://get.maestro.mobile.dev" | bash     # Maestro
```

The steps of the demo stand in [`tools/demo/flows/`](tools/demo/flows/). Each
file is a flow of Maestro, and `demo.yaml` runs all of them in one video. The
flow `01-login.yaml` takes the user of `db/data.sql`; give another user with
`maestro test -e EMAIL=a@b.c -e PASSWORD=secret`.

The pipeline also records a video on each build of the main branch and keeps
it as an artifact of the run.

[**docs/DEMO_VIDEO.md**](docs/DEMO_VIDEO.md) compares the free tools of the
market (Maestro, adb screenrecord, scrcpy, Espresso, Appium, Fastlane) and
says which one fits which need.

<p align="right">(<a href="#top">back to top</a>)</p>

---

## Repository

```
Fudge-Cooking-App/
├── Makefile                      Commands for the build, the tests and the quality
├── .github/workflows/            Pipelines of GitHub Actions
├── assets/                       Logo, screens of the design, architecture
├── docs/
│   └── DEMO_VIDEO.md             The tools for a demo video
├── tools/demo/                   Script and flows for the demo video
├── backend/food-tracker-api/
│   ├── config/checkstyle/        Style rules
│   ├── config/spotbugs/          Findings that the project accepts
│   ├── db/                       Schema and test data of PostgreSQL
│   ├── docker-compose.yml        API and database in Docker
│   └── src/
│       ├── main/java/            The code of the REST API
│       └── test/java/            557 tests
└── frontend/android-feedme-app/
    ├── config/detekt/            Rules of the static analysis
    └── app/src/
        ├── main/java/            The code of the Android client
        ├── main/res/             Layouts and the design system
        ├── test/                 Tests for the development machine
        └── androidTest/          Tests for an emulator
```

<p align="right">(<a href="#top">back to top</a>)</p>

---

## The names in this repository

The product has one name: **Fudge &mdash; Pantry & Recipes**. The code still
holds two older names, because a change of a package name or of an artifact ID
touches every file and every import.

| Name | Where it stands | What it means |
|---|---|---|
| `Fudge` | README, the label of the application, the name in Logcat | The product. Use this name in every new text. |
| `FeedMe`, `feedme` | `frontend/android-feedme-app`, the package `com.mobilesystems.feedme` | The first name of the Android client. |
| `food-tracker`, `foodtracker` | `backend/food-tracker-api`, the package `com.foodtracker.foodtrackerapi` | The first name of the REST API. |

The one name for the GitHub description:

```
Fudge - Pantry & Recipes: an Android app with a Spring Boot API that tracks
your food, reads a barcode and suggests recipes before the food is bad.
```

The list of the open work in [`CLAUDE.md`](CLAUDE.md) holds the full change of
the names as one task.

<p align="right">(<a href="#top">back to top</a>)</p>

---

## Contributing

1. Make a branch: `git checkout -b feature/my-feature`
2. Write the code **and the tests**
3. Run `make verify`. The pipeline runs the same checks.
4. Make a commit and a pull request

<p align="right">(<a href="#top">back to top</a>)</p>

---

## License

MIT. See [LICENSE](LICENSE).

Fudge is the base of the larger project
[Cookly](https://github.com/JaninaMattes/Smart-Pantry-Planner/).

<p align="right">(<a href="#top">back to top</a>)</p>
