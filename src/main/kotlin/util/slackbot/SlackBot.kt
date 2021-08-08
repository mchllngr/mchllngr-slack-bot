package util.slackbot

import com.slack.api.bolt.App
import com.slack.api.bolt.AppConfig
import com.slack.api.bolt.socket_mode.SocketModeApp
import servicelocator.ServiceLocator

object SlackBot {

    fun start(initializer: App.() -> Unit) {
        val app = App(
            AppConfig.builder()
                .singleTeamBotToken(ServiceLocator.config.token.bot)
                .build()
        )

        app.initializer()

        SocketModeApp(ServiceLocator.config.token.app, app).start()
    }
}
