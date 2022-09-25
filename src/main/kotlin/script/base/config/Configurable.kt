package script.base.config

import script.base.Script
import script.base.config.block.ConfigBlock
import script.base.config.block.ConfigBlockId
import script.base.config.block.ConfigBlockResponse
import util.slack.user.SlackUser

interface Configurable : Script {

    val configBlockIds: List<ConfigBlockId>

    fun getConfigBlocks(): List<ConfigBlock>

    fun onConfigChange(
        user: SlackUser,
        response: ConfigBlockResponse<*>
    )
}
