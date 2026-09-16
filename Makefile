.PHONY: help build clean test androidTest install lint

help:
	@echo "Available targets:"
	@echo "  make build       - assemble debug APK"
	@echo "  make clean       - remove build outputs"
	@echo "  make test        - run JVM unit tests"
	@echo "  make androidTest - run instrumented tests on a connected device/emulator"
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

install:
	./gradlew installDebug

lint:
	./gradlew lintDebug
