package script

import com.slack.api.app_backend.events.payload.EventsApiPayload
import com.slack.api.bolt.context.Context
import com.slack.api.bolt.context.builtin.ActionContext
import com.slack.api.bolt.context.builtin.EventContext
import com.slack.api.bolt.request.builtin.BlockActionRequest
import com.slack.api.model.block.Blocks.actions
import com.slack.api.model.block.Blocks.asBlocks
import com.slack.api.model.block.Blocks.divider
import com.slack.api.model.block.Blocks.image
import com.slack.api.model.block.Blocks.section
import com.slack.api.model.block.LayoutBlock
import com.slack.api.model.block.composition.BlockCompositions.markdownText
import com.slack.api.model.block.composition.BlockCompositions.plainText
import com.slack.api.model.block.element.BlockElements.asElements
import com.slack.api.model.block.element.BlockElements.button
import com.slack.api.model.event.AppHomeOpenedEvent
import com.slack.api.model.view.Views.view
import script.base.AppHomeOpenedScript
import script.base.BlockActionScript
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


class HomeScript : AppHomeOpenedScript, BlockActionScript {

    private val dateFormat: DateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG)

    override val blockActionIds = listOf(
        BLOCK_ACTION_ID_TEST_1,
        BLOCK_ACTION_ID_TEST_2
    )

    override fun onAppHomeOpenedEvent(
        event: EventsApiPayload<AppHomeOpenedEvent>,
        ctx: EventContext
    ) {
        if (event.event.tab != KEY_HOME) return

        ctx.updateHomeView(
            event.event.user,
            event.event.view.hash
        )
    }

    override fun onBlockActionEvent(
        blockActionId: String,
        request: BlockActionRequest,
        ctx: ActionContext
    ) {
        ctx.updateHomeView(
            request.payload.user.id,
            request.payload.view.hash
        )
    }

    private fun Context.updateHomeView(
        userId: String,
        viewHash: String
    ) {
        client().viewsPublish { requestBuilder ->
            requestBuilder
                .userId(userId)
                .hash(viewHash) // to protect against possible race conditions
                .view(
                    view { view ->
                        view
                            .type(KEY_HOME)
                            .blocks(createHomeBlocks(userId))
                    }
                )
        }
    }

    private fun createHomeBlocks(
        userId: String
    ): List<LayoutBlock> {
        val now: ZonedDateTime = ZonedDateTime.now()

        return asBlocks(
            section { section ->
                section.text(markdownText { mt -> mt.text(":wave: Hallo <@$userId>! (Zuletzt aktualisiert: ${now.format(dateFormat)})") })
            },
            image { img ->
                img
                    .imageUrl("https://upload.wikimedia.org/wikipedia/commons/0/0e/Tree_example_VIS.jpg")
                    .altText("example image")
            },
            divider(),
            section { section -> section.text(markdownText("*Please select a Button:*")) },
            divider(),
            actions { actions ->
                actions.elements(
                    asElements(
                        button { b ->
                            b
                                .text(plainText { pt -> pt.emoji(true).text("Button 1") })
                                .value("1")
                                .actionId(BLOCK_ACTION_ID_TEST_1)
                        },
                        button { b ->
                            b
                                .text(plainText { pt -> pt.emoji(true).text("Button 2") })
                                .value("2")
                                .actionId(BLOCK_ACTION_ID_TEST_2)
                        }
                    )
                )
            }
        )
    }

    companion object {

        private const val KEY_HOME = "home"
        private const val BLOCK_ACTION_ID_TEST_1 = "test_1"
        private const val BLOCK_ACTION_ID_TEST_2 = "test_2"
    }
}
