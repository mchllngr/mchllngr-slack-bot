import com.slack.api.bolt.App
import com.slack.api.bolt.AppConfig
import com.slack.api.bolt.jetty.SlackAppServer
import com.slack.api.model.event.AppMentionEvent

fun main() {
    // https://gist.github.com/seratch/6ab9139d15ad33c9b1e149327b5f14fa

    System.setProperty("org.slf4j.simpleLogger.log.com.slack.api", "debug")
    System.setProperty("org.slf4j.simpleLogger.log.notion.api", "debug")

    val app = App(
        // Bot token scopes: users:read, team:read, channels:read/groups:read/mpim:read/im:read
        // User token scopes: stars:read
        AppConfig.builder()
            .singleTeamBotToken(System.getenv("SLACK_BOT_TOKEN"))
            .signingSecret(System.getenv("SLACK_SIGNING_SECRET"))
            .build()
    )

    app.event(AppMentionEvent::class.java) { event, ctx ->
        ctx.say("<@${event.event.user}> What's up?")
        ctx.ack()
    }

    val server = SlackAppServer(app)
    server.start() // http://localhost:3000/slack/events
}
