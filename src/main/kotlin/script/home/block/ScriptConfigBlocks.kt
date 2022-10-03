package script.home.block

import com.slack.api.bolt.context.Context
import com.slack.api.bolt.context.builtin.ActionContext
import com.slack.api.bolt.request.builtin.BlockActionRequest
import com.slack.api.bolt.request.builtin.ViewSubmissionRequest
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
import model.view.submission.ViewSubmissionId
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

//    val viewSubmissionIds by lazy {
//        configurableScripts.flatMap { script ->
//            script.value
//                .getConfigBlocks()
//                .map { ViewSubmissionId.User.Str(it.actionId) }
//        }
//    }

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
        request: BlockActionRequest,
        ctx: ActionContext
    ) {
        val response = request.getBlockActionResponse() ?: return

        val scriptId = response.scriptConfigId.substring(SCRIPT_CONFIG_ID_PREFIX.length).let { ScriptId(it) }
        val script = configurableScripts[scriptId] ?: return

        ctx.showScriptConfigModal(scriptId, script, response.triggerId)
    }

    private fun BlockActionRequest.getBlockActionResponse() = payload.actions
        .filterNotNull()
        .firstOrNull()
        ?.actionId
        ?.let { ScriptConfigButtonClickedResponse(it, payload.triggerId) }

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

    fun onViewSubmissionEvent(
        user: SlackUser,
        request: ViewSubmissionRequest
    ) {
        val response = request.getViewSubmissionResponse() ?: return
        onScriptConfigChanges(user, response)
    }

    private fun ViewSubmissionRequest.getViewSubmissionResponse(): ScriptConfigSavedResponse? {
        // #32 improve receiving script config changes made in modal

        val responses = payload.view?.state?.values
            ?.mapNotNull {
                val blockId = it.key ?: return@mapNotNull null
                val value = it.value.values.firstOrNull() ?: return@mapNotNull null
                return@mapNotNull blockId to value
            }
            ?.map { (blockId, value) -> ConfigBlockResponse.from(blockId, value) }
            ?: return null

        return ScriptConfigSavedResponse(responses)
    }

    private fun onScriptConfigChanges(
        user: SlackUser,
        response: ScriptConfigSavedResponse
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

    private val ScriptId.configId get() = "${SCRIPT_CONFIG_ID_PREFIX}$id"

    data class ScriptConfigButtonClickedResponse(
        val scriptConfigId: String,
        val triggerId: String
    )

    data class ScriptConfigSavedResponse(val values: List<ConfigBlockResponse<*>>)

    companion object {

        private const val SCRIPT_CONFIG_ID_PREFIX = "SCRIPT_CONFIG_"
        val BLOCK_ACTION_SCRIPT_CONFIG_ID = BlockActionId.User.Regex("^$SCRIPT_CONFIG_ID_PREFIX.+$".toRegex())
        val VIEW_SUBMISSION_SCRIPT_CONFIG_ID = ViewSubmissionId.User.Regex("^$SCRIPT_CONFIG_ID_PREFIX.+$".toRegex())
    }
}
