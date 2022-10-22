package repository.absence

import model.user.UserId
import network.absence.AbsenceApiClient
import java.time.ZonedDateTime
import java.util.*

interface AbsenceRepository {

    fun getUserAvailability(
        keyId: String,
        key: String,
        now: ZonedDateTime,
        userEmails: Map<UserId, String?>
    ): Map<UserId, Availability>

    companion object {

        fun create(absenceApiClient: AbsenceApiClient): AbsenceRepository = AbsenceRepositoryImpl(absenceApiClient)
    }
}

class AbsenceRepositoryImpl(private val absenceApiClient: AbsenceApiClient) : AbsenceRepository {

    override fun getUserAvailability(
        keyId: String,
        key: String,
        now: ZonedDateTime,
        userEmails: Map<UserId, String?>
    ): Map<UserId, Availability> {
        val emails = buildEmailList(userEmails.values.filterNotNull())
        val absencesResponse = absenceApiClient.getAbsences(keyId, key, now, emails)
        return userEmails.mapValues { (_, email) ->
            val reasonId = absencesResponse.getReasonId(email)
            mapReasonIdToAvailability(reasonId)
        }
    }

    private fun buildEmailList(emails: List<String>) = buildList {
        emails.forEach { email ->
            val lowercaseEmail = email.toLowerCaseEmail()
            add(lowercaseEmail)
            add(lowercaseEmail.toCapitalizedEmail())
        }
    }

    private fun String.toLowerCaseEmail() = lowercase(Locale.getDefault())

    private fun Map<String, String>.getReasonId(email: String?): String? {
        val lowercaseEmail = email?.toLowerCaseEmail()
        return get(lowercaseEmail) ?: get(lowercaseEmail?.toCapitalizedEmail())
    }

    private fun String.toCapitalizedEmail(): String {
        val parts = split("@")
        if (parts.size != 2) return this

        return parts[0].split(".")
            .joinToString(".") { part ->
                part.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            } + "@" + parts[1]
    }

    private fun mapReasonIdToAvailability(reasonId: String?): Availability = when (reasonId) {
        "60daf6fcb5d4d20afb95a97a" -> Availability.AVAILABLE_HOME_OFFICE // mobile Arbeit (mit Freigabe)
        "60daf6bab5dc1f0a17142ab4" -> Availability.AVAILABLE_HOME_OFFICE // mobile Arbeit (ohne Freigabe)
        null -> Availability.AVAILABLE_OFFICE
        else -> Availability.UNAVAILABLE
    }
}
