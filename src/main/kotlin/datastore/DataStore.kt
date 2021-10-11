package datastore

import com.squareup.sqldelight.db.SqlDriver
import db.Database

class DataStore(
    private val databaseDriver: SqlDriver,
    private val database: Database
) : Database by database {

    fun initialize() {
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

    companion object {

        private val LOCK = Object()

        @Volatile
        private var initialized = false

        fun create(
            driver: SqlDriver,
            database: Database
        ): DataStore = DataStore(driver, database)
    }
}
