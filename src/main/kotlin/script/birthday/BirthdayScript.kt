package script.birthday

import com.slack.api.app_backend.events.payload.EventsApiPayload
import com.slack.api.bolt.context.builtin.EventContext
import com.slack.api.model.event.MessageEvent
import db.SelectUsersForTeam
import db.Team
import db.User
import model.script.ScriptId
import model.team.TeamId
import model.user.UserId
import model.user.usernameString
import script.base.MessageScript
import servicelocator.ServiceLocator.teamRepo
import servicelocator.ServiceLocator.userRepo
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

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

    private val userRepository by lazy { userRepo }
    private val teamRepository by lazy { teamRepo }

    private val currentDate = ZonedDateTime.now(ZoneId.of(ZoneId.SHORT_IDS["ECT"]))

    override val id = ID

    override fun onMessageEvent(event: EventsApiPayload<MessageEvent>, ctx: EventContext) {
        when (event.event.text.lowercase()) {
            "show next birthdays" -> {
                val result = getNextBirthdaysIn7DaysFromAllUsers()
                val response = when {
                    result.isNotEmpty() -> "Birthdays:\n" + result.joinToString(separator = "\n")
                    else -> "No birthday in the next 7 days"
                }
                ctx.say(response)
            }

            "show all birthdays" -> {
                ctx.say("Birthdays:\n" + getBirthdaysFromAllUsers().joinToString(separator = "\n"))
            }

            "notify users about birthdays" -> {
                notifyUserForIncomingBirthdays(ctx)
            }
        }
    }

    private fun getBirthdaysFromAllUsers(): List<String> {
        val users = getAllUser()

        return users.map { user ->
            val userName = user.id.usernameString
            val daysUntilMsg = user.birthdate?.let { it1 -> calculateDaysUntilBirthday(currentDate.toLocalDate(), it1) }
            daysUntilMsg?.let { message -> "$userName hat am ${getBirthdate(user.birthdate)} (in $message Tagen) Geburtstag" } ?: "Kein Geburtstag hinterlegt"
        }
    }

    private fun getNextBirthdaysIn7DaysFromAllUsers(): List<String> {
        val users = getAllUser()

        return users.mapNotNull {
            val userName = it.id.usernameString
            val daysUntilMsg = when (val daysUntilBirthday = it.birthdate?.let { it1 -> calculateDaysUntilBirthday(currentDate.toLocalDate(), it1) }) {
                7 -> "in einer Woche"
                3 -> "in $daysUntilBirthday Tagen"
                1 -> "morgen"
                0 -> "heute"
                else -> null
            }
            daysUntilMsg?.let { "$userName hat $daysUntilMsg Geburtstag" }
        }
    }

    private fun List<SelectUsersForTeam>.getNextBirthdaysIn7Days(): List<String> {
        return mapNotNull {
            val userName = it.id.usernameString
            val daysUntilMsg = when (val daysUntilBirthday = it.birthdate?.let { it1 -> calculateDaysUntilBirthday(currentDate.toLocalDate(), it1) }) {
                7 -> "in einer Woche"
                3 -> "in $daysUntilBirthday Tagen"
                1 -> "morgen"
                0 -> "heute"
                else -> null
            }
            daysUntilMsg?.let { "$userName hat $daysUntilMsg Geburtstag" }
        }
    }

    private fun notifyUserForIncomingBirthdays(ctx: EventContext) {
        val users = getAllUser()
        users.forEach { user ->
            val nextBirthdaysIn7Days = user.getBirthdaysForUser()

            val response = if (nextBirthdaysIn7Days.isNotEmpty()) {
                buildString {
                    appendLine("Geburtstage:")
                    nextBirthdaysIn7Days.forEach { it ->
                        appendLine(it)
                    }
                }
            } else {
                "Keine anstehenden Geburtstage"
            }
            ctx.client().chatPostMessage {
                it.token(ctx.botToken)
                it.channel(user.id.id)
                ctx.logger.info(ctx.requestUserId)

                it.text(response)
            }
        }
    }
    private fun User.getBirthdaysForUser(): List<String> {
        val teams = getTeamsForUser(id)
        val teamMember = buildList {
            teams.forEach { team ->
                addAll(getUserInTeam(team.id))
            }
        }.distinctBy { it.id }.filterNot { it.id == id }
        return teamMember.getNextBirthdaysIn7Days()
    }

    private fun getAllUser(): List<User> = userRepository.selectAll()

    private fun getTeamsForUser(userId: UserId): List<Team> {
        return teamRepository.getTeamsForUser(userId)
    }

    private fun getUserInTeam(teamId: TeamId): List<SelectUsersForTeam> {
        return teamRepository.getUsersForTeam(teamId)
    }

    private fun getBirthdate(birthdate: LocalDate): String? {
        val formatter = DateTimeFormatter.ofPattern("MM.dd.yyyy")
        return birthdate.format(formatter)
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

    companion object {

        val ID = ScriptId("BIRTHDAY")

    }
}