import script.BirthdayScript
import script.HomeScript
import script.TestScript
// ktlint-disable filename

import script.home.HomeScript
import script.reviewlist.ReviewListScript
import servicelocator.ServiceLocator.config
import servicelocator.ServiceLocator.dataStore
import util.debug.DebugMode
import util.slack.bot.SlackBot

fun main() {
    DebugMode.init(config.debugMode)

    dataStore.initialize()

    SlackBot.start {
        addScript(HomeScript())
        addScript(TestScript())
        addScript(BirthdayScript())
        addScript(ReviewListScript())
    }
}
