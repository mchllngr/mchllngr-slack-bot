package service.admin

import datastore.DataStore

interface AdminService {

    fun isBotEnabled(): Boolean

    fun setBotEnabled(enabled: Boolean)

    companion object {

        fun create(dataStore: DataStore): AdminService = AdminServiceImpl(dataStore)
    }
}

class AdminServiceImpl(dataStore: DataStore) : AdminService {

    private val queries = dataStore.adminQueries

    override fun isBotEnabled() = runCatching { queries.selectAdmin().executeAsOneOrNull()?.bot_enabled }.getOrNull() ?: true

    override fun setBotEnabled(enabled: Boolean) {
        queries.updateBotEnabled(enabled)
    }
}
