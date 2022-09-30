package script.base.config.block

import com.slack.api.app_backend.interactive_components.payload.BlockActionPayload
import model.script.ScriptId
import model.user.UserId
import script.base.config.block.ConfigBlock.Companion.extractScriptIdAndConfigBlockIdFromBlockId

sealed interface ConfigBlockResponse<T> {

    val scriptId: ScriptId
    val configBlockId: ConfigBlockId
    val value: T

    data class Text(
        override val scriptId: ScriptId,
        override val configBlockId: ConfigBlockId,
        override val value: String?
    ) : ConfigBlockResponse<String?>

    data class MultiUsersSelect(
        override val scriptId: ScriptId,
        override val configBlockId: ConfigBlockId,
        override val value: List<UserId>
    ) : ConfigBlockResponse<List<UserId>>

    companion object {

        fun from(action: BlockActionPayload.Action): ConfigBlockResponse<*> {
            val (scriptId, configBlockId) = action.blockId.extractScriptIdAndConfigBlockIdFromBlockId()
            return when (action.type) {
                "plain_text_input" -> Text(scriptId, configBlockId, action.value)
                "multi_users_select" -> MultiUsersSelect(scriptId, configBlockId, action.selectedUsers.map { UserId(it) })
                else -> error("unknown ConfigBlock type") // #26 determine how errors of any kind should be handled and shown
            }
        }
    }
}
