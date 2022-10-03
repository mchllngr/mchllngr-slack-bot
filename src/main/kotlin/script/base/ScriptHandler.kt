package script.base

import com.slack.api.bolt.App
import com.slack.api.bolt.handler.builtin.BlockActionHandler
import com.slack.api.bolt.handler.builtin.SlashCommandHandler
import com.slack.api.bolt.handler.builtin.ViewSubmissionHandler
import com.slack.api.model.event.AppHomeOpenedEvent
import com.slack.api.model.event.MessageEvent
import model.blockaction.BlockActionId
import model.command.CommandId
import model.script.ScriptId
import model.user.UserId
import model.view.submission.ViewSubmissionId
import repository.admin.AdminRepository
import script.home.HomeScript
import util.slack.context.getUser
import util.slack.user.isBotAdmin

class ScriptHandler(
    private val adminRepo: AdminRepository
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
            registerCommandScripts()
            registerBlockActionScripts()
            registerViewSubmissionScripts()
        }

        scripts.values.forEach { script ->
            adminRepo.insertScript(script.id)
        }
    }

    fun getScripts(): Map<ScriptId, Script> = scripts.toMap()

    fun getScriptIds() = scripts.keys.toSet()

    // region AppHomeOpenedScripts

    private fun App.registerAppHomeOpenedScripts() {
        val appHomeOpenedScripts = scripts.values.filterIsInstance<AppHomeOpenedScript>()

        event(AppHomeOpenedEvent::class.java) { event, ctx ->
            // #13 find some other way to always show admins the home-tab in a more general concept

            val userIsBotAdmin = ctx.getUser(UserId(event.event.user)).isBotAdmin

            if (!userIsBotAdmin && !adminRepo.isBotEnabled()) return@event ctx.ack()

            appHomeOpenedScripts
                .filter { it.id == HomeScript.ID && userIsBotAdmin || adminRepo.isScriptEnabled(it.id) } // #14 improve performance when checking if scripts are enabled
                .forEach { it.onAppHomeOpenedEvent(event, ctx) }

            ctx.ack()
        }
    }

    // endregion

    // region MessageScripts

    private fun App.registerMessageScripts() {
        val messageScripts = scripts.values.filterIsInstance<MessageScript>()

        event(MessageEvent::class.java) { event, ctx ->
            if (!adminRepo.isBotEnabled()) return@event ctx.ack()

            messageScripts
                .filter { adminRepo.isScriptEnabled(it.id) } // #14 improve performance when checking if scripts are enabled
                .forEach { it.onMessageEvent(event, ctx) }

            ctx.ack()
        }
    }

    // endregion

    // region CommandScripts

    private fun App.registerCommandScripts() {
        val commandScripts = scripts.values.filterIsInstance<CommandScript>()
        val commandIdToScript = commandScripts.toMapOfCommandIdToScripts()

        commandIdToScript
            .forEach { (commandId, scripts) ->
                when (commandId) {
                    is CommandId.User -> registerUserCommandScripts(commandId, scripts)
                    is CommandId.Admin -> registerAdminCommandScripts(commandId, scripts)
                }
            }
    }

    private fun List<CommandScript>.toMapOfCommandIdToScripts(): Map<CommandId, List<CommandScript>> {
        val commandIdToScript = mutableMapOf<CommandId, MutableList<CommandScript>>()
        forEach { script ->
            script.commandIds.forEach { id ->
                val scriptsForId = commandIdToScript[id]
                if (scriptsForId == null) {
                    commandIdToScript[id] = mutableListOf(script)
                } else {
                    scriptsForId += script
                }
            }
        }
        return commandIdToScript
    }

    private fun App.registerUserCommandScripts(
        commandId: CommandId.User,
        scripts: List<CommandScript>
    ) {
        val handler = SlashCommandHandler { request, ctx ->
            if (!adminRepo.isBotEnabled()) return@SlashCommandHandler ctx.ack()

            scripts
                .filter { adminRepo.isScriptEnabled(it.id) } // #14 improve performance when checking if scripts are enabled
                .forEach { it.onCommandEvent(commandId, request, ctx) }

            ctx.ack()
        }

        when (commandId) {
            is CommandId.User.Str -> command(commandId.id, handler)
            is CommandId.User.Regex -> command(commandId.idRegex.toPattern(), handler)
        }
    }

    private fun App.registerAdminCommandScripts(
        commandId: CommandId.Admin,
        scripts: List<CommandScript>
    ) {
        val handler = SlashCommandHandler { request, ctx ->
            if (!ctx.getUser(request).isBotAdmin) return@SlashCommandHandler ctx.ack()

            scripts.forEach { it.onCommandEvent(commandId, request, ctx) }

            ctx.ack()
        }

        when (commandId) {
            is CommandId.Admin.Str -> command(commandId.id, handler)
            is CommandId.Admin.Regex -> command(commandId.idRegex.toPattern(), handler)
        }
    }

    // endregion

    // region BlockActionScripts

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
                if (scriptsForId == null) {
                    blockActionIdToScript[id] = mutableListOf(script)
                } else {
                    scriptsForId += script
                }
            }
        }
        return blockActionIdToScript
    }

    private fun App.registerUserBlockActionScripts(
        blockActionId: BlockActionId.User,
        scripts: List<BlockActionScript>
    ) {
        val handler = BlockActionHandler { request, ctx ->
            if (!adminRepo.isBotEnabled()) return@BlockActionHandler ctx.ack()

            scripts
                .filter { adminRepo.isScriptEnabled(it.id) } // #14 improve performance when checking if scripts are enabled
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

    // endregion

    // region ViewSubmissionScripts

    private fun App.registerViewSubmissionScripts() {
        val viewSubmissionScripts = scripts.values.filterIsInstance<ViewSubmissionScript>()
        val viewSubmissionIdToScript = viewSubmissionScripts.toMapOfViewSubmissionIdToScripts()

        viewSubmissionIdToScript
            .forEach { (viewSubmissionId, scripts) ->
                when (viewSubmissionId) {
                    is ViewSubmissionId.User -> registerUserViewSubmissionScripts(viewSubmissionId, scripts)
                    is ViewSubmissionId.Admin -> registerAdminViewSubmissionScripts(viewSubmissionId, scripts)
                }
            }
    }

    private fun List<ViewSubmissionScript>.toMapOfViewSubmissionIdToScripts(): Map<ViewSubmissionId, List<ViewSubmissionScript>> {
        val viewSubmissionIdToScript = mutableMapOf<ViewSubmissionId, MutableList<ViewSubmissionScript>>()
        forEach { script ->
            script.viewSubmissionIds.forEach { id ->
                val scriptsForId = viewSubmissionIdToScript[id]
                if (scriptsForId == null) {
                    viewSubmissionIdToScript[id] = mutableListOf(script)
                } else {
                    scriptsForId += script
                }
            }
        }
        return viewSubmissionIdToScript
    }

    private fun App.registerUserViewSubmissionScripts(
        viewSubmissionId: ViewSubmissionId.User,
        scripts: List<ViewSubmissionScript>
    ) {
        val handler = ViewSubmissionHandler { request, ctx ->
            if (!adminRepo.isBotEnabled()) return@ViewSubmissionHandler ctx.ack()

            scripts
                .filter { adminRepo.isScriptEnabled(it.id) } // #14 improve performance when checking if scripts are enabled
                .forEach { it.onViewSubmissionEvent(viewSubmissionId, request, ctx) }

            ctx.ack()
        }

        when (viewSubmissionId) {
            is ViewSubmissionId.User.Str -> viewSubmission(viewSubmissionId.id, handler)
            is ViewSubmissionId.User.Regex -> viewSubmission(viewSubmissionId.idRegex.toPattern(), handler)
        }
    }

    private fun App.registerAdminViewSubmissionScripts(
        viewSubmissionId: ViewSubmissionId.Admin,
        scripts: List<ViewSubmissionScript>
    ) {
        val handler = ViewSubmissionHandler { request, ctx ->
            if (!ctx.getUser(request).isBotAdmin) return@ViewSubmissionHandler ctx.ack()

            scripts.forEach { it.onViewSubmissionEvent(viewSubmissionId, request, ctx) }

            ctx.ack()
        }

        when (viewSubmissionId) {
            is ViewSubmissionId.Admin.Str -> viewSubmission(viewSubmissionId.id, handler)
            is ViewSubmissionId.Admin.Regex -> viewSubmission(viewSubmissionId.idRegex.toPattern(), handler)
        }
    }

    // endregion

    companion object {

        fun create(adminRepo: AdminRepository) = ScriptHandler(adminRepo)
    }
}
