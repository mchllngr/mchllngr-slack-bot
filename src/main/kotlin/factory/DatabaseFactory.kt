package factory

import com.squareup.sqldelight.sqlite.driver.asJdbcDriver
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import db.Database
import servicelocator.ServiceLocator.config

object DatabaseFactory {

    fun create(): Database {
        val datasourceConfig = HikariConfig().apply {
            jdbcUrl = "jdbc:mariadb://${config.database.url}"
            username = config.database.user
            password = config.database.password
        }
        val dataSource = HikariDataSource(datasourceConfig)
        val driver = dataSource.asJdbcDriver()
        return Database(driver)
    }
}
