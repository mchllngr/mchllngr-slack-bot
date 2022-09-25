package script.base.config

import com.slack.api.model.block.LayoutBlock

sealed interface ConfigBlock {

    val actionId: String

    fun getLayoutBlock(): LayoutBlock
}
