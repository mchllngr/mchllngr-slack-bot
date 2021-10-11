package model

import exception.MandatoryEnvironmentVariableMissingException

data class Config(
    val debugMode: Boolean,
    val token: Token,
    val database: Database,
) {

    data class Token(
        val bot: String,
        val app: String
    )

    data class Database(
        val url: String,
        val user: String,
        val password: String
    )

    companion object {

        fun create() = Config(
            System.getenv("DEBUG_MODE").equals("true", true),
            Token(
                System.getenv("SLACK_BOT_TOKEN") ?: envVarMissing("SLACK_BOT_TOKEN"),
                System.getenv("SLACK_APP_TOKEN") ?: envVarMissing("SLACK_APP_TOKEN")
            ),
            Database(
                System.getenv("DATABASE_URL") ?: envVarMissing("DATABASE_URL"),
                System.getenv("DATABASE_USER") ?: envVarMissing("DATABASE_USER"),
                System.getenv("DATABASE_PASSWORD") ?: envVarMissing("DATABASE_PASSWORD")
            )
        )

        private fun envVarMissing(name: String): Nothing = throw MandatoryEnvironmentVariableMissingException(name)
    }
}
