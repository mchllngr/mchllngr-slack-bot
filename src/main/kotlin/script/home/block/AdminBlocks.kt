package script.home.block

import com.slack.api.bolt.context.builtin.ActionContext
import com.slack.api.bolt.request.builtin.BlockActionRequest
import com.slack.api.model.User
import com.slack.api.model.block.Blocks.section
import com.slack.api.model.block.LayoutBlock
import com.slack.api.model.block.composition.BlockCompositions.confirmationDialog
import com.slack.api.model.block.composition.BlockCompositions.markdownText
import com.slack.api.model.block.composition.BlockCompositions.plainText
import com.slack.api.model.block.element.BlockElements.button
import script.base.ScriptHandler
import service.admin.AdminService
import util.context.getUser
import util.slack.block.headerSection
import util.slack.user.isBotAdmin

class AdminBlocks(
    private val adminService: AdminService,
    private val scriptHandler: ScriptHandler
) {

    fun createBlocks(
        user: User?
    ): List<LayoutBlock>? {
        if (!user.isBotAdmin) return null

        val isBotEnabled = adminService.isBotEnabled()

        return listOf(
            headerSection(text = ":zap: Admin", emoji = true),
            section { section ->
                section
                    .text(markdownText("Bot ist *${if (isBotEnabled) "eingeschaltet :large_green_circle:" else "ausgeschaltet :red_circle:"}*"))
                    .accessory(
                        button {
                            it.actionId(ACTION_BOT_ENABLED_SELECTED)
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
        )
    }

    fun onActionBotEnabledSelected(
        request: BlockActionRequest,
        ctx: ActionContext
    ) {
        val user = ctx.getUser(request.payload.user.id)
        if (!user.isBotAdmin) return

        val isBotEnabled = request.getSelectedBotEnabledValue()
        if (isBotEnabled != null) adminService.setBotEnabled(user, isBotEnabled)
    }

    private fun BlockActionRequest.getSelectedBotEnabledValue() = payload?.actions?.find { it?.actionId == ACTION_BOT_ENABLED_SELECTED }?.value?.toBoolean()

    companion object {

        const val ACTION_BOT_ENABLED_SELECTED = "BOT_ENABLED_SELECTED"
    }
}
