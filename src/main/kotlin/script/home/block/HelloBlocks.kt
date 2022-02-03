package script.home.block

import com.slack.api.model.block.LayoutBlock
import util.slack.block.headerSection
import util.slack.user.SlackUser

class HelloBlocks {

    fun createBlocks(
        slackUser: SlackUser?
    ): List<LayoutBlock> {
        val realName = slackUser?.realName

        return listOf(
            headerSection(
                text = buildString {
                    append(":wave: Hallo")
                    if (realName.isNullOrBlank()) append("") else append(" $realName")
                    append("!")
                },
                emoji = true
            )
        )
    }
}
