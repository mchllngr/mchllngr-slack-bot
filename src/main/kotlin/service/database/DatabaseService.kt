package service.database

import com.squareup.sqldelight.db.SqlDriver
import db.Database
import db.Test

interface DatabaseService {

    fun initialize()

    fun getTestEntries(): List<Test>

    fun insertTest(test: Test)
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

    override fun getTestEntries() = database.testQueries.selectAll().executeAsList()

    override fun insertTest(test: Test) {
        database.testQueries.insert(test.name, test.number)
    }

    companion object {

        private val LOCK = Object()

        @Volatile
        private var initialized = false
    }
}
