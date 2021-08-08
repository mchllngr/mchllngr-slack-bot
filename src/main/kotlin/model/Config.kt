package model

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
}
