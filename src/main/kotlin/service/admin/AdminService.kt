package service.admin

import com.slack.api.model.User
import datastore.DataStore
import db.Script
import model.script.ScriptId
import service.script.ScriptService
import util.slack.user.isBotAdmin

interface AdminService {

    fun isBotEnabled(): Boolean

    fun setBotEnabled(
        user: User?,
        enabled: Boolean
    )

    fun getScriptsById(ids: Collection<ScriptId>): List<Script>

    fun insertScript(id: ScriptId)

    fun isScriptEnabled(id: ScriptId): Boolean

    fun setScriptEnabled(
        user: User?,
        id: ScriptId,
        enabled: Boolean
    )

    companion object {

        fun create(
            dataStore: DataStore,
            scriptService: ScriptService
        ): AdminService = AdminServiceImpl(dataStore, scriptService)
    }
}

class AdminServiceImpl(
    dataStore: DataStore,
    private val scriptService: ScriptService
) : AdminService {

    private val queries = dataStore.adminQueries

    override fun isBotEnabled() = runCatching { queries.selectAdmin().executeAsOneOrNull()?.botEnabled }.getOrNull() ?: true

    override fun setBotEnabled(
        user: User?,
        enabled: Boolean
    ) {
        if (!user.isBotAdmin) return

        queries.updateBotEnabled(enabled)
    }

    override fun getScriptsById(ids: Collection<ScriptId>) = scriptService.getById(ids)

    override fun insertScript(id: ScriptId) {
        scriptService.insert(id)
    }

    override fun isScriptEnabled(id: ScriptId) = scriptService.isEnabled(id)

    override fun setScriptEnabled(
        user: User?,
        id: ScriptId,
        enabled: Boolean
    ) {
        if (!user.isBotAdmin) return

        scriptService.setEnabled(id, enabled)
    }
}
