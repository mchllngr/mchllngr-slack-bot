package script

import com.slack.api.app_backend.events.payload.EventsApiPayload
import com.slack.api.bolt.context.builtin.EventContext
import com.slack.api.model.event.MessageEvent
import script.base.MessageScript
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@JvmInline
value class UserId(val id: String)

@JvmInline
value class TeamId(val id: String)

class User(
    val id: UserId,
    val birthdate: LocalDate
)

class Team(
    val id: TeamId,
    val admin: UserId,
    val users: List<UserId>
)

class BirthdayScript : MessageScript {

    // Mock-data
    private val users = mapOf(
        UserId("user1") to User(UserId("user1"), LocalDate.of(1990, 5, 1)),
        UserId("user2") to User(UserId("user2"), LocalDate.of(1970, 1, 1)),
        UserId("user3") to User(UserId("user3"), LocalDate.of(1954, 10, 21)),
        UserId("user4") to User(UserId("user4"), LocalDate.of(1954, 10, 23)),
        UserId("user5") to User(UserId("user5"), LocalDate.of(1954, 10, 27)),
        UserId("user6") to User(UserId("user6"), LocalDate.of(1954, 10, 28)),
        UserId("user6") to User(UserId("user6"), LocalDate.of(1954, 10, 20))
    )
    private val teams = mapOf(
        TeamId("team1") to Team(
            TeamId("team1"),
            UserId("user1"),
            listOf(
                UserId("user1"),
                UserId("user2")
            )
        )
    )

    override fun onMessageEvent(event: EventsApiPayload<MessageEvent>, ctx: EventContext) {
        when (event.event.text.lowercase()) {
            "show birthdays" -> {
                ctx.say("Birthdays:\n " + getBirthdays().joinToString(separator = "\n"))
            }
        }
    }

    private fun getBirthdays(): List<String> {
        val currentDate = ZonedDateTime.now(ZoneId.of(ZoneId.SHORT_IDS["ECT"]))
        return users.mapNotNull {
            val userName = it.value.id.id
            val daysUntilMsg = when (val daysUntilBirthday =
                calculateDaysUntilBirthday(currentDate.toLocalDate(), it.value.birthdate)) {
                7 -> "in einer Woche"
                3 -> "in $daysUntilBirthday Tagen"
                1 -> "morgen"
                0 -> "heute"
                else -> null
            }
            daysUntilMsg?.let { "$userName hat $daysUntilMsg Geburtstag" }
        }
    }

    private fun calculateDaysUntilBirthday(startDate: LocalDate, endDate: LocalDate): Int {
        val date = LocalDate.of(startDate.year, endDate.month, endDate.dayOfMonth)
        var daysUntilBirthday = startDate.until(date, ChronoUnit.DAYS)
        if (daysUntilBirthday < 0) {
            daysUntilBirthday = startDate.until(
                LocalDate.of(
                    startDate.year + 1,
                    endDate.month,
                    endDate.dayOfMonth
                ),
                ChronoUnit.DAYS
            )
        }
        return daysUntilBirthday.toInt()
    }

}