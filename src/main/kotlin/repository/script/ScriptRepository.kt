package repository.script

import datastore.DataStore
import db.Script
import model.script.ScriptId

// #15 decide how this repository should be available to the outside
interface ScriptRepository {

    fun getById(ids: Collection<ScriptId>): List<Script>

    fun insert(id: ScriptId)

    fun isEnabled(id: ScriptId): Boolean

    fun setEnabled(
        id: ScriptId,
        enabled: Boolean
    )

    companion object {

        fun create(dataStore: DataStore): ScriptRepository = ScriptRepositoryImpl(dataStore)
    }
}

class ScriptRepositoryImpl(dataStore: DataStore) : ScriptRepository {

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
