package factory

import exception.MandatoryEnvironmentVariableMissingException
import model.Config

object ConfigFactory {

    fun create() = Config(
        System.getenv("DEBUG_MODE").equals("true", true),
        Config.Token(
            System.getenv("SLACK_BOT_TOKEN") ?: envVarMissing("SLACK_BOT_TOKEN"),
            System.getenv("SLACK_APP_TOKEN") ?: envVarMissing("SLACK_APP_TOKEN")
        ),
        Config.Database(
            System.getenv("DATABASE_URL") ?: envVarMissing("DATABASE_URL"),
            System.getenv("DATABASE_USER") ?: envVarMissing("DATABASE_USER"),
            System.getenv("DATABASE_PASSWORD") ?: envVarMissing("DATABASE_PASSWORD")
        )
    )

    private fun envVarMissing(name: String): Nothing = throw MandatoryEnvironmentVariableMissingException(name)
}
