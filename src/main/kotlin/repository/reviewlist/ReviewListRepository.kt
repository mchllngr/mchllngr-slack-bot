package repository.reviewlist

import datastore.DataStore
import model.user.UserId

interface ReviewListRepository {

    var absenceApiKeyId: String?

    var absenceApiKey: String?

    var users: List<UserId>

    companion object {

        fun create(dataStore: DataStore): ReviewListRepository = ReviewListRepositoryImpl(dataStore)
    }
}

class ReviewListRepositoryImpl(dataStore: DataStore) : ReviewListRepository {

    private val queries = dataStore.reviewListQueries
    private val userQueries = dataStore.userQueries

    override var absenceApiKeyId: String?
        get() = queries.selectAbsenceApiKeyId().executeAsOneOrNull()?.absenceApiKeyId
        set(keyId) = queries.updateAbsenceApiKeyId(keyId)

    override var absenceApiKey: String?
        get() = queries.selectAbsenceApiKey().executeAsOneOrNull()?.absenceApiKey
        set(key) = queries.updateAbsenceApiKey(key)

    override var users: List<UserId>
        get() = queries.selectUsers().executeAsList()
        set(list) = queries.transaction {
            queries.deleteUsers()
            list.forEach { user ->
                // make sure the user exists already to satisfy foreign constraint
                userQueries.insert(user)

                queries.insertUser(user)
            }
        }
}
