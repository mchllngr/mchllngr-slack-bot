package script.reviewlist

import com.slack.api.app_backend.slash_commands.response.SlashCommandResponse
import com.slack.api.bolt.context.builtin.SlashCommandContext
import com.slack.api.bolt.request.builtin.SlashCommandRequest
import model.command.CommandId
import model.script.ScriptId
import script.base.CommandScript
import script.base.config.ConfigBlock
import script.base.config.ConfigBlockText
import script.base.config.Configurable
import util.logger.getLogger
import util.slack.block.markdownSection

class ReviewListScript : CommandScript, Configurable {

    private val logger = getLogger()

    override val id = ID

    override val commandIds = listOf(COMMAND_ID_REVIEW_LIST)

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

    override fun getConfigBlocks(): List<ConfigBlock> {
        return listOf(
            ConfigBlockText(
                id = "ABSENCE_API_KEY",
                label = "absence.io API Key",
                placeholder = "API Key"
            ) {
                logger.error("$id Config: $it")
            }
        )
    }

    companion object {

        val ID = ScriptId("REVIEW_LIST")

        private val COMMAND_ID_REVIEW_LIST = CommandId.User.Str("/reviewlist")

        private const val RESPONSE_TYPE_IN_CHANNEL = "in_channel"
    }
}
