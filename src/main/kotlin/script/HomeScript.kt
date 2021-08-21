package script

import com.slack.api.app_backend.events.payload.EventsApiPayload
import com.slack.api.bolt.context.builtin.EventContext
import com.slack.api.model.block.Blocks.asBlocks
import com.slack.api.model.block.Blocks.image
import com.slack.api.model.block.Blocks.section
import com.slack.api.model.block.composition.BlockCompositions.markdownText
import com.slack.api.model.event.AppHomeOpenedEvent
import com.slack.api.model.view.View
import com.slack.api.model.view.Views.view
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


class HomeScript : AppHomeOpenedScript {

    override fun onAppHomeOpenedEvent(
        event: EventsApiPayload<AppHomeOpenedEvent>,
        ctx: EventContext
    ) {
        if (event.event.tab != KEY_HOME) return

        ctx.client().viewsPublish {
            it
                .userId(event.event.user)
                .hash(event.event.view.hash) // to protect against possible race conditions
                .view(getHomeView(event))
        }

//        ctx.say(asBlocks(
//            Blocks.section { it.blockId("foo").text(markdownText("<@${event.event.user}> **What's up?**")) },
//            Blocks.section { it.blockId("conversation").text(plainText("Found conversation for name 'test': ${ctx.getConversation("test")}")) }
//        ))
//
//        ctx.say {
//            it.channel("test")
//                .blocks(
//                    asBlocks(
//                        Blocks.section { it.blockId("test").text(plainText("Typed in 'test'")) }
//                    )
//                )
//                .text("Typed in 'test'")
//        }
    }

    private fun getHomeView(event: EventsApiPayload<AppHomeOpenedEvent>): View {
        val now: ZonedDateTime = ZonedDateTime.now()
        val dateFormat: DateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG)

        return view { view ->
            view
                .type(KEY_HOME)
                .blocks(
                    asBlocks(
                        section { section ->
                            section
                                .text(markdownText { mt ->
                                    mt.text(":wave: Hallo <@${event.event.user}>! (Zuletzt aktualisiert: ${now.format(dateFormat)})")
                                })
                        },
                        image { img ->
                            img
                                .imageUrl("https://upload.wikimedia.org/wikipedia/commons/0/0e/Tree_example_VIS.jpg")
                                .altText("example image")
                        }
                    )
                )
        }
    }

    companion object {

        private const val KEY_HOME = "home"
    }
}
