package util.botenabled

import com.slack.api.bolt.context.SayUtility
import servicelocator.ServiceLocator.databaseService

fun isBotEnabled(ctx: SayUtility): Boolean {
    val botEnabled = databaseService.isBotEnabled()

    if (!botEnabled) ctx.say("TODO Bot is disabled")

    return botEnabled
}
