import com.slack.api.bolt.App
import com.slack.api.bolt.AppConfig
import com.slack.api.bolt.socket_mode.SocketModeApp
import script.registerTestScript
import util.APP_TOKEN
import util.BOT_TOKEN

fun main() {
    // TODO debug flag -> explain how to set in README
    if (true) {
        System.setProperty("org.slf4j.simpleLogger.log.com.slack.api", "debug")
        System.setProperty("org.slf4j.simpleLogger.log.notion.api", "debug")
        System.setProperty("SLACK_APP_LOCAL_DEBUG", "debug")
    }

    val app = App(
        AppConfig.builder()
            .singleTeamBotToken(BOT_TOKEN)
            .build()
    )

    app.registerTestScript()

    SocketModeApp(APP_TOKEN, app).start()
}
