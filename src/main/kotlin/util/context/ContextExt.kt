package util.context

import com.slack.api.bolt.context.Context
import com.slack.api.methods.response.chat.ChatPostMessageResponse
import com.slack.api.methods.response.users.UsersInfoResponse
import com.slack.api.model.Conversation
import com.slack.api.model.User
import com.slack.api.model.block.LayoutBlock
import servicelocator.ServiceLocator.config

fun Context.getConversation(name: String): Conversation? {
    try {
        return client()
            .conversationsList { it.token(config.token.bot) }
            .channels
            .find { it.name == name }
    } catch (e: Throwable) {
        logger.error("error: {}", e.message, e)
    }
    return null
}

fun Context.postChatMessageInChannel(
    channelName: String,
    blocksBuilder: () -> List<LayoutBlock>
): ChatPostMessageResponse = slack.methods(config.token.bot).chatPostMessage { requestBuilder ->
    requestBuilder
        .channel(channelName)
        .blocks(blocksBuilder())
}

fun Context.getUser(userId: String): User? {
    val usersInfo: UsersInfoResponse = client().usersInfo {
        it.token(config.token.bot)
        it.user(userId)
    }
    return if (usersInfo.isOk) usersInfo.user else null
}
