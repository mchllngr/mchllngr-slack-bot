package script.base.config

import script.base.Script
import util.slack.user.SlackUser

interface Configurable : Script {

    val configBlockIds: List<ConfigBlockId>

    fun getConfigBlocks(): List<ConfigBlock>

    fun onConfigChange(
        user: SlackUser,
        configBlockId: ConfigBlockId,
        value: String?
    )
}
