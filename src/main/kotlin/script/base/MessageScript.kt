package script.base

import com.slack.api.app_backend.events.payload.EventsApiPayload
import com.slack.api.bolt.context.builtin.EventContext
import com.slack.api.model.event.MessageEvent
import model.message.MessageId

interface MessageScript : Script {

    val messageIds: List<MessageId>

    fun onMessageEvent(
        messageId: MessageId,
        event: EventsApiPayload<MessageEvent>,
        ctx: EventContext
    )
}
