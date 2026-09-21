package com.example.kotlintest.marks

/*
 * Test "marks" - the pytest `@pytest.mark.xxx` equivalent for this suite.
 *
 * Put one or more on a test class or a single @Test method, then select them with
 * AndroidJUnitRunner's annotation filter (wired up in the Makefile):
 *
 *   make gmd MARK=Smoke                 # only @Smoke tests
 *   make gmd MARK=Critical,Smoke        # tests carrying BOTH @Critical and @Smoke
 *   make gmd NOT_MARK=Low               # everything except @Low tests
 *
 * A mark on the class applies to every test in it. Kotlin annotations are retained at
 * runtime by default, which is what the runner needs to see them.
 */

// ---- Suite type ----

/** Fast, broad check that the app's main flows still work. Run on every change. */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class Smoke

/** Full regression coverage. Slower; run before a release or on a schedule. */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class Regression

// ---- Priority (use one per test) ----

/** Blocks the core user journey (e.g. cannot log in). Must never fail. */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class Critical

/** Major feature broken, but a workaround exists. */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class High

/** Partial or edge-case feature issue. */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class Medium

/** Cosmetic or rare-path issue. */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class Low
