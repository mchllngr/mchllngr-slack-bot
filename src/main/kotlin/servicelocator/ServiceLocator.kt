package servicelocator

import datastore.DataStore
import factory.DatabaseFactory
import factory.SqlDriverFactory
import model.Config
import script.base.ScriptHandler
import service.bot.BotConfigService
import service.team.TeamService
import service.user.UserService

object ServiceLocator {

    val config by lazy { Config.create() }

    val scriptHandler by lazy { ScriptHandler.create() }

    private val databaseDriver by lazy { SqlDriverFactory.create() }

    private val database by lazy { DatabaseFactory.create(databaseDriver) }

    val dataStore by lazy { DataStore.create(databaseDriver, database) }

    val botConfigService by lazy { BotConfigService.create(dataStore) }

    val userService by lazy { UserService.create(dataStore) }

    val teamService by lazy { TeamService.create(dataStore) }
}
