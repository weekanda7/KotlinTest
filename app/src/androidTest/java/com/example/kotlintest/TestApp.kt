package com.example.kotlintest

/**
 * Application the instrumented tests run against. [InstrumentedTestRunner] instantiates
 * this instead of the manifest's [KotlinTestApp]; the only difference is the container,
 * where anything that would otherwise touch a thread, a network or a clock is a fake.
 */
class TestApp : KotlinTestApp() {

    override val appContainer: AppContainer by lazy { TestAppContainer() }
}

class TestAppContainer : AppContainer {

    override val deviceRepository: DeviceRepository = FakeDeviceRepository()
}
