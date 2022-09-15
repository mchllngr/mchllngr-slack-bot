package script.home.block

import com.slack.api.model.block.Blocks.section
import com.slack.api.model.block.LayoutBlock
import com.slack.api.model.block.composition.BlockCompositions.markdownText
import db.Team
import model.user.UserId
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
        val teamNames = teamsForUser.map { it.name }
        return if (teamNames.size == 1) "Du gehörst dem Team *${teamNames.first()}* an."
        else "Du gehörst den Teams *${teamNames.joinToString(separator = ", ", lastSeparator = " und ")}* an."
    }

    fun getTeamBlocks(teamsForUser: List<Team>): List<LayoutBlock> = buildList {
        teamsForUser.forEach { team ->
            this += markdownSection("Teammitglieder *${team.name}*:")

            val usersInTeam = teamRepo.getUsersForTeam(team.id)
                .map { user -> markdownText("<@${user.id.id}> ${if (user.admin) " *(Teamadmin)*" else ""}") }
            this += section { it.fields(usersInTeam) }
        }
    }
}
