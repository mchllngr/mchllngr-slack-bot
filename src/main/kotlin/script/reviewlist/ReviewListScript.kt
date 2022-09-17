package script.reviewlist

import com.slack.api.app_backend.slash_commands.response.SlashCommandResponse
import com.slack.api.bolt.context.builtin.SlashCommandContext
import com.slack.api.bolt.request.builtin.SlashCommandRequest
import com.slack.api.model.block.LayoutBlock
import model.command.CommandId
import model.script.ScriptId
import script.base.CommandScript
import script.base.Configurable
import util.slack.block.markdownSection

class ReviewListScript : CommandScript, Configurable {

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

    override fun getConfigBlocks(): List<LayoutBlock> {
        return listOf(
            markdownSection("TODO config")
        )
    }

    companion object {

        val ID = ScriptId("REVIEW_LIST")

        private val COMMAND_ID_REVIEW_LIST = CommandId.User.Str("/reviewlist")

        private const val RESPONSE_TYPE_IN_CHANNEL = "in_channel"
    }
}
