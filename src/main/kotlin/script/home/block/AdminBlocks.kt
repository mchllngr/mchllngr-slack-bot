package script.home.block

import com.slack.api.bolt.context.Context
import com.slack.api.bolt.request.builtin.BlockActionRequest
import com.slack.api.model.User
import com.slack.api.model.block.Blocks.actions
import com.slack.api.model.block.Blocks.section
import com.slack.api.model.block.LayoutBlock
import com.slack.api.model.block.composition.BlockCompositions.confirmationDialog
import com.slack.api.model.block.composition.BlockCompositions.markdownText
import com.slack.api.model.block.composition.BlockCompositions.plainText
import com.slack.api.model.block.element.BlockElements.button
import model.blockaction.BlockActionId
import model.script.ScriptId
import script.base.ScriptHandler
import service.admin.AdminService
import util.slack.block.headerSection
import util.slack.block.markdownSection
import util.slack.block.plainTextSection
import util.slack.context.getUser
import util.slack.user.isBotAdmin

class AdminBlocks(
    private val adminService: AdminService,
    private val scriptHandler: ScriptHandler
) {

    @OptIn(ExperimentalStdlibApi::class)
    fun createBlocks(
        user: User?
    ): List<LayoutBlock>? {
        if (!user.isBotAdmin) return null

        val isBotEnabled = adminService.isBotEnabled()

        return buildList {
            this += headerSection(text = ":zap: Admin", emoji = true)

            this += section { section ->
                section
                    .text(markdownText("Bot ist *${if (isBotEnabled) "eingeschaltet :large_green_circle:" else "ausgeschaltet :red_circle:"}*"))
                    .accessory(
                        button {
                            it.actionId(BLOCK_ACTION_ID_BOT_ENABLED_SELECTED.id)
                            it.value(isBotEnabled.not().toString())
                            it.text(plainText(if (!isBotEnabled) ":large_green_circle: Einschalten" else ":red_circle: Ausschalten", true))
                            it.confirm(
                                confirmationDialog { dialog ->
                                    dialog.title(plainText(if (!isBotEnabled) "Bot einschalten" else "Bot ausschalten"))
                                    dialog.text(plainText("Bist du sicher?"))
                                    dialog.confirm(plainText(if (!isBotEnabled) "Einschalten" else "Ausschalten"))
                                    dialog.deny(plainText("Abbrechen"))
                                }
                            )
                        }
                    )
            }

            val scriptElements = adminService.getScriptsById(scriptHandler.getScriptIds()).map { script ->
                button {
                    it.actionId(BLOCK_ACTION_ID_SCRIPT_ENABLED_SELECTED_PREFIX + script.id.id)
                    it.value(script.id.id + ACTION_SCRIPT_ENABLED_KEY_VALUE_SEPARATOR + script.enabled.not().toString())
                    it.text(plainText("${if (script.enabled) ":large_green_circle:" else ":red_circle:"} ${script.id.id}", true))
                    it.confirm(
                        confirmationDialog { dialog ->
                            dialog.title(plainText("Skript '${script.id.id}' ${if (!script.enabled) "einschalten" else "ausschalten"}"))
                            dialog.text(plainText("Bist du sicher?"))
                            dialog.confirm(plainText(if (!script.enabled) "Einschalten" else "Ausschalten"))
                            dialog.deny(plainText("Abbrechen"))
                        }
                    )
                }
            }

            this += plainTextSection("Skripte:")
            this += if (scriptElements.isNotEmpty()) {
                actions(scriptElements)
            } else {
                markdownSection("_Keine Skripte registriert_")
            }
        }
    }

    fun onActionBotEnabledSelected(
        request: BlockActionRequest,
        ctx: Context
    ) {
        val user = ctx.getUser(request)
        if (!user.isBotAdmin) return

        val isBotEnabled = request.getSelectedBotEnabledValue()
        if (isBotEnabled != null) adminService.setBotEnabled(user, isBotEnabled)
    }

    fun onActionScriptEnabledSelected(
        request: BlockActionRequest,
        ctx: Context
    ) {
        val user = ctx.getUser(request)
        if (!user.isBotAdmin) return

        val scriptEnabledAction = request.getSelectedScriptEnabledAction()
        if (scriptEnabledAction != null) adminService.setScriptEnabled(user, scriptEnabledAction.id, scriptEnabledAction.enabled)
    }

    private fun BlockActionRequest.getSelectedBotEnabledValue() = payload?.actions?.find { it?.actionId == BLOCK_ACTION_ID_BOT_ENABLED_SELECTED.id }?.value?.toBoolean()

    private fun BlockActionRequest.getSelectedScriptEnabledAction(): ScriptEnabledAction? {
        val value = payload?.actions?.find { it?.actionId?.startsWith(BLOCK_ACTION_ID_SCRIPT_ENABLED_SELECTED_PREFIX) ?: false }?.value ?: return null
        val scriptIdToEnabled = REGEX_ACTION_SCRIPT_ENABLED_KEY_VALUE.split(value)

        val scriptId = scriptIdToEnabled.firstOrNull()?.let { ScriptId(it) }
        val enabled = scriptIdToEnabled.getOrNull(1)?.lowercase()?.toBooleanStrictOrNull()

        return if (scriptId != null && enabled != null) ScriptEnabledAction(scriptId, enabled) else null
    }

    data class ScriptEnabledAction(
        val id: ScriptId,
        val enabled: Boolean
    )

    companion object {

        private const val ACTION_SCRIPT_ENABLED_KEY_VALUE_SEPARATOR = "="
        private val REGEX_ACTION_SCRIPT_ENABLED_KEY_VALUE by lazy { ACTION_SCRIPT_ENABLED_KEY_VALUE_SEPARATOR.toRegex() }

        val BLOCK_ACTION_ID_BOT_ENABLED_SELECTED = BlockActionId.Admin.Str("BOT_ENABLED_SELECTED")
        private const val BLOCK_ACTION_ID_SCRIPT_ENABLED_SELECTED_PREFIX = "SCRIPT_ENABLED_SELECTED_"
        val BLOCK_ACTION_ID_SCRIPT_ENABLED_SELECTED = BlockActionId.Admin.Regex("^$BLOCK_ACTION_ID_SCRIPT_ENABLED_SELECTED_PREFIX.+$".toRegex())
    }
}
