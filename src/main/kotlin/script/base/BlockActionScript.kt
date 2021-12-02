package script.base

import com.slack.api.bolt.context.builtin.ActionContext
import com.slack.api.bolt.request.builtin.BlockActionRequest
import model.blockaction.BlockActionId

interface BlockActionScript : Script {

    val blockActionIds: List<BlockActionId>

    fun onBlockActionEvent(
        blockActionId: BlockActionId,
        request: BlockActionRequest,
        ctx: ActionContext
    )
}
