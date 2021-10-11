package script

import com.slack.api.app_backend.events.payload.EventsApiPayload
import com.slack.api.bolt.context.builtin.EventContext
import com.slack.api.model.event.MessageEvent
import db.Test
import script.base.MessageScript
import servicelocator.ServiceLocator

class TestScript : MessageScript {

    private val testService by lazy { ServiceLocator.testService }

    override fun onMessageEvent(event: EventsApiPayload<MessageEvent>, ctx: EventContext) {
        when (event.event.text.lowercase()) {
            "get tests" -> {
                ctx.say("test entries:\n" + testService.getTestEntries().joinToString(separator = "\n"))
            }
            "insert test" -> {
                testService.insertTest(Test("Name ${event.eventId}", event.eventTime))
                ctx.say("inserted test")
            }
            "delete tests" -> {
                testService.deleteTests()
                ctx.say("deleted tests")
            }
        }
    }
}
