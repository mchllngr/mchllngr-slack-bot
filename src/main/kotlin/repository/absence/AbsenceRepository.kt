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
        userEmails: Map<UserId, String?>,
        locale: Locale
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
        userEmails: Map<UserId, String?>,
        locale: Locale
    ): Map<UserId, Availability> {
        val emails = buildEmailList(userEmails.values.filterNotNull(), locale)
        val absencesResponse = absenceApiClient.getAbsences(keyId, key, now, emails)
        return userEmails.mapValues { (_, email) ->
            val reasonId = absencesResponse.getReasonId(email, locale)
            mapReasonIdToAvailability(reasonId)
        }
    }

    private fun buildEmailList(
        emails: List<String>,
        locale: Locale
    ) = buildList {
        emails.forEach { email ->
            val lowercaseEmail = email.toLowerCaseEmail(locale)
            add(lowercaseEmail)
            add(lowercaseEmail.toCapitalizedEmail(locale))
        }
    }

    private fun String.toLowerCaseEmail(locale: Locale) = lowercase(locale)

    private fun Map<String, String>.getReasonId(
        email: String?,
        locale: Locale
    ): String? {
        val lowercaseEmail = email?.toLowerCaseEmail(locale)
        return get(lowercaseEmail) ?: get(lowercaseEmail?.toCapitalizedEmail(locale))
    }

    private fun String.toCapitalizedEmail(locale: Locale): String {
        val parts = split("@")
        if (parts.size != 2) return this

        return parts[0].split(".")
            .joinToString(".") { part ->
                part.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
            } + "@" + parts[1]
    }

    private fun mapReasonIdToAvailability(reasonId: String?): Availability = when (reasonId) {
        "60daf6fcb5d4d20afb95a97a" -> Availability.AVAILABLE_HOME_OFFICE // mobile Arbeit (mit Freigabe)
        "641c7f763c81a3f2b73458c1" -> Availability.AVAILABLE_HOME_OFFICE // mobile Arbeit (mit Freigabe) - kränkelnd: Ansteckungsgefahr
        "60daf6bab5dc1f0a17142ab4" -> Availability.AVAILABLE_HOME_OFFICE // mobile Arbeit (ohne Freigabe)
        "641c7f34a863afe0865d5a91" -> Availability.AVAILABLE_HOME_OFFICE // mobile Arbeit (ohne Freigabe) - kränkelnd: Ansteckungsgefahr
        null -> Availability.AVAILABLE_OFFICE
        else -> Availability.UNAVAILABLE
    }
}
