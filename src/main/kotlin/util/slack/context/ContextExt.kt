package util.slack.context

import com.slack.api.app_backend.events.payload.EventsApiPayload
import com.slack.api.bolt.context.Context
import com.slack.api.bolt.request.builtin.BlockActionRequest
import com.slack.api.bolt.request.builtin.SlashCommandRequest
import com.slack.api.bolt.request.builtin.ViewSubmissionRequest
import com.slack.api.methods.response.chat.ChatPostMessageResponse
import com.slack.api.methods.response.reactions.ReactionsAddResponse
import com.slack.api.methods.response.reactions.ReactionsRemoveResponse
import com.slack.api.methods.response.users.UsersInfoResponse
import com.slack.api.model.Conversation
import com.slack.api.model.block.LayoutBlock
import com.slack.api.model.event.MessageEvent
import model.user.UserId
import servicelocator.ServiceLocator.config
import util.slack.user.SlackUser

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
    blocks: List<LayoutBlock>
): ChatPostMessageResponse = client().chatPostMessage { requestBuilder ->
    requestBuilder
        .token(config.token.bot)
        .channel(channelName)
        .blocks(blocks)
}

fun Context.addReactionToMessage(
    channelId: String,
    messageTimestamp: String,
    emojiName: String
): ReactionsAddResponse = client().reactionsAdd {
    it.token(config.token.bot)
    it.channel(channelId)
    it.name(emojiName)
    it.timestamp(messageTimestamp)
}

fun Context.removeReactionFromMessage(
    channelId: String,
    messageTimestamp: String,
    emojiName: String
): ReactionsRemoveResponse = client().reactionsRemove {
    it.token(config.token.bot)
    it.channel(channelId)
    it.name(emojiName)
    it.timestamp(messageTimestamp)
}

fun Context.getUser(userId: UserId): SlackUser? {
    val usersInfo: UsersInfoResponse = client().usersInfo {
        it.token(config.token.bot)
        it.user(userId.id)
        it.includeLocale(true)
    }
    return if (usersInfo.isOk) usersInfo.user else null
}

fun Context.getUser(request: SlashCommandRequest) = getUser(UserId(request.payload.userId))

fun Context.getUser(request: BlockActionRequest) = getUser(UserId(request.payload.user.id))

fun Context.getUser(request: ViewSubmissionRequest) = getUser(UserId(request.payload.user.id))

fun Context.getUser(event: EventsApiPayload<MessageEvent>) = getUser(UserId(event.event.user))

fun Context.userExists(userId: UserId) = getUser(userId) != null
