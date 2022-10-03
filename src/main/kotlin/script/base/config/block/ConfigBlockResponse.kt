package script.base.config.block

import com.slack.api.model.view.ViewState
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

        fun from(blockId: String, value: ViewState.Value): ConfigBlockResponse<*> {
            val (scriptId, configBlockId) = blockId.extractScriptIdAndConfigBlockIdFromBlockId()
            return when (value.type) {
                "plain_text_input" -> Text(scriptId, configBlockId, value.value)
                "multi_users_select" -> MultiUsersSelect(scriptId, configBlockId, value.selectedUsers.map { UserId(it) })
                else -> error("unknown ConfigBlock type") // #26 determine how errors of any kind should be handled and shown
            }
        }
    }
}
