package service.user

import datastore.DataStore

interface UserService {

    companion object {

        fun create(dataStore: DataStore): UserService = UserServiceImpl(dataStore)
    }
}

class UserServiceImpl(dataStore: DataStore) : UserService {

    private val queries = dataStore.userQueries
}
