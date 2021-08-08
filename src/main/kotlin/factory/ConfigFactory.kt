package factory

import model.Config

object ConfigFactory {

    fun create() = Config(
        System.getenv("DEBUG_MODE").equals("true", true),
        System.getenv("SLACK_BOT_TOKEN"),
        System.getenv("SLACK_APP_TOKEN")
    )
}
