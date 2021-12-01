package servicelocator

import datastore.DataStore
import factory.DatabaseFactory
import factory.SqlDriverFactory
import model.config.Config
import script.base.ScriptHandler
import service.admin.AdminService
import service.script.ScriptService
import service.team.TeamService
import service.user.UserService

object ServiceLocator {

    val config by lazy { Config.create() }

    val scriptHandler by lazy { ScriptHandler.create(adminService) }

    private val databaseDriver by lazy { SqlDriverFactory.create() }

    private val database by lazy { DatabaseFactory.create(databaseDriver) }

    val dataStore by lazy { DataStore.create(databaseDriver, database) }

    val adminService by lazy { AdminService.create(dataStore, scriptService) }

    private val scriptService by lazy { ScriptService.create(dataStore) }

    val userService by lazy { UserService.create(dataStore) }

    val teamService by lazy { TeamService.create(dataStore) }
}
