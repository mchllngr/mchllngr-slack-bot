package script

import com.slack.api.bolt.App
import com.slack.api.model.block.Blocks
import com.slack.api.model.block.composition.BlockCompositions
import com.slack.api.model.event.AppMentionEvent
import extension.getConversation

fun App.registerTestScript() {
    event(AppMentionEvent::class.java) { event, ctx ->
        ctx.say(Blocks.asBlocks(
            Blocks.section { it.blockId("foo").text(BlockCompositions.markdownText("<@${event.event.user}> **What's up?**")) },
            Blocks.section { it.blockId("conversation").text(BlockCompositions.plainText("Found conversation for name 'test': ${ctx.getConversation("test")}")) }
        ))

        ctx.say {
            it.channel("test")
                .blocks(
                    Blocks.asBlocks(
                        Blocks.section { it.blockId("test").text(BlockCompositions.plainText("Typed in 'test'")) }
                    )
                )
                .text("Typed in 'test'")
        }

        ctx.ack()
    }
}
