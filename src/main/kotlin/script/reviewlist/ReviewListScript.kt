package script.reviewlist

import com.slack.api.app_backend.slash_commands.response.SlashCommandResponse
import com.slack.api.bolt.context.builtin.SlashCommandContext
import com.slack.api.bolt.request.builtin.SlashCommandRequest
import model.command.CommandId
import model.script.ScriptId
import script.base.CommandScript
import script.base.config.Configurable
import script.base.config.block.ConfigBlockId
import script.base.config.block.ConfigBlockText
import util.logger.getLogger
import util.slack.block.markdownSection
import util.slack.user.SlackUser

class ReviewListScript : CommandScript, Configurable {

    private val logger = getLogger()

    override val id = ID

    override val commandIds = listOf(COMMAND_ID_REVIEW_LIST)

    override val configBlockIds = listOf(CONFIG_ACTION_ID_ABSENCE_API_KEY)

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
        )
    )

    override fun onConfigChange(
        user: SlackUser,
        configBlockId: ConfigBlockId,
        value: String?
    ) {
        logger.error("${id.id} Config: ${user.realName} changed '${configBlockId.id}' to '$value'")
    }

    companion object {

        val ID = ScriptId("REVIEW_LIST")

        private val COMMAND_ID_REVIEW_LIST = CommandId.User.Str("/reviewlist")

        private val CONFIG_ACTION_ID_ABSENCE_API_KEY = ConfigBlockId("ABSENCE_API_KEY")

        private const val RESPONSE_TYPE_IN_CHANNEL = "in_channel"
    }
}
