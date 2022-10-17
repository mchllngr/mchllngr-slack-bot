// ktlint-disable filename

import script.home.HomeScript
import script.reviewlist.ReviewListScript
import servicelocator.ServiceLocator.Database
import servicelocator.ServiceLocator.config
import util.debug.DebugMode
import util.slack.bot.SlackBot

fun main() {
    DebugMode.init(config.debugMode)

    Database.dataStore.initialize()

    SlackBot.start {
        addScript(HomeScript())
        addScript(ReviewListScript())
    }
}
