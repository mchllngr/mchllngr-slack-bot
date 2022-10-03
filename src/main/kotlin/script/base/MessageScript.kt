package script.base

import com.slack.api.app_backend.events.payload.EventsApiPayload
import com.slack.api.bolt.context.builtin.EventContext
import com.slack.api.model.event.MessageEvent

interface MessageScript : Script {

    // #31 allow setting a String or Regex to filter wanted messages

    fun onMessageEvent(
        event: EventsApiPayload<MessageEvent>,
        ctx: EventContext
    )
}
