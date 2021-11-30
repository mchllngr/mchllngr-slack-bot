package script.home.block

import com.slack.api.model.User
import com.slack.api.model.block.LayoutBlock
import util.slack.block.headerSection
import util.slack.user.isBotAdmin

class AdminBlocks {

    fun createBlocks(
        user: User?
    ): List<LayoutBlock>? {
        if (!user.isBotAdmin) return null

        return listOf(
            headerSection(text = ":zap: Admin", emoji = true)
        )
    }
}
