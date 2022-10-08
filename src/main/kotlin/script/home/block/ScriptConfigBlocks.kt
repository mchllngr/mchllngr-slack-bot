package script.home.block

import com.slack.api.bolt.context.Context
import com.slack.api.bolt.context.builtin.ActionContext
import com.slack.api.bolt.request.builtin.BlockActionRequest
import com.slack.api.bolt.request.builtin.ViewSubmissionRequest
import com.slack.api.model.block.Blocks.actions
import com.slack.api.model.block.LayoutBlock
import com.slack.api.model.block.composition.BlockCompositions.confirmationDialog
import com.slack.api.model.block.composition.BlockCompositions.plainText
import com.slack.api.model.block.element.BlockElements.button
import db.Script
import model.blockaction.BlockActionId
import model.script.ScriptId
import model.view.submission.ViewSubmissionId
import repository.admin.AdminRepository
import script.base.ScriptHandler
import script.base.config.Configurable
import script.base.config.block.ConfigBlockResponse
import util.logger.getLogger
import util.slack.block.headerSection
import util.slack.block.markdownSection
import util.slack.context.ModalText
import util.slack.context.openModal
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

    fun createBlocks(
        slackUser: SlackUser
    ): List<LayoutBlock>? {
        if (!slackUser.isBotAdmin) return null // #24 Allow setting script admins that can change the script-config

        val scriptIds = scriptHandler.getScripts().keys
        if (scriptIds.isEmpty()) return null

        return buildList {
            this += headerSection(text = ":clipboard: Skripte-Konfiguration", emoji = true)

            adminRepo.getScriptsById(scriptIds).forEach { script ->
                this += getScriptConfigBlocks(script)
            }

        }
    }

    private fun getScriptConfigBlocks(script: Script) = listOf(
        markdownSection("${script.id.id} ist *${if (script.enabled) "eingeschaltet :large_green_circle:" else "ausgeschaltet :red_circle:"}*"),
        actions(
            buildList {
                this += button {
                    it.actionId(BLOCK_ACTION_ID_SCRIPT_ENABLED_SELECTED_PREFIX + script.id.id)
                    it.value(script.id.id + ACTION_SCRIPT_ENABLED_KEY_VALUE_SEPARATOR + script.enabled.not().toString())
                    it.text(plainText(if (!script.enabled) "Einschalten" else "Ausschalten"))
                    it.style(if (!script.enabled) "primary" else "danger")
                    it.confirm(
                        confirmationDialog { dialog ->
                            dialog.title(plainText("Skript '${script.id.id}' ${if (!script.enabled) "einschalten" else "ausschalten"}"))
                            dialog.style(if (!script.enabled) "primary" else "danger")
                            dialog.text(plainText("Bist du sicher?"))
                            dialog.confirm(plainText(if (!script.enabled) "Einschalten" else "Ausschalten"))
                            dialog.deny(plainText("Abbrechen"))
                        }
                    )
                }

                if (script.id in configurableScripts) {
                    this += button {
                        it.actionId(script.id.configId)
                        it.text(plainText("Konfiguration"))
                    }
                }
            }
        )
    )

    fun onActionScriptEnabledSelected(
        user: SlackUser,
        request: BlockActionRequest
    ) {
        if (!user.isBotAdmin) return

        val scriptEnabledAction = request.getSelectedScriptEnabledAction()
        if (scriptEnabledAction != null) adminRepo.setScriptEnabled(user, scriptEnabledAction.id, scriptEnabledAction.enabled)
    }

    private fun BlockActionRequest.getSelectedScriptEnabledAction(): ScriptEnabledAction? {
        val value = payload?.actions?.find { it?.actionId?.startsWith(BLOCK_ACTION_ID_SCRIPT_ENABLED_SELECTED_PREFIX) ?: false }?.value ?: return null
        val scriptIdToEnabled = REGEX_ACTION_SCRIPT_ENABLED_KEY_VALUE.split(value)

        val scriptId = scriptIdToEnabled.firstOrNull()?.let { ScriptId(it) }
        val enabled = scriptIdToEnabled.getOrNull(1)?.lowercase()?.toBooleanStrictOrNull()

        return if (scriptId != null && enabled != null) ScriptEnabledAction(scriptId, enabled) else null
    }

    fun onActionShowScriptConfig(
        user: SlackUser,
        request: BlockActionRequest,
        ctx: ActionContext
    ) {
        if (!user.isBotAdmin) return

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

        openModal(
            triggerId = triggerId,
            callbackId = scriptId.configId,
            title = ModalText(scriptId.id),
            blocks = blocks,
            submit = ModalText("Speichern"),
            close = ModalText("Verwerfen")
        )
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
                        """
                        |Config changes:
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

    private data class ScriptEnabledAction(
        val id: ScriptId,
        val enabled: Boolean
    )

    data class ScriptConfigButtonClickedResponse(
        val scriptConfigId: String,
        val triggerId: String
    )

    data class ScriptConfigSavedResponse(val values: List<ConfigBlockResponse<*>>)

    companion object {

        private const val ACTION_SCRIPT_ENABLED_KEY_VALUE_SEPARATOR = "="
        private val REGEX_ACTION_SCRIPT_ENABLED_KEY_VALUE by lazy { ACTION_SCRIPT_ENABLED_KEY_VALUE_SEPARATOR.toRegex() }

        private const val BLOCK_ACTION_ID_SCRIPT_ENABLED_SELECTED_PREFIX = "SCRIPT_ENABLED_SELECTED_"
        val BLOCK_ACTION_ID_SCRIPT_ENABLED_SELECTED = BlockActionId.Admin.Regex("^$BLOCK_ACTION_ID_SCRIPT_ENABLED_SELECTED_PREFIX.+$".toRegex())

        private const val SCRIPT_CONFIG_ID_PREFIX = "SCRIPT_CONFIG_"
        val BLOCK_ACTION_SCRIPT_CONFIG_ID = BlockActionId.Admin.Regex("^$SCRIPT_CONFIG_ID_PREFIX.+$".toRegex())
        val VIEW_SUBMISSION_SCRIPT_CONFIG_ID = ViewSubmissionId.Admin.Regex("^$SCRIPT_CONFIG_ID_PREFIX.+$".toRegex())
    }
}
