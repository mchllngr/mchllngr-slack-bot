import com.slack.api.bolt.App
import com.slack.api.bolt.AppConfig
import com.slack.api.bolt.socket_mode.SocketModeApp
import com.slack.api.model.block.Blocks.asBlocks
import com.slack.api.model.block.Blocks.section
import com.slack.api.model.block.composition.BlockCompositions.markdownText
import com.slack.api.model.block.composition.BlockCompositions.plainText
import com.slack.api.model.event.AppMentionEvent
import com.slack.api.model.event.HelloEvent

fun main() {
    // https://api.slack.com/apps/A023Q0JNSCX
    // https://github.com/slackapi/java-slack-sdk/blob/master/bolt-kotlin-examples/src/main/kotlin/examples/reply/app.kt
    // https://gist.github.com/seratch/6ab9139d15ad33c9b1e149327b5f14fa
    // https://slack.dev/java-slack-sdk/guides/bolt-basics
    // https://slack.dev/java-slack-sdk/guides/getting-started-with-bolt
    // https://slack.dev/java-slack-sdk/guides/getting-started-with-bolt-socket-mode

    System.setProperty("org.slf4j.simpleLogger.log.com.slack.api", "debug")
    System.setProperty("org.slf4j.simpleLogger.log.notion.api", "debug")
    System.setProperty("SLACK_APP_LOCAL_DEBUG", "debug")

    val app = App(
        AppConfig.builder()
            .singleTeamBotToken(System.getenv("SLACK_BOT_TOKEN"))
            .signingSecret(System.getenv("SLACK_SIGNING_SECRET"))
            .build()
    )

    app.event(HelloEvent::class.java) { event, ctx ->
        ctx.logger.debug("hello $event")
        ctx.ack()
    }

    app.event(AppMentionEvent::class.java) { event, ctx ->
        ctx.say("<@${event.event.user}> What's up? 1")
        ctx.say(asBlocks(
            section { it.blockId("foo").text(markdownText("<@${event.event.user}> **What's up?** 2")) },
            section { it.blockId("foo").text(plainText("<@${event.event.user}> What's up? 3")) }
        ))
        ctx.ack()
    }

    app.message("test 123") { event , ctx  ->
        ctx.say("<@${event.event.user}> What's up? message")
        ctx.ack()
    }

    SocketModeApp(app).start()
}
