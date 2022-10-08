package script.home.block

import com.slack.api.bolt.request.builtin.BlockActionRequest
import com.slack.api.model.block.Blocks.actions
import com.slack.api.model.block.Blocks.section
import com.slack.api.model.block.LayoutBlock
import com.slack.api.model.block.composition.BlockCompositions.confirmationDialog
import com.slack.api.model.block.composition.BlockCompositions.markdownText
import com.slack.api.model.block.composition.BlockCompositions.plainText
import com.slack.api.model.block.element.BlockElements.button
import model.blockaction.BlockActionId
import model.script.ScriptId
import repository.admin.AdminRepository
import script.base.ScriptHandler
import util.slack.block.headerSection
import util.slack.block.markdownSection
import util.slack.block.plainTextSection
import util.slack.user.SlackUser
import util.slack.user.isBotAdmin

class AdminBlocks(
    private val adminRepo: AdminRepository,
    private val scriptHandler: ScriptHandler
) {

    fun createBlocks(
        slackUser: SlackUser
    ): List<LayoutBlock>? {
        if (!slackUser.isBotAdmin) return null

        val isBotEnabled = adminRepo.isBotEnabled()

        return buildList {
            this += headerSection(text = ":zap: Admin", emoji = true)

            this += section { section ->
                section
                    .text(markdownText("Bot ist *${if (isBotEnabled) "eingeschaltet :large_green_circle:" else "ausgeschaltet :red_circle:"}*"))
                    .accessory(
                        button {
                            it.actionId(BLOCK_ACTION_ID_BOT_ENABLED_SELECTED.id)
                            it.value(isBotEnabled.not().toString())
                            it.text(plainText(if (!isBotEnabled) "Einschalten" else "Ausschalten", false))
                            it.style(if (!isBotEnabled) "primary" else "danger")
                            it.confirm(
                                confirmationDialog { dialog ->
                                    dialog.title(plainText(if (!isBotEnabled) "Bot einschalten" else "Bot ausschalten"))
                                    dialog.style(if (!isBotEnabled) "primary" else "danger")
                                    dialog.text(plainText("Bist du sicher?"))
                                    dialog.confirm(plainText(if (!isBotEnabled) "Einschalten" else "Ausschalten"))
                                    dialog.deny(plainText("Abbrechen"))
                                }
                            )
                        }
                    )
            }

            val scriptElements = adminRepo.getScriptsById(scriptHandler.getScriptIds()).map { script ->
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
        slackUser: SlackUser,
        request: BlockActionRequest
    ) {
        if (!slackUser.isBotAdmin) return

        val isBotEnabled = request.getSelectedBotEnabledValue()
        if (isBotEnabled != null) adminRepo.setBotEnabled(slackUser, isBotEnabled)
    }

    fun onActionScriptEnabledSelected(
        slackUser: SlackUser,
        request: BlockActionRequest
    ) {
        if (!slackUser.isBotAdmin) return

        val scriptEnabledAction = request.getSelectedScriptEnabledAction()
        if (scriptEnabledAction != null) adminRepo.setScriptEnabled(slackUser, scriptEnabledAction.id, scriptEnabledAction.enabled)
    }

    private fun BlockActionRequest.getSelectedBotEnabledValue() = payload?.actions?.find { it?.actionId == BLOCK_ACTION_ID_BOT_ENABLED_SELECTED.id }?.value?.toBoolean()

    private fun BlockActionRequest.getSelectedScriptEnabledAction(): ScriptEnabledAction? {
        val value = payload?.actions?.find { it?.actionId?.startsWith(BLOCK_ACTION_ID_SCRIPT_ENABLED_SELECTED_PREFIX) ?: false }?.value ?: return null
        val scriptIdToEnabled = REGEX_ACTION_SCRIPT_ENABLED_KEY_VALUE.split(value)

        val scriptId = scriptIdToEnabled.firstOrNull()?.let { ScriptId(it) }
        val enabled = scriptIdToEnabled.getOrNull(1)?.lowercase()?.toBooleanStrictOrNull()

        return if (scriptId != null && enabled != null) ScriptEnabledAction(scriptId, enabled) else null
    }

    private data class ScriptEnabledAction(
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
