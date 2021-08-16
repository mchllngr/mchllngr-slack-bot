package servicelocator

import factory.ConfigFactory
import factory.DatabaseFactory
import factory.DatabaseServiceFactory
import factory.ScriptHandlerFactory
import factory.SqlDriverFactory

object ServiceLocator {

    val config by lazy { ConfigFactory.create() }

    private val databaseDriver by lazy { SqlDriverFactory.create() }

    private val database by lazy { DatabaseFactory.create(databaseDriver) }

    val databaseService by lazy { DatabaseServiceFactory.create(databaseDriver, database) }

    val scriptHandler by lazy { ScriptHandlerFactory.create() }
}
