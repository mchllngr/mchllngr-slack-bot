package script.home.block

import com.slack.api.model.block.LayoutBlock
import db.Team
import model.user.UserId
import model.user.usernameString
import repository.team.TeamRepository
import util.charsequence.joinToString
import util.slack.block.headerSection
import util.slack.block.markdownSection
import util.slack.block.plainTextSection
import util.slack.user.SlackUser

class TeamBlocks(
    private val teamRepo: TeamRepository
) {

    fun createBlocks(
        slackUser: SlackUser
    ): List<LayoutBlock> = buildList {
        this += headerSection(text = ":busts_in_silhouette: Team", emoji = true)

        val teamsForUser = teamRepo.getTeamsForUser(UserId(slackUser.id))
        if (teamsForUser.isEmpty()) {
            this += plainTextSection("Du gehörst keinem Team an.")
            return@buildList
        }

        this += markdownSection(getTeamsTitle(teamsForUser))
        addAll(getTeamBlocks(teamsForUser))
    }

    private fun getTeamsTitle(teamsForUser: List<Team>): String {
        val teamNames = teamsForUser.map { "*${it.name}*" }
        return if (teamNames.size == 1) {
            "Du gehörst dem Team ${teamNames.first()} an."
        } else {
            "Du gehörst den Teams ${teamNames.joinToString(separator = ", ", lastSeparator = " und ")} an."
        }
    }

    private fun getTeamBlocks(teamsForUser: List<Team>): List<LayoutBlock> = buildList {
        teamsForUser.forEach { team ->
            this += markdownSection("Teammitglieder *${team.name}*:")

            val usersInTeam = teamRepo.getUsersForTeam(team.id)
                .joinToString(separator = "\n") { user -> "${user.id.usernameString} ${if (user.admin) " *(Teamadmin)*" else ""}" }
            this += markdownSection(usersInTeam)
        }
    }
}
