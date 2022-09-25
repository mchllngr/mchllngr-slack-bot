package script.home.block

import com.slack.api.model.block.LayoutBlock
import repository.admin.AdminRepository
import script.base.ScriptHandler
import script.base.config.Configurable
import util.slack.block.headerSection
import util.slack.block.markdownSection
import util.slack.user.SlackUser
import util.slack.user.isBotAdmin

class ScriptConfigBlocks(
    private val adminRepo: AdminRepository,
    private val scriptHandler: ScriptHandler
) {

    fun createBlocks(
        slackUser: SlackUser
    ): List<LayoutBlock>? {
        if (!slackUser.isBotAdmin) return null // #24 Allow setting script admins that can change the script-config

        val scripts = scriptHandler.getScripts()
            .filterValues { it is Configurable }
            .mapValues { it.value as Configurable }

        if (scripts.isEmpty()) return null

        return buildList {
            this += headerSection(text = ":clipboard: Skripte-Konfiguration", emoji = true)

            scripts.forEach { (id, script) ->
                this += markdownSection("Skript *${id.id}*")
                this += script.getConfigBlocks().map { it.getLayoutBlock() }
            }

        }
    }
}
