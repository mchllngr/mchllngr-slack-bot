package network

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking

class ApiClient(
    private val client: HttpClient
) {

    fun getAbsences(): String = runBlocking {
        // TODO #29 do real stuff

        val response: HttpResponse = client.get("https://ktor.io/")
        return@runBlocking response.bodyAsText()
    }

    companion object {

        fun create(client: HttpClient) = ApiClient(client)
    }
}
