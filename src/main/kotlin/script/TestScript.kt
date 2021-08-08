package script

import com.slack.api.bolt.App
import com.slack.api.model.event.MessageEvent
import db.Test
import servicelocator.ServiceLocator.databaseService
import util.botenabled.isBotEnabled


fun App.registerTestScript() {
//    event(AppMentionEvent::class.java) { event, ctx ->
//        ctx.say(Blocks.asBlocks(
//            Blocks.section { it.blockId("foo").text(BlockCompositions.markdownText("<@${event.event.user}> **What's up?**")) },
//            Blocks.section { it.blockId("conversation").text(BlockCompositions.plainText("Found conversation for name 'test': ${ctx.getConversation("test")}")) }
//        ))
//
//        ctx.say {
//            it.channel("test")
//                .blocks(
//                    Blocks.asBlocks(
//                        Blocks.section { it.blockId("test").text(BlockCompositions.plainText("Typed in 'test'")) }
//                    )
//                )
//                .text("Typed in 'test'")
//        }
//
//        ctx.ack()
//    }

    event(MessageEvent::class.java) { event, ctx ->
        if (!isBotEnabled(ctx)) return@event ctx.ack()

        when (event.event.text.lowercase()) {
            "get tests" -> {
                ctx.say("test entries:\n" + databaseService.getTestEntries().joinToString(separator = "\n"))
            }
            "insert test" -> {
                databaseService.insertTest(Test("Name ${event.eventId}", event.eventTime))
                ctx.say("inserted test")
            }
            "delete tests" -> {
                databaseService.deleteTests()
                ctx.say("deleted tests")
            }
        }

        ctx.ack()
    }
}
