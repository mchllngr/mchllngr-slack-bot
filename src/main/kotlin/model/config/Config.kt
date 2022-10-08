package model.config

import buildconfig.BuildConfig
import exception.MandatoryEnvironmentVariableMissingException
import model.user.UserId

data class Config(
    val buildInfo: BuildInfo,
    val debugMode: Boolean,
    val token: Token,
    val database: Database,
    val admin: Admin
) {

    data class BuildInfo(
        val version: String,
        val commitHash: String
    )

    data class Token(
        val bot: String,
        val app: String
    )

    data class Database(
        val url: String,
        val user: String,
        val password: String
    )

    data class Admin(
        val ids: List<UserId>
    )

    companion object {

        private const val ADMIN_IDS_SEPARATOR = ","

        fun create() = Config(
            BuildInfo(
                version = BuildConfig.VERSION,
                commitHash = BuildConfig.COMMIT_HASH.take(7)
            ),
            System.getenv("DEBUG_MODE").equals("true", true),
            Token(
                System.getenv("SLACK_BOT_TOKEN") ?: envVarMissing("SLACK_BOT_TOKEN"),
                System.getenv("SLACK_APP_TOKEN") ?: envVarMissing("SLACK_APP_TOKEN")
            ),
            Database(
                System.getenv("DATABASE_URL") ?: envVarMissing("DATABASE_URL"),
                System.getenv("DATABASE_USER") ?: envVarMissing("DATABASE_USER"),
                System.getenv("DATABASE_PASSWORD") ?: envVarMissing("DATABASE_PASSWORD")
            ),
            Admin(
                System.getenv("SLACK_BOT_ADMIN_IDS")
                    ?.split(ADMIN_IDS_SEPARATOR)
                    ?.filter { it.isNotBlank() }
                    ?.map { UserId(it) }
                    ?: emptyList()
            )
        )

        private fun envVarMissing(name: String): Nothing = throw MandatoryEnvironmentVariableMissingException(name)
    }
}
