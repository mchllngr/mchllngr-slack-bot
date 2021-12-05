package service.script

import datastore.DataStore
import db.Script
import model.script.ScriptId

// #15 decide how this service should be available to the outside
interface ScriptService {

    fun getById(ids: Collection<ScriptId>): List<Script>

    fun insert(id: ScriptId)

    fun isEnabled(id: ScriptId): Boolean

    fun setEnabled(
        id: ScriptId,
        enabled: Boolean
    )

    companion object {

        fun create(dataStore: DataStore): ScriptService = ScriptServiceImpl(dataStore)
    }
}

class ScriptServiceImpl(dataStore: DataStore) : ScriptService {

    private val queries = dataStore.scriptQueries

    override fun getById(ids: Collection<ScriptId>) = queries.selectById(ids).executeAsList()

    override fun insert(id: ScriptId) {
        queries.insert(id)
    }

    override fun isEnabled(id: ScriptId) = runCatching { queries.selectEnabled(id).executeAsOneOrNull() }.getOrNull() ?: true

    override fun setEnabled(
        id: ScriptId,
        enabled: Boolean
    ) {
        queries.updateEnabled(enabled, id)
    }
}
