package script

import com.slack.api.app_backend.events.payload.EventsApiPayload
import com.slack.api.bolt.context.builtin.EventContext
import com.slack.api.model.event.MessageEvent
import db.Test
import servicelocator.ServiceLocator.databaseService

class TestScript : MessageScript {

    override fun onMessageEvent(event: EventsApiPayload<MessageEvent>, ctx: EventContext) {
        when (event.event.text.lowercase()) {
            "get tests" -> {
                ctx.say("test entries:\n" + databaseService.getTestEntries().joinToString(separator = "\n"))
            }
            "insert test" -> {
                databaseService.insertTest(Test("Name ${event.eventId}", event.eventTime))
                ctx.say("inserted test")
            }
            "delete tests" -> {
                databaseService.deleteTests()
                ctx.say("deleted tests")
            }
        }
    }
}
