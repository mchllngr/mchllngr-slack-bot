package util.slackbot

import com.slack.api.bolt.App
import com.slack.api.bolt.AppConfig
import com.slack.api.bolt.socket_mode.SocketModeApp
import script.ScriptHandler
import servicelocator.ServiceLocator

object SlackBot {

    private val scriptHandler = ServiceLocator.scriptHandler

    fun start(initializer: ScriptHandler.() -> Unit) {
        val app = App(
            AppConfig.builder()
                .singleTeamBotToken(ServiceLocator.config.token.bot)
                .build()
        )

        scriptHandler.initializer()
        scriptHandler.registerScripts(app)

        SocketModeApp(ServiceLocator.config.token.app, app).start()
    }
}
