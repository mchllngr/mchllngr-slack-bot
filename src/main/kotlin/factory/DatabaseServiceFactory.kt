package factory

import com.squareup.sqldelight.db.SqlDriver
import db.Database
import service.database.DatabaseService
import service.database.DatabaseServiceImpl

object DatabaseServiceFactory {

    fun create(
        driver: SqlDriver,
        database: Database
    ): DatabaseService = DatabaseServiceImpl(driver, database)
}
