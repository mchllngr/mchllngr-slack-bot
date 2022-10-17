package servicelocator

import datastore.DataStore
import factory.DatabaseFactory
import factory.SqlDriverFactory
import model.config.Config
import network.absence.AbsenceApiClient
import network.client.HttpClientFactory
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

    val scriptHandler by lazy { ScriptHandler.create(Admin.repo) }

    object Database {

        private val databaseDriver by lazy { SqlDriverFactory.create() }

        private val database by lazy { DatabaseFactory.create(databaseDriver) }

        val dataStore by lazy { DataStore.create(databaseDriver, database) }
    }

    private object Http {

        val client by lazy { HttpClientFactory.create() }
    }

    object Absence {

        private val apiClient by lazy { AbsenceApiClient.create(Http.client) }

        val repo by lazy { AbsenceRepository.create(apiClient) }
    }

    object Admin {

        private val scriptRepo by lazy { ScriptRepository.create(Database.dataStore) }

        val repo by lazy { AdminRepository.create(Database.dataStore, scriptRepo) }
    }

    object User {

        val repo by lazy { UserRepository.create(Database.dataStore) }
    }

    object Team {

        val repo by lazy { TeamRepository.create(Database.dataStore) }
    }

    object ReviewList {

        val repo by lazy { ReviewListRepository.create(Database.dataStore) }
    }
}
