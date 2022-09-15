package script.reviewlist

import com.slack.api.app_backend.slash_commands.response.SlashCommandResponse
import com.slack.api.bolt.context.builtin.SlashCommandContext
import com.slack.api.bolt.request.builtin.SlashCommandRequest
import model.command.CommandId
import model.script.ScriptId
import script.base.CommandScript

class ReviewListScript : CommandScript {

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
                .text("Received command: '$commandId'")
                .build()
        )
    }

    companion object {

        val ID = ScriptId("REVIEW_LIST")

        private val COMMAND_ID_REVIEW_LIST = CommandId.User.Str("/reviewlist")

        private const val RESPONSE_TYPE_IN_CHANNEL = "in_channel"
    }
}
