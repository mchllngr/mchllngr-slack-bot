package network.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import servicelocator.ServiceLocator
import util.logger.getLogger

object HttpClientFactory {

    fun create(): HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                }
            )
        }

        if (ServiceLocator.config.debugMode) {
            install(Logging) {
                logger = object : Logger {

                    private val logger = getLogger<HttpClient>()

                    override fun log(message: String) {
                        logger.debug(message)
                    }
                }
                level = LogLevel.HEADERS
            }
        }
    }
}
