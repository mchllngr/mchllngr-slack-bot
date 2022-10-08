package util.slack.context

import com.slack.api.bolt.context.Context
import com.slack.api.model.block.LayoutBlock
import com.slack.api.model.view.ViewClose
import com.slack.api.model.view.ViewSubmit
import com.slack.api.model.view.ViewTitle
import com.slack.api.model.view.Views.view
import servicelocator.ServiceLocator.config
import util.logger.getLogger

private const val VIEW_TYPE_MODAL = "modal"
private const val VIEW_TYPE_PLAIN_TEXT = "plain_text"

fun Context.openModal(
    triggerId: String,
    callbackId: String,
    title: ModalText,
    blocks: List<LayoutBlock>,
    submit: ModalText? = null,
    close: ModalText? = null
) {
    client().viewsOpen { requestBuilder ->
        requestBuilder
            .token(config.token.bot)
            .triggerId(triggerId)
            .view(
                view { view ->
                    view
                        .callbackId(callbackId)
                        .type(VIEW_TYPE_MODAL)
                        .title(
                            ViewTitle(
                                VIEW_TYPE_PLAIN_TEXT,
                                title.sanitizedText,
                                title.emoji
                            )
                        )
                        .blocks(blocks)
                        .apply {
                            submit?.let { text ->
                                submit(
                                    ViewSubmit(
                                        VIEW_TYPE_PLAIN_TEXT,
                                        text.sanitizedText,
                                        text.emoji
                                    )
                                )
                            }

                            close?.let { text ->
                                close(
                                    ViewClose(
                                        VIEW_TYPE_PLAIN_TEXT,
                                        text.sanitizedText,
                                        text.emoji
                                    )
                                )
                            }
                        }
                }
            )
    }
}

data class ModalText(
    private val text: String,
    val emoji: Boolean = false
) {

    val sanitizedText = text.take(24)

    init {
        // https://api.slack.com/reference/surfaces/views
        if (text.length > 24) getLogger().warn(
            """
            |'text' must have a max length of 24 characters, the text will be cut off
            |    provided text: $text
            |    sanitized text: $sanitizedText
            """.trimMargin()
        )
    }
}
