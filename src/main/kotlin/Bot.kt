import script.HomeScript
import script.TestScript
import servicelocator.ServiceLocator.config
import servicelocator.ServiceLocator.databaseService
import util.debug.DebugMode
import util.slackbot.SlackBot

fun main() {
    DebugMode.init(config.debugMode)

    databaseService.initialize()

    SlackBot.start {
        addScript(HomeScript())
        addScript(TestScript())
    }
}
