package extension

import com.slack.api.bolt.context.Context
import com.slack.api.model.Conversation
import servicelocator.ServiceLocator.config

fun Context.getConversation(name: String): Conversation? {
    try {
        return client()
            .conversationsList { it.token(config.botToken) }
            .channels
            .find { it.name == name }
    } catch (e: Throwable) {
        logger.error("error: {}", e.message, e)
    }
    return null
}
