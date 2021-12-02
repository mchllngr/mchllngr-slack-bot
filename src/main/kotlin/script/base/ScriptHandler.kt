package script.base

import com.slack.api.bolt.App
import com.slack.api.bolt.handler.builtin.BlockActionHandler
import com.slack.api.model.event.AppHomeOpenedEvent
import com.slack.api.model.event.MessageEvent
import model.blockaction.BlockActionId
import model.script.ScriptId
import model.user.UserId
import script.home.HomeScript
import service.admin.AdminService
import util.slack.context.getUser
import util.slack.user.isBotAdmin

class ScriptHandler(
    private val adminService: AdminService
) {

    private val scripts = mutableMapOf<ScriptId, Script>()

    private var registered = false

    fun addScript(script: Script) {
        if (registered) throw IllegalStateException("already registered")
        if (script.id.id.isBlank()) throw IllegalStateException("a ScriptId must not be blank")
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

            if (!userIsBotAdmin && !adminService.isBotEnabled()) return@event ctx.ack()

            appHomeOpenedScripts
                .filter { it.id == HomeScript.ID && userIsBotAdmin || adminService.isScriptEnabled(it.id) } // TODO improve performance: check enabled-state with a list of ids so that only one db-request is needed ("get all enabled=true from db")
                .forEach { it.onAppHomeOpenedEvent(event, ctx) }

            ctx.ack()
        }
    }

    private fun App.registerMessageScripts() {
        val messageScripts = scripts.values.filterIsInstance<MessageScript>()

        event(MessageEvent::class.java) { event, ctx ->
            if (!adminService.isBotEnabled()) return@event ctx.ack()

            messageScripts
                .filter { adminService.isScriptEnabled(it.id) } // TODO improve performance: check enabled-state with a list of ids so that only one db-request is needed ("get all enabled=true from db")
                .forEach { it.onMessageEvent(event, ctx) }

            ctx.ack()
        }
    }

    private fun App.registerBlockActionScripts() {
        val blockActionScripts = scripts.values.filterIsInstance<BlockActionScript>()
        val blockActionIdToScript = blockActionScripts.toMapOfBlockActionIdToScripts()

        blockActionIdToScript
            .forEach { (blockActionId, scripts) ->
                when (blockActionId) {
                    is BlockActionId.User -> registerUserBlockActionScripts(blockActionId, scripts)
                    is BlockActionId.Admin -> registerAdminBlockActionScripts(blockActionId, scripts)
                }
            }
    }

    private fun List<BlockActionScript>.toMapOfBlockActionIdToScripts(): Map<BlockActionId, List<BlockActionScript>> {
        val blockActionIdToScript = mutableMapOf<BlockActionId, MutableList<BlockActionScript>>()
        forEach { script ->
            script.blockActionIds.forEach { id ->
                val scriptsForId = blockActionIdToScript[id]
                if (scriptsForId == null) blockActionIdToScript[id] = mutableListOf(script)
                else scriptsForId += script
            }
        }
        return blockActionIdToScript
    }

    private fun App.registerUserBlockActionScripts(
        blockActionId: BlockActionId.User,
        scripts: List<BlockActionScript>
    ) {
        val handler = BlockActionHandler { request, ctx ->
            if (!adminService.isBotEnabled()) return@BlockActionHandler ctx.ack()

            scripts
                .filter { adminService.isScriptEnabled(it.id) } // TODO improve performance: check enabled-state with a list of ids so that only one db-request is needed ("get all enabled=true from db")
                .forEach { it.onBlockActionEvent(blockActionId, request, ctx) }

            ctx.ack()
        }

        when (blockActionId) {
            is BlockActionId.User.Str -> blockAction(blockActionId.id, handler)
            is BlockActionId.User.Regex -> blockAction(blockActionId.idRegex.toPattern(), handler)
        }
    }

    private fun App.registerAdminBlockActionScripts(
        blockActionId: BlockActionId.Admin,
        scripts: List<BlockActionScript>
    ) {
        val handler = BlockActionHandler { request, ctx ->
            if (!ctx.getUser(request).isBotAdmin) return@BlockActionHandler ctx.ack()

            scripts.forEach { it.onBlockActionEvent(blockActionId, request, ctx) }

            ctx.ack()
        }

        when (blockActionId) {
            is BlockActionId.Admin.Str -> blockAction(blockActionId.id, handler)
            is BlockActionId.Admin.Regex -> blockAction(blockActionId.idRegex.toPattern(), handler)
        }
    }

    companion object {

        fun create(adminService: AdminService) = ScriptHandler(adminService)
    }
}
