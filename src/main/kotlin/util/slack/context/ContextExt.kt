package util.slack.context

import com.slack.api.bolt.context.Context
import com.slack.api.bolt.request.builtin.BlockActionRequest
import com.slack.api.methods.response.chat.ChatPostMessageResponse
import com.slack.api.methods.response.users.UsersInfoResponse
import com.slack.api.model.Conversation
import com.slack.api.model.User
import com.slack.api.model.block.LayoutBlock
import model.user.UserId
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

fun Context.getUser(userId: UserId): User? {
    val usersInfo: UsersInfoResponse = client().usersInfo {
        it.token(config.token.bot)
        it.user(userId.id)
    }
    return if (usersInfo.isOk) usersInfo.user else null
}

fun Context.getUser(request: BlockActionRequest) = getUser(UserId(request.payload.user.id))

fun Context.userExists(userId: UserId) = getUser(userId) != null
