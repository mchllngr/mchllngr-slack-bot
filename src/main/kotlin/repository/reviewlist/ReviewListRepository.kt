package repository.reviewlist

import datastore.DataStore
import model.user.UserId
import repository.absence.AbsenceRepository

interface ReviewListRepository {

    var absenceApiKey: String?

    var users: List<UserId>

    companion object {

        fun create(
            dataStore: DataStore,
            absenceRepo: AbsenceRepository
        ): ReviewListRepository = ReviewListRepositoryImpl(dataStore, absenceRepo)
    }
}

class ReviewListRepositoryImpl(
    dataStore: DataStore,
    private val absenceRepo: AbsenceRepository
) : ReviewListRepository {

    private val queries = dataStore.reviewListQueries
    private val userQueries = dataStore.userQueries

    override var absenceApiKey: String?
        get() = queries.selectAbsenceApiKey().executeAsOneOrNull()?.absenceApiKey
        set(key) = queries.updateAbsenceApiKey(key)

    override var users: List<UserId>
        get() = queries.selectUsers().executeAsList() // TODO #29 use AbsenceRepository.getUserAvailability to filter out unavailable users
        set(list) = queries.transaction {
            queries.deleteUsers()
            list.forEach { user ->
                // make sure the user exists already to satisfy foreign constraint
                userQueries.insert(user)

                queries.insertUser(user)
            }
        }
}
