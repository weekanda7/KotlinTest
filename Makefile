.PHONY: help build clean test androidTest gmd gmdCi install lint

help:
	@echo "Available targets:"
	@echo "  make build       - assemble debug APK"
	@echo "  make clean       - remove build outputs"
	@echo "  make test        - run JVM unit tests"
	@echo "  make androidTest - run instrumented tests on a connected device/emulator"
	@echo "  make gmd         - run instrumented tests on the Gradle-managed Pixel 8 / API 37 device"
	@echo "  make gmdCi       - run instrumented tests on the 'ci' device group (ATD API 33 + API 37)"
	@echo "  make install     - install debug APK on a connected device/emulator"
	@echo "  make lint        - run Android lint"

build:
	./gradlew assembleDebug

clean:
	./gradlew clean

test:
	./gradlew testDebugUnitTest

androidTest:
	./gradlew connectedDebugAndroidTest

gmd:
	./gradlew pixel8api37DebugAndroidTest

gmdCi:
	./gradlew --continue ciGroupDebugAndroidTest

install:
	./gradlew installDebug

lint:
	./gradlew lintDebug
