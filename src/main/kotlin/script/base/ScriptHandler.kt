package script.base

import com.slack.api.bolt.App
import com.slack.api.model.event.AppHomeOpenedEvent
import com.slack.api.model.event.MessageEvent
import model.script.ScriptId
import model.user.UserId
import script.home.HomeScript
import script.home.block.AdminBlocks
import service.admin.AdminService
import util.bot.isBotEnabled
import util.slack.context.getUser
import util.slack.user.isBotAdmin

class ScriptHandler(
    private val adminService: AdminService
) {

    private val scripts = mutableMapOf<ScriptId, Script>()

    private var registered = false

    fun addScript(script: Script) {
        if (registered) throw IllegalStateException("already registered")
        if (script.id in scripts) throw IllegalStateException("a Script with the id '${script.id}' was already added")

        scripts += script.id to script
    }

    fun registerScripts(app: App) {
        if (registered) throw IllegalStateException("already registered")
        registered = true

        app.apply {
            registerAppHomeOpenedScripts()
            registerMessageScripts()
            registerBlockActionScripts()
        }

        scripts.values.forEach { script ->
            adminService.insertScript(script.id)
        }
    }

    fun getScriptIds() = scripts.keys.toSet()

    private fun App.registerAppHomeOpenedScripts() {
        val appHomeOpenedScripts = scripts.values.filterIsInstance<AppHomeOpenedScript>()

        event(AppHomeOpenedEvent::class.java) { event, ctx ->
            // TODO find some other way to always show admins the home-tab in a more general concept
            //      -> some kind of "admin only" script?

            val userIsBotAdmin = ctx.getUser(UserId(event.event.user)).isBotAdmin

            if (!userIsBotAdmin && !isBotEnabled(ctx)) return@event ctx.ack()

            appHomeOpenedScripts
                .filter { it.id == HomeScript.ID && userIsBotAdmin || adminService.isScriptEnabled(it.id) } // TODO improve performance: check enabled-state with a list of ids so that only one db-request is needed ("get all enabled=true from db")
                .forEach { it.onAppHomeOpenedEvent(event, ctx) }

            ctx.ack()
        }
    }

    private fun App.registerMessageScripts() {
        val messageScripts = scripts.values.filterIsInstance<MessageScript>()

        event(MessageEvent::class.java) { event, ctx ->
            if (!isBotEnabled(ctx)) return@event ctx.ack()

            messageScripts
                .filter { adminService.isScriptEnabled(it.id) } // TODO improve performance: check enabled-state with a list of ids so that only one db-request is needed ("get all enabled=true from db")
                .forEach { it.onMessageEvent(event, ctx) }

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
                    // TODO find some other way to handle admin actions in a more general concept
                    //      -> some kind of "admin only" action?

                    val isAdminAction = blockActionId == AdminBlocks.ACTION_BOT_ENABLED_SELECTED
                            || blockActionId == AdminBlocks.ACTION_SCRIPT_ENABLED_SELECTED

                    if (!isAdminAction && !isBotEnabled(ctx)) return@blockAction ctx.ack()

                    scripts
                        .filter { isAdminAction || adminService.isScriptEnabled(it.id) } // TODO improve performance: check enabled-state with a list of ids so that only one db-request is needed ("get all enabled=true from db")
                        .forEach { it.onBlockActionEvent(blockActionId, request, ctx) }

                    ctx.ack()
                }
            }
    }

    companion object {

        fun create(adminService: AdminService) = ScriptHandler(adminService)
    }
}
