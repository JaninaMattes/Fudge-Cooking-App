# =============================================================================
#  Fudge - build, test and quality
#
#  Use "make help" to see all commands.
#
#  The commands work on two parts:
#    backend   Spring Boot REST API (Maven)
#    frontend  Android client (Gradle)
#
#  The build of the frontend needs the Android SDK. Set ANDROID_HOME or write
#  the path into frontend/android-feedme-app/local.properties.
# =============================================================================

BACKEND_DIR  := backend/food-tracker-api
FRONTEND_DIR := frontend/android-feedme-app

# Place of the Android SDK. Gradle needs it for each command of the frontend.
# The variable takes the value of the environment first, then the usual place
# on macOS and on Linux.
ANDROID_HOME ?= $(firstword $(wildcard $(HOME)/Library/Android/sdk $(HOME)/Android/Sdk))
export ANDROID_HOME

# The repository holds a wrapper for each build tool, so the version of the
# tool is the same on each machine and in the pipeline.
MVN          := cd $(BACKEND_DIR) && sh ./mvnw
GRADLE       := cd $(FRONTEND_DIR) && ./gradlew

# The pipeline needs no colours and no questions.
MVN_FLAGS    := -B
GRADLE_FLAGS := --console=plain --stacktrace

# The least free space that a build needs. See disk-check.
DISK_MIN_MB  ?= 2048

.DEFAULT_GOAL := help
.PHONY: help all build test lint coverage clean verify \
        backend-build backend-test backend-lint backend-coverage backend-clean backend-run \
        frontend-build frontend-test frontend-lint frontend-coverage frontend-clean \
        frontend-sdk-check frontend-format frontend-verify backend-verify backend-bugs backend-docs \
        frontend-ui-test docs db-up db-down api-package api-up api-down api-logs api-smoke \
        emulator-create emulator-start demo-video demo-check disk-check

# -----------------------------------------------------------------------------
#  Help
# -----------------------------------------------------------------------------

help: ## Shows this list of commands
	@echo "Fudge - build, test and quality"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) \
		| awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-22s\033[0m %s\n", $$1, $$2}'
	@echo ""

# -----------------------------------------------------------------------------
#  Commands for both parts
# -----------------------------------------------------------------------------

all: lint build test ## Runs the style check, the build and the tests of both parts

verify: backend-verify frontend-verify ## Runs every check that the pipeline runs

# A build writes into the project and into the caches of Maven and of Gradle
# in the home directory. Without free space the compiler cannot write every
# class file, and the tests then stop with NoClassDefFoundError, which looks
# like an error of the code. This guard says the truth instead.
disk-check: ## Stops when a volume holds less free space than DISK_MIN_MB
	@for dir in . "$$HOME"; do \
		free=$$(df -m "$$dir" | awk 'NR == 2 { print $$4 }'); \
		if [ "$$free" -lt $(DISK_MIN_MB) ]; then \
			echo "Stop: the volume of $$dir holds $$free MB free."; \
			echo "      A build needs $(DISK_MIN_MB) MB."; \
			echo "      Free space and run the command again."; \
			exit 1; \
		fi; \
	done

build: backend-build frontend-build ## Builds both parts

test: backend-test frontend-test ## Runs the tests of both parts

lint: backend-lint frontend-lint ## Runs the style check of both parts

coverage: backend-coverage frontend-coverage ## Writes the coverage report of both parts

clean: backend-clean frontend-clean ## Removes the files of the last build

# -----------------------------------------------------------------------------
#  Backend (Spring Boot, Maven)
# -----------------------------------------------------------------------------

backend-build: ## Builds the JAR file of the REST API
	$(MVN) $(MVN_FLAGS) package -DskipTests

backend-test: ## Runs the unit tests of the REST API
	$(MVN) $(MVN_FLAGS) test

backend-lint: ## Runs Checkstyle on the code of the REST API
	$(MVN) $(MVN_FLAGS) checkstyle:check

backend-bugs: ## Runs SpotBugs on the code of the REST API
	$(MVN) $(MVN_FLAGS) compile spotbugs:check

backend-coverage: ## Writes the coverage report to target/site/jacoco/index.html
	$(MVN) $(MVN_FLAGS) clean verify
	@echo "Report: $(BACKEND_DIR)/target/site/jacoco/index.html"

backend-verify: disk-check ## Runs the style check, the tests, the coverage gate and SpotBugs
	$(MVN) $(MVN_FLAGS) clean verify

backend-run: ## Starts the REST API on the local machine
	$(MVN) $(MVN_FLAGS) spring-boot:run

backend-docs: ## Writes the Javadoc of the REST API
	$(MVN) $(MVN_FLAGS) javadoc:javadoc
	@echo "Documentation: $(BACKEND_DIR)/target/site/apidocs/index.html"

backend-clean: ## Removes the files of the last build of the REST API
	$(MVN) $(MVN_FLAGS) clean

# -----------------------------------------------------------------------------
#  Frontend (Android, Gradle)
# -----------------------------------------------------------------------------

frontend-sdk-check: ## Tells if the Android SDK is available
	@if [ -z "$(ANDROID_HOME)" ] || [ ! -d "$(ANDROID_HOME)" ]; then \
		echo "The Android SDK is missing."; \
		echo "Install Android Studio, or set ANDROID_HOME to the SDK:"; \
		echo "  export ANDROID_HOME=\$$HOME/Library/Android/sdk"; \
		exit 1; \
	fi
	@echo "Android SDK: $(ANDROID_HOME)"

frontend-build: frontend-sdk-check ## Builds the debug APK of the Android client
	$(GRADLE) $(GRADLE_FLAGS) assembleDebug

frontend-test: frontend-sdk-check ## Runs the unit tests of the Android client
	$(GRADLE) $(GRADLE_FLAGS) testDebugUnitTest

frontend-lint: frontend-sdk-check ## Runs ktlint, detekt and the Android Lint
	$(GRADLE) $(GRADLE_FLAGS) ktlintCheck detekt lintDebug

frontend-format: frontend-sdk-check ## Corrects the style of the Kotlin code
	$(GRADLE) $(GRADLE_FLAGS) ktlintFormat

frontend-coverage: frontend-sdk-check ## Writes the coverage report of the Android client
	$(GRADLE) $(GRADLE_FLAGS) jacocoTestReport
	@echo "Report: $(FRONTEND_DIR)/app/build/reports/jacoco/jacocoTestReport/html/index.html"

frontend-verify: frontend-sdk-check ## Runs the style check, the tests and the build
	$(GRADLE) $(GRADLE_FLAGS) ktlintCheck detekt lintDebug testDebugUnitTest assembleDebug

frontend-ui-test: frontend-sdk-check ## Runs the user interface tests. An emulator must run.
	$(GRADLE) $(GRADLE_FLAGS) connectedDebugAndroidTest

frontend-clean: ## Removes the files of the last build of the Android client
	$(GRADLE) $(GRADLE_FLAGS) clean

# -----------------------------------------------------------------------------
#  Database and demo
# -----------------------------------------------------------------------------

db-up: ## Starts the PostgreSQL database in Docker
	cd $(BACKEND_DIR) && docker compose up -d postgresdb

db-down: ## Stops the PostgreSQL database
	cd $(BACKEND_DIR) && docker compose down

# -----------------------------------------------------------------------------
#  Demo on an emulator
#
#  Three commands give a demo, in this order:
#    1. make api-up          the REST API and the database in Docker
#    2. make emulator-start  the emulator fudge-34
#    3. make demo-video      the APK, the steps of Maestro and the video
#
#  "make demo-check" tells which part is missing.
# -----------------------------------------------------------------------------

AVD_NAME  ?= fudge-34
SDK_IMAGE ?= system-images;android-34;google_apis;arm64-v8a
SDK_TOOLS := $(ANDROID_HOME)/cmdline-tools/latest/bin
EMULATOR  := $(ANDROID_HOME)/emulator/emulator
ADB       := $(ANDROID_HOME)/platform-tools/adb
MAESTRO   ?= $(firstword $(wildcard $(HOME)/.maestro/bin/maestro) maestro)

api-package: ## Builds the JAR file that the Docker image needs
	$(MVN) $(MVN_FLAGS) clean package -DskipTests

api-up: api-package ## Starts the REST API and PostgreSQL in Docker
	cd $(BACKEND_DIR) && docker compose up -d --build
	@echo "The API answers on http://localhost:80, and on http://10.0.2.2:80 for an emulator."

api-down: ## Stops the REST API and PostgreSQL. CLEAN=1 also drops the data
	cd $(BACKEND_DIR) && docker compose down $(if $(CLEAN),-v,)

api-smoke: ## Asks the running API the three questions of the demo
	@sh tools/demo/api-smoke.sh

api-logs: ## Shows the log of the REST API
	cd $(BACKEND_DIR) && docker compose logs -f app

emulator-create: ## Makes the emulator $(AVD_NAME). The download needs about 1.5 GB
	yes | $(SDK_TOOLS)/sdkmanager --install "emulator" "$(SDK_IMAGE)"
	echo no | $(SDK_TOOLS)/avdmanager create avd -n $(AVD_NAME) -k "$(SDK_IMAGE)" -d pixel_5

emulator-start: ## Starts the emulator $(AVD_NAME) and waits for its start
	@tools/demo/start-emulator.sh $(AVD_NAME)

demo-check: ## Tells which part of the demo is missing
	@echo "Android SDK:  $(ANDROID_HOME)"
	@test -x "$(EMULATOR)" && echo "emulator:     yes" \
		|| echo "emulator:     MISSING - make emulator-create"
	@$(EMULATOR) -list-avds 2>/dev/null | grep -qx "$(AVD_NAME)" \
		&& echo "AVD:          $(AVD_NAME)" \
		|| echo "AVD:          MISSING - make emulator-create"
	@test -x "$(MAESTRO)" && echo "maestro:      $$($(MAESTRO) --version 2>/dev/null)" \
		|| echo "maestro:      MISSING - curl -fsSL https://get.maestro.mobile.dev | bash"
	@docker info > /dev/null 2>&1 && echo "docker:       runs" \
		|| echo "docker:       MISSING - start Docker Desktop"
	@$(ADB) devices | grep -qw device && echo "device:       runs" \
		|| echo "device:       none - make emulator-start"

demo-video: ## Records a video of the application on an emulator
	PATH="$(dir $(MAESTRO)):$(ANDROID_HOME)/platform-tools:$$PATH" \
		./tools/demo/record-demo.sh
