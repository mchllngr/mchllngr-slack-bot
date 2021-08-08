package servicelocator

import factory.ConfigFactory
import factory.DatabaseFactory

object ServiceLocator {

    val config by lazy { ConfigFactory.create() }

    val database by lazy { DatabaseFactory.create() }
}
