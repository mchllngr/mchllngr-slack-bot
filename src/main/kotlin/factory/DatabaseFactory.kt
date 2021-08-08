package factory

import com.squareup.sqldelight.sqlite.driver.asJdbcDriver
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import db.Database

object DatabaseFactory {

    fun create(): Database {
        val datasourceConfig = HikariConfig().apply {
            jdbcUrl = "jdbc:mariadb://localhost:3306/slack-bot"
            username = "root"
            password = "mypass"
        }
        val dataSource = HikariDataSource(datasourceConfig)
        val driver = dataSource.asJdbcDriver()
        return Database(driver)
    }
}
