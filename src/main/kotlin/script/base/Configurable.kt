package script.base

import com.slack.api.model.block.LayoutBlock

interface Configurable {

    fun getConfigBlocks(): List<LayoutBlock>
}
