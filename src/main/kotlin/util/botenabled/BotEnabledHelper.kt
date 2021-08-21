package util.botenabled

import com.slack.api.bolt.context.ActionRespondUtility
import com.slack.api.bolt.context.Context
import com.slack.api.bolt.context.SayUtility
import servicelocator.ServiceLocator.databaseService

fun isBotEnabled(ctx: Context): Boolean {
    val botEnabled = databaseService.isBotEnabled()

    if (!botEnabled) {
        val message = "TODO Bot is disabled" // TODO find out what to do (if something should be done at all)
        when (ctx) {
            is SayUtility -> ctx.say(message)
            is ActionRespondUtility -> ctx.respond(message)
        }
    }

    return botEnabled
}
