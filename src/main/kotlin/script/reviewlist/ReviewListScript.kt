package script.reviewlist

import com.slack.api.app_backend.slash_commands.response.SlashCommandResponse
import com.slack.api.bolt.context.builtin.SlashCommandContext
import com.slack.api.bolt.request.builtin.SlashCommandRequest
import com.slack.api.model.block.LayoutBlock
import model.command.CommandId
import model.script.ScriptId
import model.user.UserId
import repository.absence.Availability
import script.base.CommandScript
import script.base.config.Configurable
import script.base.config.block.ConfigBlockId
import script.base.config.block.ConfigBlockMultiUsersSelect
import script.base.config.block.ConfigBlockResponse
import script.base.config.block.ConfigBlockText
import servicelocator.ServiceLocator.Absence
import servicelocator.ServiceLocator.ReviewList
import servicelocator.ServiceLocator.random
import util.map.filterNotNullValues
import util.slack.block.markdownSection
import util.slack.context.getUser
import util.slack.user.SlackUser
import util.slack.user.usernameString
import util.time.getZoneDateTimeFromSlackUser
import java.time.ZonedDateTime

class ReviewListScript : CommandScript, Configurable {

    private val repo by lazy { ReviewList.repo }
    private val absenceRepo by lazy { Absence.repo }

    override val id = ID

    override val commandIds = listOf(COMMAND_ID_REVIEW_LIST)

    override val configBlockIds = listOf(
        CONFIG_ACTION_ID_ABSENCE_API_KEY_ID,
        CONFIG_ACTION_ID_ABSENCE_API_KEY,
        CONFIG_ACTION_ID_USER_LIST
    )

    override fun onCommandEvent(
        commandId: CommandId,
        request: SlashCommandRequest,
        ctx: SlashCommandContext
    ) {
        ctx.getUser(request)?.let { user ->
            when (commandId) {
                COMMAND_ID_REVIEW_LIST -> onCommandReviewList(user, ctx)
                else -> Unit
            }
        }
    }

    private fun onCommandReviewList(
        slackUser: SlackUser,
        ctx: SlashCommandContext
    ) {
        val now: ZonedDateTime = getZoneDateTimeFromSlackUser(slackUser)

        val users = repo.users
            .associateWith { ctx.getUser(it) }
            .filterNotNullValues()

        val userAvailability = getUserAvailability(users, now)

        val filteredUsers = users
            .filter { (userId, _) -> userAvailability[userId] != Availability.UNAVAILABLE }
            .values

        val reviewUsers = filteredUsers
            .shuffled(random)
            .mapIndexed { index, user -> "${index + 1}. ${user.usernameString}" }
            .joinToString(separator = "\n")

        ctx.respondInChannel(
            listOf(
                markdownSection(":speaking_head_in_silhouette: *Review-Reihenfolge:*\n$reviewUsers")
            )
        )
    }

    private fun getUserAvailability(
        users: Map<UserId, SlackUser>,
        now: ZonedDateTime
    ): Map<UserId, Availability> {
        val absenceApiKeyId = repo.absenceApiKeyId
        val absenceApiKey = repo.absenceApiKey
        if (absenceApiKeyId == null || absenceApiKey == null) {
            return users.mapValues { Availability.AVAILABLE_OFFICE }
        }

        val userEmails = users.mapValues { (_, slackUser) -> slackUser.profile?.email }
        return absenceRepo.getUserAvailability(
            absenceApiKeyId,
            absenceApiKey,
            now,
            userEmails
        )
    }

    private fun SlashCommandContext.respondInChannel(blocks: List<LayoutBlock>) {
        val response = SlashCommandResponse.builder()
            .responseType(RESPONSE_TYPE_IN_CHANNEL)
            .blocks(blocks)
            .build()
        respond(response)
    }

    override fun getConfigBlocks() = listOf(
        ConfigBlockText(
            scriptId = id,
            id = CONFIG_ACTION_ID_ABSENCE_API_KEY_ID,
            label = "absence.io API KeyId",
            optional = true,
            placeholder = "API KeyId",
            initialValue = repo.absenceApiKeyId
        ),
        ConfigBlockText(
            scriptId = id,
            id = CONFIG_ACTION_ID_ABSENCE_API_KEY,
            label = "absence.io API Key",
            optional = true,
            placeholder = "API Key",
            initialValue = repo.absenceApiKey
        ),
        ConfigBlockMultiUsersSelect(
            scriptId = id,
            id = CONFIG_ACTION_ID_USER_LIST,
            label = "ReviewList Users",
            optional = true,
            placeholder = "Users",
            initialUsers = repo.users
        )
    )

    override fun onConfigChange(
        user: SlackUser,
        response: ConfigBlockResponse<*>
    ) {
        when {
            response.configBlockId == CONFIG_ACTION_ID_ABSENCE_API_KEY_ID && response is ConfigBlockResponse.Text -> {
                repo.absenceApiKeyId = response.value
            }

            response.configBlockId == CONFIG_ACTION_ID_ABSENCE_API_KEY && response is ConfigBlockResponse.Text -> {
                repo.absenceApiKey = response.value
            }

            response.configBlockId == CONFIG_ACTION_ID_USER_LIST && response is ConfigBlockResponse.MultiUsersSelect -> {
                repo.users = response.value
            }
        }
    }

    companion object {

        val ID = ScriptId("REVIEW_LIST")

        private val COMMAND_ID_REVIEW_LIST = CommandId.User.Str("/reviewlist")

        private val CONFIG_ACTION_ID_ABSENCE_API_KEY_ID = ConfigBlockId("ABSENCE_API_KEY_ID")
        private val CONFIG_ACTION_ID_ABSENCE_API_KEY = ConfigBlockId("ABSENCE_API_KEY")
        private val CONFIG_ACTION_ID_USER_LIST = ConfigBlockId("USER_LIST")

        private const val RESPONSE_TYPE_IN_CHANNEL = "in_channel"
    }
}
