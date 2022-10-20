package repository.absence

import model.user.UserId
import network.absence.AbsenceApiClient
import java.time.ZonedDateTime

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
        val emails = userEmails.values.filterNotNull()
        val absencesResponse = absenceApiClient.getAbsences(keyId, key, now, emails)
        return userEmails.mapValues { (_, email) ->
            val reasonId = absencesResponse[email]
            mapReasonIdToAvailability(reasonId)
        }
    }

    private fun mapReasonIdToAvailability(reasonId: String?): Availability = when (reasonId) {
        "60daf6fcb5d4d20afb95a97a" -> Availability.AVAILABLE_HOME_OFFICE // mobile Arbeit (mit Freigabe)
        "60daf6bab5dc1f0a17142ab4" -> Availability.AVAILABLE_HOME_OFFICE // mobile Arbeit (ohne Freigabe)
        "54f81e196a72a10300e0f856" -> Availability.UNAVAILABLE // Urlaub
        "54fd9e846a42820300d747f6" -> Availability.UNAVAILABLE // Krank ohne Lohnfortzahlung
        "54f81e196a72a10300e0f858" -> Availability.UNAVAILABLE // Krankheit
        "5bcf31d5a04bf7492fea144a" -> Availability.UNAVAILABLE // Krankheit ohne Attest
        "5bcf32034d71600341cbbd73" -> Availability.UNAVAILABLE // Krankheit mit Attest
        "55b9ef90c903052a5eccf9f5" -> Availability.UNAVAILABLE // Unbezahlter Urlaub
        "54f862808d1a200300996dbd" -> Availability.UNAVAILABLE // Kind krank
        "55c8ac81586cc0ca6dee3e3f" -> Availability.UNAVAILABLE // Berufsschule/Hochschule
        "55a6492bb5c0bb262491a69b" -> Availability.UNAVAILABLE // Beschaeftigungsverbot
        "55a6493d0ece8e2124821d3d" -> Availability.UNAVAILABLE // Beschaeftigungsverbot halbtags
        "5512c4a2401a7103003b6ae8" -> Availability.UNAVAILABLE // Elternzeit
        "5d4bcb991495ff79f9196d72" -> Availability.UNAVAILABLE // Freistellung unwiderruflich
        "5d4bcc0aebccf479ae824e45" -> Availability.UNAVAILABLE // Freistellung widerruflich
        "54f96605780ec6030018b510" -> Availability.UNAVAILABLE // Mutterschutz
        "5bcf32eda8975f054ee9407b" -> Availability.UNAVAILABLE // Schule
        "54f836718d1a200300996d17" -> Availability.UNAVAILABLE // Sonderurl. Tod Angeh. 1.Grades
        "54f837246a72a10300e0f936" -> Availability.UNAVAILABLE // Sonderurlaub Geburt eig. Kind
        "567a8dd5c8d4c28f4bb5305e" -> Availability.UNAVAILABLE // Sonderurlaub besond. Leistung
        "54f81f3b8d1a200300996c7a" -> Availability.UNAVAILABLE // Sonderurlaub eigene Hochzeit
        "54f8629a8d1a200300996dbe" -> Availability.UNAVAILABLE // Unfall
        "54f834138d1a200300996d13" -> Availability.UNAVAILABLE // Weiterbildung
        else -> Availability.AVAILABLE_OFFICE
    }
}
