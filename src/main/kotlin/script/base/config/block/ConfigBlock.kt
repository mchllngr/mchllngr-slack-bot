package script.base.config.block

import com.slack.api.model.block.LayoutBlock
import model.script.ScriptId

sealed class ConfigBlock(
    scriptId: ScriptId,
    id: ConfigBlockId
) {

    val blockId = "${scriptId.id}${ACTION_ID_SEPARATOR}${id.id}"
    val actionId = id.id

    abstract fun getLayoutBlock(): LayoutBlock

    companion object {

        private const val ACTION_ID_SEPARATOR = ";;|;;"

        fun String.extractScriptIdAndConfigBlockIdFromBlockId(): Pair<ScriptId, ConfigBlockId> {
            val split = split(ACTION_ID_SEPARATOR)
            if (split.size != 2) return ScriptId("ERROR") to ConfigBlockId("ERROR") // #26 determine how errors of any kind should be handled and shown
            return ScriptId(split[0]) to ConfigBlockId(split[1])
        }
    }
}
