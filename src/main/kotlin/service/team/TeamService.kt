package service.team

import datastore.DataStore

interface TeamService {

    companion object {

        fun create(dataStore: DataStore): TeamService = TeamServiceImpl(dataStore)
    }
}

class TeamServiceImpl(dataStore: DataStore) : TeamService {

    private val queries = dataStore.teamQueries
}
