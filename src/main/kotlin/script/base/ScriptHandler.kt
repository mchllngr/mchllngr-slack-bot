package script.base

import com.slack.api.bolt.App
import com.slack.api.model.event.AppHomeOpenedEvent
import com.slack.api.model.event.MessageEvent
import script.home.block.AdminBlocks
import util.botenabled.isBotEnabled
import util.context.getUser
import util.slack.user.isBotAdmin

class ScriptHandler {

    private val scripts = mutableMapOf<String, Script>()

    private var registered = false

    fun addScript(script: Script) {
        if (registered) throw IllegalStateException("already registered")
        if (script.name in scripts) throw IllegalStateException("a Script with the name '${script.name}' was already added")

        scripts += script.name to script
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
        val appHomeOpenedScripts = scripts.values.filterIsInstance<AppHomeOpenedScript>()

        event(AppHomeOpenedEvent::class.java) { event, ctx ->
            // TODO find some other way to always show admins the home-tab
            if (!ctx.getUser(event.event.user).isBotAdmin && !isBotEnabled(ctx)) return@event ctx.ack()

            appHomeOpenedScripts.forEach { it.onAppHomeOpenedEvent(event, ctx) }

            ctx.ack()
        }
    }

    private fun App.registerMessageScripts() {
        val messageScripts = scripts.values.filterIsInstance<MessageScript>()

        event(MessageEvent::class.java) { event, ctx ->
            if (!isBotEnabled(ctx)) return@event ctx.ack()

            messageScripts.forEach { it.onMessageEvent(event, ctx) }

            ctx.ack()
        }
    }

    private fun App.registerBlockActionScripts() {
        val blockActionScripts = scripts.values.filterIsInstance<BlockActionScript>()

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
                    // TODO find some other way to handle admin actions
                    if (blockActionId != AdminBlocks.ACTION_BOT_ENABLED_SELECTED && !isBotEnabled(ctx)) return@blockAction ctx.ack()

                    scripts.forEach { it.onBlockActionEvent(blockActionId, request, ctx) }

                    ctx.ack()
                }
            }
    }

    companion object {

        fun create() = ScriptHandler()
    }
}
