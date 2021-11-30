package script.home.block

import com.slack.api.model.block.Blocks.section
import com.slack.api.model.block.LayoutBlock
import com.slack.api.model.block.composition.BlockCompositions.markdownText
import com.slack.api.model.block.composition.BlockCompositions.plainText
import util.slack.block.headerSection
import util.slack.block.markdownSection
import util.slack.block.plainTextSection

class TeamBlocks {

    fun createBlocks(): List<LayoutBlock> = listOf(
        headerSection(text = ":busts_in_silhouette: Team", emoji = true),
        markdownSection("Du gehörst zum Team *TODO*."),
        plainTextSection("Teammitglieder:"),
        section {
            it.fields(
                listOf(
                    markdownText("Person 0 *(Teamadmin)*"),
                    markdownText("Person 1 *(Teamadmin)*"),
                    plainText("Person 2"),
                    plainText("Person 3"),
                    plainText("Person 4")
                )
            )
        }
    )
}
