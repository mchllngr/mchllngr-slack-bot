package network.absence

import com.wealdtech.hawk.HawkClient
import com.wealdtech.hawk.HawkCredentials
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField

class AbsenceApiClient(
    private val client: HttpClient
) {

    fun getAbsences(
        keyId: String,
        key: String,
        now: ZonedDateTime,
        emails: List<String>
    ): Map<String, String> = runBlocking {
        val requestBody = getRequestBody(now, emails)

        val response: HttpResponse = client.post("https://app.absence.io/api/v2/absences") {
            method = HttpMethod.Post
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            header(HttpHeaders.Authorization, generateAuthorizationHeader(keyId, key))
            setBody(requestBody)
        }

        return@runBlocking response.body<AbsencesResponse>().list
            .associate { it.assignedTo.email to it.reasonId }
    }

    private fun getRequestBody(
        now: ZonedDateTime,
        emails: List<String>
    ): AbsencesRequest {
        val startOfDay: ZonedDateTime = now
            .with(ChronoField.SECOND_OF_DAY, 0)
            .with(ChronoField.MINUTE_OF_DAY, 0)
            .with(ChronoField.HOUR_OF_DAY, 0)
        val endOfDay: ZonedDateTime = now
            .with(ChronoField.SECOND_OF_DAY, 59)
            .with(ChronoField.MINUTE_OF_DAY, 59)
            .with(ChronoField.HOUR_OF_DAY, 23)

        return AbsencesRequest(
            skip = 0,
            limit = 50,
            filter = AbsencesRequest.Filter(
                assignedToUser = AbsencesRequest.Filter.User(
                    email = AbsencesRequest.Filter.User.Emails(
                        list = emails
                    )
                ),
                start = AbsencesRequest.Filter.Start(
                    lte = dateTimeFormatter.format(endOfDay)
                ),
                end = AbsencesRequest.Filter.End(
                    gte = dateTimeFormatter.format(startOfDay)
                )
            ),
            relations = listOf(
                "assignedToId",
                "reasonId",
                "approverId"
            )
        )
    }

    private fun HttpRequestBuilder.generateAuthorizationHeader(
        keyId: String,
        key: String
    ): String {
        val hawkCredentials = HawkCredentials.Builder()
            .keyId(keyId)
            .key(key)
            .algorithm(HawkCredentials.Algorithm.SHA256)
            .build()

        return HawkClient.Builder()
            .credentials(hawkCredentials)
            .build()
            .generateAuthorizationHeader(
                url.build().toURI(),
                method.value,
                null,
                null,
                null,
                null
            )
    }

    companion object {

        private val dateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

        fun create(client: HttpClient) = AbsenceApiClient(client)
    }
}
