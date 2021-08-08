package service.database

import com.squareup.sqldelight.db.SqlDriver
import db.Database
import db.Test

interface DatabaseService {

    fun initialize()

    fun isBotEnabled(): Boolean

    fun setBotEnabled(enabled: Boolean)

    fun getTestEntries(): List<Test>

    fun insertTest(test: Test)

    fun deleteTests()
}

class DatabaseServiceImpl(
    private val databaseDriver: SqlDriver,
    private val database: Database
) : DatabaseService {

    override fun initialize() {
        if (initialized) {
            println("database was already initialized")
            return
        }

        synchronized(LOCK) {
            if (initialized) {
                println("database was already initialized")
                return
            }

            val oldVersion = runCatching { database.databaseVersionQueries.selectDbVersion().executeAsOneOrNull() }.getOrNull() ?: 0

            Database.Schema.migrate(
                databaseDriver,
                oldVersion,
                Database.Schema.version
            )

            initialized = true
        }
    }

    override fun isBotEnabled() = runCatching { database.botConfigQueries.selectBotConfig().executeAsOneOrNull()?.enabled }.getOrNull() ?: true

    override fun setBotEnabled(enabled: Boolean) {
        database.botConfigQueries.updateBotEnabled(enabled)
    }

    override fun getTestEntries() = database.testQueries.selectAll().executeAsList()

    override fun insertTest(test: Test) {
        database.testQueries.insert(test.name, test.number)
    }

    override fun deleteTests() {
        database.testQueries.delete()
    }

    companion object {

        private val LOCK = Object()

        @Volatile
        private var initialized = false
    }
}
