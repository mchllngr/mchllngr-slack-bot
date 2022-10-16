package servicelocator

import datastore.DataStore
import factory.DatabaseFactory
import factory.SqlDriverFactory
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import model.config.Config
import network.ApiClient
import repository.absence.AbsenceRepository
import repository.admin.AdminRepository
import repository.reviewlist.ReviewListRepository
import repository.script.ScriptRepository
import repository.team.TeamRepository
import repository.user.UserRepository
import script.base.ScriptHandler
import kotlin.random.Random

object ServiceLocator {

    val random = Random(System.currentTimeMillis())

    val config by lazy { Config.create() }

    val scriptHandler by lazy { ScriptHandler.create(adminRepo) }

    private val databaseDriver by lazy { SqlDriverFactory.create() }

    private val database by lazy { DatabaseFactory.create(databaseDriver) }

    val dataStore by lazy { DataStore.create(databaseDriver, database) }

    private val httpClient by lazy { HttpClient(CIO) }

    val apiClient by lazy { ApiClient.create(httpClient) }

    val adminRepo by lazy { AdminRepository.create(dataStore, scriptRepo) }

    private val scriptRepo by lazy { ScriptRepository.create(dataStore) }

    val userRepo by lazy { UserRepository.create(dataStore) }

    val teamRepo by lazy { TeamRepository.create(dataStore) }

    val reviewListRepo by lazy { ReviewListRepository.create(dataStore, absenceRepo) }

    val absenceRepo by lazy { AbsenceRepository.create(apiClient) }
}
