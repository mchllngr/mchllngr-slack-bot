import script.registerTestScript
import servicelocator.ServiceLocator.config
import util.debug.DebugMode
import util.slackbot.SlackBot

fun main() {
    DebugMode.init(config.debugMode)

    SlackBot.start {
        registerTestScript()
    }
}
