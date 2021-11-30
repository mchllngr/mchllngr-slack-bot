package service.admin

import com.slack.api.model.User
import datastore.DataStore
import util.slack.user.isBotAdmin

interface AdminService {

    fun isBotEnabled(): Boolean

    fun setBotEnabled(
        user: User?,
        enabled: Boolean
    )

    companion object {

        fun create(dataStore: DataStore): AdminService = AdminServiceImpl(dataStore)
    }
}

class AdminServiceImpl(dataStore: DataStore) : AdminService {

    private val queries = dataStore.adminQueries

    override fun isBotEnabled() = runCatching { queries.selectAdmin().executeAsOneOrNull()?.bot_enabled }.getOrNull() ?: true

    override fun setBotEnabled(
        user: User?,
        enabled: Boolean
    ) {
        if (!user.isBotAdmin) return

        queries.updateBotEnabled(enabled)
    }
}
