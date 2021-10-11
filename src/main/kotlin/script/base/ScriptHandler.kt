package script.base

import com.slack.api.bolt.App
import com.slack.api.model.event.AppHomeOpenedEvent
import com.slack.api.model.event.MessageEvent
import util.botenabled.isBotEnabled

class ScriptHandler {

    private val scripts = mutableListOf<Script>()

    private var registered = false

    fun addScript(script: Script) {
        if (registered) throw IllegalStateException("already registered")

        if (script !in scripts) scripts += script
    }

    fun registerScripts(app: App) {
        if (registered) throw IllegalStateException("already registered")
        registered = true

        app.apply {
            registerAppHomeOpenedScripts()
            registerMessageScripts()
            registerBlockActionScripts()
        }
    }

    private fun App.registerAppHomeOpenedScripts() {
        val appHomeOpenedScripts = scripts.filterIsInstance<AppHomeOpenedScript>()

        event(AppHomeOpenedEvent::class.java) { event, ctx ->
            if (!isBotEnabled(ctx)) return@event ctx.ack()

            appHomeOpenedScripts.forEach { it.onAppHomeOpenedEvent(event, ctx) }

            ctx.ack()
        }
    }

    private fun App.registerMessageScripts() {
        val messageScripts = scripts.filterIsInstance<MessageScript>()

        event(MessageEvent::class.java) { event, ctx ->
            if (!isBotEnabled(ctx)) return@event ctx.ack()

            messageScripts.forEach { it.onMessageEvent(event, ctx) }

            ctx.ack()
        }
    }

    private fun App.registerBlockActionScripts() {
        val blockActionScripts = scripts.filterIsInstance<BlockActionScript>()

        val blockActionIdToScript = mutableMapOf<String, MutableList<BlockActionScript>>()
        blockActionScripts.forEach { script ->
            script.blockActionIds.forEach { id ->
                val scriptsForId = blockActionIdToScript[id]
                if (scriptsForId == null) blockActionIdToScript[id] = mutableListOf(script)
                else scriptsForId += script
            }
        }

        blockActionIdToScript
            .forEach { (blockActionId, scripts) ->
                blockAction(blockActionId) { request, ctx ->
                    if (!isBotEnabled(ctx)) return@blockAction ctx.ack()

                    scripts.forEach { it.onBlockActionEvent(blockActionId, request, ctx) }

                    ctx.ack()
                }
            }
    }

    companion object {

        fun create() = ScriptHandler()
    }
}
