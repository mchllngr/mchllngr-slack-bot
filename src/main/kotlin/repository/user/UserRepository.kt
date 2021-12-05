package repository.user

import datastore.DataStore

interface UserRepository {

    companion object {

        fun create(dataStore: DataStore): UserRepository = UserRepositoryImpl(dataStore)
    }
}

class UserRepositoryImpl(dataStore: DataStore) : UserRepository {

    private val queries = dataStore.userQueries
}
