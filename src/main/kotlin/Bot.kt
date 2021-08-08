import com.slack.api.bolt.App
import com.slack.api.bolt.AppConfig
import com.slack.api.bolt.socket_mode.SocketModeApp
import script.registerTestScript
import servicelocator.ServiceLocator.config
import util.debug.DebugModeHandler

fun main() {
    DebugModeHandler.handle(config.debugMode)

    val app = App(
        AppConfig.builder()
            .singleTeamBotToken(config.botToken)
            .build()
    )

    app.registerTestScript()

    SocketModeApp(config.appToken, app).start()
}
