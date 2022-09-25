package script.home.block

import com.slack.api.bolt.request.builtin.BlockActionRequest
import com.slack.api.model.block.LayoutBlock
import model.blockaction.BlockActionId
import model.script.ScriptId
import repository.admin.AdminRepository
import script.base.ScriptHandler
import script.base.config.ConfigBlockId
import script.base.config.Configurable
import util.slack.block.headerSection
import util.slack.block.markdownSection
import util.slack.user.SlackUser
import util.slack.user.isBotAdmin

class ScriptConfigBlocks(
    private val adminRepo: AdminRepository,
    private val scriptHandler: ScriptHandler
) {

    private val configurableScripts by lazy {
        scriptHandler.getScripts()
            .filterValues { it is Configurable }
            .mapValues { it.value as Configurable }
    }

    val blockActionIds by lazy {
        configurableScripts.flatMap { script ->
            script.value
                .getConfigBlocks()
                .map { BlockActionId.User.Str(it.actionId) }
        }
    }

    fun createBlocks(
        slackUser: SlackUser
    ): List<LayoutBlock>? {
        if (!slackUser.isBotAdmin) return null // #24 Allow setting script admins that can change the script-config

        val scripts = configurableScripts
        if (scripts.isEmpty()) return null

        return buildList {
            this += headerSection(text = ":clipboard: Skripte-Konfiguration", emoji = true)

            scripts.forEach { (id, script) ->
                this += markdownSection("Skript *${id.id}*")
                this += script.getConfigBlocks().map { it.getLayoutBlock() }
            }

        }
    }

    fun onBlockActionEvent(
        user: SlackUser,
        request: BlockActionRequest
    ) {
        request.getActions().forEach { action ->
            configurableScripts
                .filter { (scriptId, script) -> scriptId == action.scriptId && action.configBlockId in script.configBlockIds }
                .forEach { (_, script) -> script.onConfigChange(user, action.configBlockId, action.value) }
        }
    }

    private fun BlockActionRequest.getActions() = payload?.actions?.map { action ->
        ConfigAction(
            ScriptId(action.blockId),
            ConfigBlockId(action.actionId),
            action.value
        )
    } ?: emptyList()

    private data class ConfigAction(
        val scriptId: ScriptId,
        val configBlockId: ConfigBlockId,
        val value: String?
    )
}
