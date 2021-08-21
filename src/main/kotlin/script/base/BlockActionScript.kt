package script.base

import com.slack.api.bolt.context.builtin.ActionContext
import com.slack.api.bolt.request.builtin.BlockActionRequest

interface BlockActionScript : Script {

    val blockActionIds: List<String>

    fun onBlockActionEvent(
        blockActionId: String,
        request: BlockActionRequest,
        ctx: ActionContext
    )
}
