package factory

import com.squareup.sqldelight.db.SqlDriver
import db.Database

object DatabaseFactory {

    fun create(driver: SqlDriver): Database = Database(driver)
}
