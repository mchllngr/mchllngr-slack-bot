package script.home.block

import com.slack.api.model.block.Blocks
import com.slack.api.model.block.LayoutBlock
import com.slack.api.model.block.composition.BlockCompositions
import util.slack.block.headerSection
import util.slack.block.markdownSection
import util.slack.block.plainTextSection

class TeamBlocks {

    fun createBlocks(): List<LayoutBlock> = listOf(
        headerSection(text = ":busts_in_silhouette: Team", emoji = true),
        markdownSection("Du gehörst zum Team *TODO*."),
        plainTextSection("Teammitglieder:"),
        Blocks.section {
            it.fields(
                listOf(
                    BlockCompositions.markdownText("Person 0 *(Teamadmin)*"),
                    BlockCompositions.markdownText("Person 1 *(Teamadmin)*"),
                    BlockCompositions.plainText("Person 2"),
                    BlockCompositions.plainText("Person 3"),
                    BlockCompositions.plainText("Person 4")
                )
            )
        }
    )
}
