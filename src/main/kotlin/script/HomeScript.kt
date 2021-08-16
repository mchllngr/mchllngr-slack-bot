package script

import com.slack.api.app_backend.events.payload.EventsApiPayload
import com.slack.api.bolt.context.builtin.EventContext
import com.slack.api.model.block.Blocks.asBlocks
import com.slack.api.model.block.Blocks.image
import com.slack.api.model.block.Blocks.section
import com.slack.api.model.block.SectionBlock.SectionBlockBuilder
import com.slack.api.model.block.composition.BlockCompositions.markdownText
import com.slack.api.model.event.AppHomeOpenedEvent
import com.slack.api.model.view.Views.view
import java.time.ZonedDateTime


class HomeScript : AppHomeOpenedScript {

    override fun onAppHomeOpenedEvent(
        event: EventsApiPayload<AppHomeOpenedEvent>,
        ctx: EventContext
    ) {
        if (event.event.tab != "home") return

        val now = ZonedDateTime.now()
        val appHomeView = view { view ->
            view
                .type("home")
                .blocks(asBlocks(
                    section { section: SectionBlockBuilder -> section.text(markdownText { mt -> mt.text(":wave: Hello, App Home! (Last updated: $now)") }) },
                    image { img -> img.imageUrl("https://www.example.com/foo.png") }
                ))
        }
        // Update the App Home for the given user
        val res = ctx.client().viewsPublish {
            it
                .userId(event.event.user)
//                .hash(event.event.view.hash) // To protect against possible race conditions
                .view(appHomeView)
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
}
