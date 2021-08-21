package script.base

import com.slack.api.app_backend.events.payload.EventsApiPayload
import com.slack.api.bolt.context.builtin.EventContext
import com.slack.api.model.event.MessageEvent

interface MessageScript : Script {

    fun onMessageEvent(
        event: EventsApiPayload<MessageEvent>,
        ctx: EventContext
    )
}
