import com.example.kotlintest.screens.LoginScreen
import com.kaspersky.kaspresso.testcases.api.scenario.Scenario
import com.kaspersky.kaspresso.testcases.core.testcontext.TestContext

class LoginScenario(private val email: String, private val password: String) : Scenario() {
    override val steps: TestContext<Unit>.() -> Unit = {
        step("登入 $email") {
            LoginScreen {
                enterEmail { typeText(email) }
                enterPassword { typeText(password) }
                tapLogin { click() }
            }
        }
    }
}
