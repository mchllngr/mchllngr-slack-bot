package script.home.block

import com.slack.api.bolt.request.builtin.BlockActionRequest
import com.slack.api.model.block.LayoutBlock
import model.blockaction.BlockActionId
import repository.admin.AdminRepository
import script.base.ScriptHandler
import script.base.config.Configurable
import script.base.config.block.ConfigBlockResponse
import util.logger.getLogger
import util.slack.block.headerSection
import util.slack.block.markdownSection
import util.slack.user.SlackUser
import util.slack.user.isBotAdmin

class ScriptConfigBlocks(
    private val adminRepo: AdminRepository,
    private val scriptHandler: ScriptHandler
) {

    private val logger = getLogger()

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
        request.getConfigBlockResponses().forEach { response ->
            configurableScripts
                .filter { (scriptId, script) -> scriptId == response.scriptId && response.configBlockId in script.configBlockIds }
                .forEach { (_, script) ->
                    logger.debug(
                        """Config changes:
                        |    ScriptId: ${response.scriptId.id}
                        |    User: ${user.id}
                        |    ConfigBlockId: ${response.configBlockId.id}
                        |    New value: ${response.value}
                        """.trimMargin()
                    )
                    script.onConfigChange(user, response)
                }
        }
    }

    private fun BlockActionRequest.getConfigBlockResponses() = payload?.actions
        ?.filterNotNull()
        ?.map { ConfigBlockResponse.from(it) }
        ?: emptyList()
}
