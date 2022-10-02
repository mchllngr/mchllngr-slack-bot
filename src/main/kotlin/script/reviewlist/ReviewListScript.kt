package script.reviewlist

import com.slack.api.app_backend.slash_commands.response.SlashCommandResponse
import com.slack.api.bolt.context.builtin.SlashCommandContext
import com.slack.api.bolt.request.builtin.SlashCommandRequest
import com.slack.api.model.block.LayoutBlock
import model.command.CommandId
import model.script.ScriptId
import script.base.CommandScript
import script.base.config.Configurable
import script.base.config.block.ConfigBlockId
import script.base.config.block.ConfigBlockMultiUsersSelect
import script.base.config.block.ConfigBlockResponse
import script.base.config.block.ConfigBlockText
import servicelocator.ServiceLocator.random
import servicelocator.ServiceLocator.reviewListRepo
import util.charsequence.mask
import util.slack.block.markdownSection
import util.slack.context.getUser
import util.slack.user.SlackUser
import util.slack.user.usernameString

class ReviewListScript : CommandScript, Configurable {

    private val repo by lazy { reviewListRepo }

    override val id = ID

    override val commandIds = listOf(COMMAND_ID_REVIEW_LIST)

    override val configBlockIds = listOf(
        CONFIG_ACTION_ID_ABSENCE_API_KEY,
        CONFIG_ACTION_ID_USER_LIST
    )

    override fun onCommandEvent(
        commandId: CommandId,
        request: SlashCommandRequest,
        ctx: SlashCommandContext
    ) {
        // #29 use Absence to filter out unavailable people in reviewlist

        val reviewUsers = repo.users
            .shuffled(random)
            .mapNotNull { ctx.getUser(it) }
            .mapIndexed { index, user -> "${index + 1}. ${user.usernameString}" }
            .joinToString(separator = "\n")

        ctx.respondInChannel(
            listOf(
                markdownSection(":speaking_head_in_silhouette: *Review-Reihenfolge:*\n$reviewUsers")
            )
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
            id = CONFIG_ACTION_ID_ABSENCE_API_KEY,
            label = "absence.io API Key",
            placeholder = repo.absenceApiKey.mask() ?: "API Key"
        ),
        ConfigBlockMultiUsersSelect(
            scriptId = id,
            id = CONFIG_ACTION_ID_USER_LIST,
            label = "ReviewList Users",
            placeholder = "Users",
            initialUsers = repo.users
        )
    )

    override fun onConfigChange(
        user: SlackUser,
        response: ConfigBlockResponse<*>
    ) {
        when {
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

        private val CONFIG_ACTION_ID_ABSENCE_API_KEY = ConfigBlockId("ABSENCE_API_KEY")
        private val CONFIG_ACTION_ID_USER_LIST = ConfigBlockId("USER_LIST")

        private const val RESPONSE_TYPE_IN_CHANNEL = "in_channel"
    }
}
