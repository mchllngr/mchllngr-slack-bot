package factory

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.asJdbcDriver
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import servicelocator.ServiceLocator.config

object SqlDriverFactory {

    fun create(): SqlDriver {
        val datasourceConfig = HikariConfig().apply {
            jdbcUrl = "jdbc:mariadb://${config.database.url}"
            username = config.database.user
            password = config.database.password
        }
        val dataSource = HikariDataSource(datasourceConfig)
        return dataSource.asJdbcDriver()
    }
}
