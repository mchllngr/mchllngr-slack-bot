package repository.team

import datastore.DataStore
import db.SelectUsersForTeam
import db.Team
import model.team.TeamId
import model.user.UserId

interface TeamRepository {

    fun getTeamsForUser(userId: UserId): List<Team>

    fun getUsersForTeam(teamId: TeamId): List<SelectUsersForTeam>

    companion object {

        fun create(dataStore: DataStore): TeamRepository = TeamRepositoryImpl(dataStore)
    }
}

class TeamRepositoryImpl(dataStore: DataStore) : TeamRepository {

    private val queries = dataStore.teamQueries

    override fun getTeamsForUser(userId: UserId) = queries.selectTeamsForUser(userId).executeAsList()

    override fun getUsersForTeam(teamId: TeamId) = queries.selectUsersForTeam(teamId).executeAsList()
}
