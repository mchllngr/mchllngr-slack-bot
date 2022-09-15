package script.base

import com.slack.api.bolt.context.builtin.SlashCommandContext
import com.slack.api.bolt.request.builtin.SlashCommandRequest
import model.command.CommandId

interface CommandScript : Script {

    val commandIds: List<CommandId>

    fun onCommandEvent(
        commandId: CommandId,
        request: SlashCommandRequest,
        ctx: SlashCommandContext
    )
}
