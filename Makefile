.PHONY: help build clean test androidTest gmd gmdCi install lint format lint-check

# ---------------------------------------------------------------------------
# Instrumented-test filters (pytest-style selection). All optional; leave them
# empty to run the whole suite. Passed to AndroidJUnitRunner as
# -Pandroid.testInstrumentationRunnerArguments.<key>=<value>, which works for
# androidTest, gmd and gmdCi alike. Filters combine as AND.
#
#   TEST      class / Class#method, comma-separated     (pytest file::Class::test)
#             short names get com.example.kotlintest.cases. prepended:
#               TEST=login.LoginActivityTest
#               TEST='login.LoginActivityTest#login_withInvalidEmail_showsEmailError'
#   EXCLUDE   class / Class#method to skip, same format as TEST
#   PKG       package, e.g. PKG=device -> com.example.kotlintest.cases.device
#   MARK      marks the test must carry (all of them), e.g. MARK=Smoke   (pytest -m)
#   NOT_MARK  marks to skip (any of them), e.g. NOT_MARK=Low       (pytest -m "not ...")
#   K         regex on "pkg.Class#method", e.g. K='.*withEmpty.*'   (pytest -k)
#
# Marks are the annotations in app/src/androidTest/.../marks/Marks.kt:
#   Smoke, Regression, Critical, High, Medium, Low
# ---------------------------------------------------------------------------
TEST     ?=
EXCLUDE  ?=
PKG      ?=
MARK     ?=
NOT_MARK ?=
K        ?=

# Gradle-managed device used by `make gmd` (see app/build.gradle.kts).
DEVICE   ?= pixel8api33atd

# Extra Gradle flags for the instrumented-test targets, e.g. the software GPU on CI:
#   GRADLE_ARGS=-Pandroid.testoptions.manageddevices.emulator.gpu=swiftshader_indirect
GRADLE_ARGS ?=

BASE_PKG  := com.example.kotlintest
CASES_PKG := $(BASE_PKG).cases
MARKS_PKG := $(BASE_PKG).marks

comma := ,
empty :=
space := $(empty) $(empty)

# $(call qualify,<list>,<prefix>): prefix every comma-separated item that does not
# already start with $(BASE_PKG), then rejoin with commas.
qualify = $(subst $(space),$(comma),$(strip $(foreach t,$(subst $(comma),$(space),$(1)),$(if $(filter $(BASE_PKG) $(BASE_PKG).%,$(t)),$(t),$(2).$(t)))))

RUNNER_ARG := -Pandroid.testInstrumentationRunnerArguments

FILTER_ARGS := \
	$(if $(TEST),'$(RUNNER_ARG).class=$(call qualify,$(TEST),$(CASES_PKG))') \
	$(if $(EXCLUDE),'$(RUNNER_ARG).notClass=$(call qualify,$(EXCLUDE),$(CASES_PKG))') \
	$(if $(PKG),'$(RUNNER_ARG).package=$(call qualify,$(PKG),$(CASES_PKG))') \
	$(if $(MARK),'$(RUNNER_ARG).annotation=$(call qualify,$(MARK),$(MARKS_PKG))') \
	$(if $(NOT_MARK),'$(RUNNER_ARG).notAnnotation=$(call qualify,$(NOT_MARK),$(MARKS_PKG))') \
	$(if $(K),'$(RUNNER_ARG).tests_regex=$(K)')
FILTER_ARGS := $(strip $(FILTER_ARGS))

help:
	@echo "Available targets:"
	@echo "  make build       - assemble debug APK"
	@echo "  make clean       - remove build outputs"
	@echo "  make test        - run JVM unit tests"
	@echo "  make androidTest - run instrumented tests on a connected device/emulator"
	@echo "  make gmd         - run instrumented tests on a Gradle-managed ATD (DEVICE=$(DEVICE))"
	@echo "  make gmdCi       - run instrumented tests on the 'ci' device group (ATD API 30 + API 33)"
	@echo "  make install     - install debug APK on a connected device/emulator"
	@echo "  make format      - auto-fix Kotlin formatting only (ktlint via Spotless, fast)"
	@echo "  make lint        - auto-fix: Android lint safe fixes + format"
	@echo "  make lint-check  - verify only, no file changes (for CI)"
	@echo ""
	@echo "Test filters for androidTest / gmd / gmdCi (all optional, combined as AND):"
	@echo "  TEST=login.LoginActivityTest            one class (short name under cases/)"
	@echo "  TEST='login.LoginActivityTest#method'   one test method"
	@echo "  TEST=login.LoginActivityTest,device.DeviceSearchTest   several"
	@echo "  EXCLUDE=device.DeviceSearchTest         skip classes/methods"
	@echo "  PKG=device                              one package under cases/"
	@echo "  MARK=Smoke  |  MARK=Critical,Smoke      tests carrying all listed marks"
	@echo "  NOT_MARK=Low                            skip tests with any listed mark"
	@echo "  K='.*withEmpty.*'                       regex on pkg.Class#method"
	@echo "  marks: Smoke Regression Critical High Medium Low"
	@echo ""
	@echo "  e.g. make gmd MARK=Critical   /   make androidTest TEST=login.LoginActivityTest"

build:
	./gradlew assembleDebug

clean:
	./gradlew clean

test:
	./gradlew testDebugUnitTest

androidTest:
	./gradlew connectedDebugAndroidTest $(GRADLE_ARGS) $(FILTER_ARGS)

gmd:
	./gradlew $(DEVICE)DebugAndroidTest $(GRADLE_ARGS) $(FILTER_ARGS)

gmdCi:
	./gradlew --continue ciGroupDebugAndroidTest $(GRADLE_ARGS) $(FILTER_ARGS)

install:
	./gradlew installDebug

# Kotlin formatting only - the fast one, like `ruff format`.
format:
	./gradlew spotlessApply

# Auto-fix, like `ruff check --fix && ruff format`.
# lintFix deliberately fails the build whenever it edits a file ("sources were modified
# after compilation"), so on failure re-run a plain lint: it passes if the only reason
# was applied fixes, and still fails on real errors. Format last so lint's edits get
# formatted too.
lint:
	./gradlew lintFixDebug || ./gradlew lintDebug
	./gradlew spotlessApply

# Verify only - fails on any formatting or Android lint issue, changes nothing.
lint-check:
	./gradlew spotlessCheck lintDebug
