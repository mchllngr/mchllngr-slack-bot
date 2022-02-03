package repository.admin

import datastore.DataStore
import db.Script
import model.script.ScriptId
import repository.script.ScriptRepository
import util.slack.user.SlackUser
import util.slack.user.isBotAdmin

interface AdminRepository {

    fun isBotEnabled(): Boolean

    fun setBotEnabled(
        slackUser: SlackUser?,
        enabled: Boolean
    )

    fun getScriptsById(ids: Collection<ScriptId>): List<Script>

    fun insertScript(id: ScriptId)

    fun isScriptEnabled(id: ScriptId): Boolean

    fun setScriptEnabled(
        slackUser: SlackUser?,
        id: ScriptId,
        enabled: Boolean
    )

    companion object {

        fun create(
            dataStore: DataStore,
            scriptRepo: ScriptRepository
        ): AdminRepository = AdminRepositoryImpl(dataStore, scriptRepo)
    }
}

class AdminRepositoryImpl(
    dataStore: DataStore,
    private val scriptRepo: ScriptRepository
) : AdminRepository {

    private val queries = dataStore.adminQueries

    override fun isBotEnabled() = runCatching { queries.selectAdmin().executeAsOneOrNull()?.botEnabled }.getOrNull() ?: true

    override fun setBotEnabled(
        slackUser: SlackUser?,
        enabled: Boolean
    ) {
        if (!slackUser.isBotAdmin) return

        queries.updateBotEnabled(enabled)
    }

    override fun getScriptsById(ids: Collection<ScriptId>) = scriptRepo.getById(ids)

    override fun insertScript(id: ScriptId) {
        scriptRepo.insert(id)
    }

    override fun isScriptEnabled(id: ScriptId) = scriptRepo.isEnabled(id)

    override fun setScriptEnabled(
        slackUser: SlackUser?,
        id: ScriptId,
        enabled: Boolean
    ) {
        if (!slackUser.isBotAdmin) return

        scriptRepo.setEnabled(id, enabled)
    }
}
