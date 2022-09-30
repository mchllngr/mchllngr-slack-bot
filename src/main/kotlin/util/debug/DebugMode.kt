package util.debug

object DebugMode {

    fun init(debugMode: Boolean) {
        if (!debugMode) return

        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug")
        System.setProperty("org.slf4j.simpleLogger.log.com.slack.api", "debug")
        System.setProperty("org.slf4j.simpleLogger.log.notion.api", "debug")
        System.setProperty("SLACK_APP_LOCAL_DEBUG", "debug")
    }
}
