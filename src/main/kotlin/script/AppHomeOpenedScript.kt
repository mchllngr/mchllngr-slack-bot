package script

import com.slack.api.app_backend.events.payload.EventsApiPayload
import com.slack.api.bolt.context.builtin.EventContext
import com.slack.api.model.event.AppHomeOpenedEvent

interface AppHomeOpenedScript : Script {

    fun onAppHomeOpenedEvent(
        event: EventsApiPayload<AppHomeOpenedEvent>,
        ctx: EventContext
    )
}
