package servicelocator

import datastore.DataStore
import factory.DatabaseFactory
import factory.SqlDriverFactory
import model.config.Config
import repository.admin.AdminRepository
import repository.reviewlist.ReviewListRepository
import repository.script.ScriptRepository
import repository.team.TeamRepository
import repository.user.UserRepository
import script.base.ScriptHandler

object ServiceLocator {

    val config by lazy { Config.create() }

    val scriptHandler by lazy { ScriptHandler.create(adminRepo) }

    private val databaseDriver by lazy { SqlDriverFactory.create() }

    private val database by lazy { DatabaseFactory.create(databaseDriver) }

    val dataStore by lazy { DataStore.create(databaseDriver, database) }

    val adminRepo by lazy { AdminRepository.create(dataStore, scriptRepo) }

    private val scriptRepo by lazy { ScriptRepository.create(dataStore) }

    val userRepo by lazy { UserRepository.create(dataStore) }

    val teamRepo by lazy { TeamRepository.create(dataStore) }

    val reviewListRepo by lazy { ReviewListRepository.create(dataStore) }
}
