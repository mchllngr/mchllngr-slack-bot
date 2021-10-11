import script.HomeScript
import script.TestScript
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
    }
}
