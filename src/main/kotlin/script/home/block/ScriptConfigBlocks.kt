package script.home.block

import com.slack.api.bolt.context.Context
import com.slack.api.bolt.context.builtin.ActionContext
import com.slack.api.bolt.request.builtin.BlockActionRequest
import com.slack.api.model.block.Blocks.actions
import com.slack.api.model.block.LayoutBlock
import com.slack.api.model.block.composition.BlockCompositions.plainText
import com.slack.api.model.block.element.BlockElements.button
import com.slack.api.model.view.ViewClose
import com.slack.api.model.view.ViewSubmit
import com.slack.api.model.view.ViewTitle
import com.slack.api.model.view.Views.view
import model.blockaction.BlockActionId
import model.script.ScriptId
import repository.admin.AdminRepository
import script.base.ScriptHandler
import script.base.config.Configurable
import script.base.config.block.ConfigBlockResponse
import servicelocator.ServiceLocator.config
import util.logger.getLogger
import util.slack.block.headerSection
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
    private val scriptConfigIds by lazy {
        configurableScripts.map { (scriptId, _) -> scriptId.configId }
    }

    val blockActionIds by lazy {
        buildList {
            configurableScripts.forEach { (scriptId, script) ->
                val blocks = script.getConfigBlocks()
                if (blocks.isEmpty()) return@forEach

                this += BlockActionId.User.Str(scriptId.configId)
                blocks.forEach { this += BlockActionId.User.Str(it.actionId) }
            }
        }
    }

    fun createBlocks(
        slackUser: SlackUser
    ): List<LayoutBlock>? {
        if (!slackUser.isBotAdmin) return null // #24 Allow setting script admins that can change the script-config

        val scriptIds = configurableScripts.keys
        if (scriptIds.isEmpty()) return null

        return buildList {
            this += headerSection(text = ":clipboard: Skripte-Konfiguration", emoji = true)
            this += getScriptConfigButtons(scriptIds)
        }
    }

    private fun getScriptConfigButtons(scriptIds: Set<ScriptId>): LayoutBlock {
        return actions(
            scriptIds.map { scriptId ->
                button {
                    it.actionId(scriptId.configId)
                    it.text(plainText(scriptId.id))
                }
            }
        )
    }

    fun onBlockActionEvent(
        user: SlackUser,
        request: BlockActionRequest,
        ctx: ActionContext
    ) {
        when (val response = request.getResponse()) {
            is BlockActionResponse.ScriptConfigButtonClicked -> handleScriptConfigButtonClicked(response, ctx)
            is BlockActionResponse.ScriptConfigSaved -> handleScriptConfigSaved(user, response)
            null -> Unit // ignore
        }
    }

    private fun BlockActionRequest.getResponse(): BlockActionResponse? {
        val actions = payload.actions.filterNotNull()
        if (actions.isEmpty()) return null

        actions.firstOrNull()?.actionId?.let { actionId ->
            if (actionId in scriptConfigIds) return BlockActionResponse.ScriptConfigButtonClicked(actionId, payload.triggerId)
        }

        return BlockActionResponse.ScriptConfigSaved(actions.map { ConfigBlockResponse.from(it) })
    }

    private fun handleScriptConfigButtonClicked(
        response: BlockActionResponse.ScriptConfigButtonClicked,
        ctx: ActionContext
    ) {
        val scriptId = response.scriptConfigId.substring(SCRIPT_CONFIG_ID_PREFIX.length).let { ScriptId(it) }
        val script = configurableScripts[scriptId] ?: return

        ctx.showScriptConfigModal(scriptId, script, response.triggerId)
    }

    private fun handleScriptConfigSaved(
        user: SlackUser,
        response: BlockActionResponse.ScriptConfigSaved
    ) {
        response.values.forEach { value ->
            configurableScripts
                .filter { (scriptId, script) -> scriptId == value.scriptId && value.configBlockId in script.configBlockIds }
                .forEach { (_, script) ->
                    logger.debug(
                        """Config changes:
                        |    ScriptId: ${value.scriptId.id}
                        |    User: ${user.id}
                        |    ConfigBlockId: ${value.configBlockId.id}
                        |    New value: ${value.value}
                        """.trimMargin()
                    )
                    script.onConfigChange(user, value)
                }
        }
    }

    private fun Context.showScriptConfigModal(
        scriptId: ScriptId,
        script: Configurable,
        triggerId: String
    ) {
        val blocks = script.getConfigBlocks().map { it.getLayoutBlock() }

        client().viewsOpen { requestBuilder ->
            requestBuilder
                .token(config.token.bot)
                .triggerId(triggerId)
                .view(
                    view { view ->
                        view
                            .callbackId(scriptId.configId)
                            .title(
                                ViewTitle(
                                    "plain_text",
                                    scriptId.id.take(24), // "must be less than 25 characters"
                                    false
                                )
                            )
                            .submit(
                                ViewSubmit(
                                    "plain_text",
                                    "Speichern",
                                    false
                                )
                            )
                            .close(
                                ViewClose(
                                    "plain_text",
                                    "Verwerfen",
                                    false
                                )
                            )
                            .type("modal")
                            .blocks(blocks)
                    }
                )
        }
    }

    private val ScriptId.configId get() = "${SCRIPT_CONFIG_ID_PREFIX}$id"

    private sealed interface BlockActionResponse {

        data class ScriptConfigButtonClicked(
            val scriptConfigId: String,
            val triggerId: String
        ) : BlockActionResponse

        data class ScriptConfigSaved(val values: List<ConfigBlockResponse<*>>) : BlockActionResponse
    }

    companion object {

        private const val SCRIPT_CONFIG_ID_PREFIX = "SCRIPT_CONFIG_"
    }
}
