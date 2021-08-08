import script.registerTestScript
import servicelocator.ServiceLocator.config
import servicelocator.ServiceLocator.databaseService
import util.debug.DebugMode
import util.slackbot.SlackBot

fun main() {
    DebugMode.init(config.debugMode)

    databaseService.initialize()

    SlackBot.start {
        registerTestScript()
    }
}
