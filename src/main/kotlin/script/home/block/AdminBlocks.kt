package script.home.block

import com.slack.api.bolt.request.builtin.BlockActionRequest
import com.slack.api.model.block.Blocks.section
import com.slack.api.model.block.LayoutBlock
import com.slack.api.model.block.composition.BlockCompositions.confirmationDialog
import com.slack.api.model.block.composition.BlockCompositions.markdownText
import com.slack.api.model.block.composition.BlockCompositions.plainText
import com.slack.api.model.block.element.BlockElements.button
import model.blockaction.BlockActionId
import repository.admin.AdminRepository
import util.slack.block.headerSection
import util.slack.user.SlackUser
import util.slack.user.isBotAdmin

class AdminBlocks(
    private val adminRepo: AdminRepository
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

    private fun BlockActionRequest.getSelectedBotEnabledValue() = payload?.actions?.find { it?.actionId == BLOCK_ACTION_ID_BOT_ENABLED_SELECTED.id }?.value?.toBoolean()

    companion object {

        val BLOCK_ACTION_ID_BOT_ENABLED_SELECTED = BlockActionId.Admin.Str("BOT_ENABLED_SELECTED")
    }
}
