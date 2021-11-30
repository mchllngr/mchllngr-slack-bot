package script.home.block

import com.slack.api.model.User
import com.slack.api.model.block.LayoutBlock
import util.slack.block.headerSection

class HelloBlocks {

    fun createBlocks(
        user: User?
    ): List<LayoutBlock> {
        val realName = user?.realName

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
