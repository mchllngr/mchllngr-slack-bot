package service.bot

import datastore.DataStore

interface BotConfigService {

    fun isBotEnabled(): Boolean

    fun setBotEnabled(enabled: Boolean)

    companion object {

        fun create(dataStore: DataStore): BotConfigService = BotConfigServiceImpl(dataStore)
    }
}

class BotConfigServiceImpl(dataStore: DataStore) : BotConfigService {

    private val queries = dataStore.botConfigQueries

    override fun isBotEnabled() = runCatching { queries.selectBotConfig().executeAsOneOrNull()?.enabled }.getOrNull() ?: true

    override fun setBotEnabled(enabled: Boolean) {
        queries.updateBotEnabled(enabled)
    }
}
