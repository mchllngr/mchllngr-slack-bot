package script.reviewlist

import com.slack.api.app_backend.slash_commands.response.SlashCommandResponse
import com.slack.api.bolt.context.builtin.SlashCommandContext
import com.slack.api.bolt.request.builtin.SlashCommandRequest
import model.command.CommandId
import model.script.ScriptId
import script.base.CommandScript
import script.base.config.Configurable
import script.base.config.block.ConfigBlockId
import script.base.config.block.ConfigBlockMultiUsersSelect
import script.base.config.block.ConfigBlockResponse
import script.base.config.block.ConfigBlockText
import util.logger.getLogger
import util.slack.block.markdownSection
import util.slack.user.SlackUser

class ReviewListScript : CommandScript, Configurable {

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
        ctx.respond(
            SlashCommandResponse.builder()
                .responseType(RESPONSE_TYPE_IN_CHANNEL)
                .blocks(
                    listOf(
                        markdownSection("Received command: *'$commandId'*")
                    )
                )
                .build()
        )
    }

    override fun getConfigBlocks() = listOf(
        ConfigBlockText(
            scriptId = id,
            id = CONFIG_ACTION_ID_ABSENCE_API_KEY,
            label = "absence.io API Key",
            placeholder = "API Key"
        ),
        ConfigBlockMultiUsersSelect(
            scriptId = id,
            id = CONFIG_ACTION_ID_USER_LIST,
            label = "User List",
            placeholder = "Some Placeholder"
        )
    )

    override fun onConfigChange(
        user: SlackUser,
        response: ConfigBlockResponse<*>
    ) {
        when {
            response.configBlockId == CONFIG_ACTION_ID_ABSENCE_API_KEY && response is ConfigBlockResponse.Text -> {
                getLogger().debug("handle ${CONFIG_ACTION_ID_ABSENCE_API_KEY.id}") // TODO
            }
            response.configBlockId == CONFIG_ACTION_ID_USER_LIST && response is ConfigBlockResponse.MultiUsersSelect -> {
                getLogger().debug("handle ${CONFIG_ACTION_ID_USER_LIST.id}") // TODO
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