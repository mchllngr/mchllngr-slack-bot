package script.birthday

import com.slack.api.app_backend.events.payload.EventsApiPayload
import com.slack.api.bolt.context.Context
import com.slack.api.bolt.context.builtin.EventContext
import com.slack.api.model.block.LayoutBlock
import com.slack.api.model.event.MessageEvent
import db.User
import model.message.MessageId
import model.script.ScriptId
import model.user.UserId
import model.user.usernameString
import script.base.MessageScript
import servicelocator.ServiceLocator
import util.slack.block.markdownSection
import util.slack.context.addReactionToMessage
import util.slack.context.getUser
import util.slack.context.postChatMessageInChannel
import util.slack.context.removeReactionFromMessage
import util.time.getZoneDateTimeFromSlackUser
import java.time.LocalDate
import java.time.Period
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class BirthdayReminderScript : MessageScript {

    private val userRepo by lazy { ServiceLocator.User.repo }
    private val teamRepo by lazy { ServiceLocator.Team.repo }

    override val id = ID

    override val messageIds = listOf(
        MESSAGE_ID_SEND_BIRTHDAY_REMINDER
    )

    override fun onMessageEvent(
        messageId: MessageId,
        event: EventsApiPayload<MessageEvent>,
        ctx: EventContext
    ) {
        val slackUser = ctx.getUser(event)
        val now: ZonedDateTime = getZoneDateTimeFromSlackUser(slackUser)

        when (messageId) {
            MESSAGE_ID_SEND_BIRTHDAY_REMINDER -> onMessageSendBirthdayReminder(now, event, ctx)
            else -> Unit
        }
    }

    private fun onMessageSendBirthdayReminder(
        now: ZonedDateTime,
        event: EventsApiPayload<MessageEvent>,
        ctx: Context
    ) {
        addReactionToOriginalMessage("thinking_face", event, ctx)

        userRepo.selectAllBirthdateReminderEnabled()
            .forEach { user ->
                val birthdaysForUser = user.getBirthdays(now)
                if (birthdaysForUser.isNotEmpty()) {
                    postBirthdayReminderToUser(user, birthdaysForUser, ctx)
                }
            }

        addReactionToOriginalMessage("thumbsup", event, ctx)
        removeReactionFromOriginalMessage("thinking_face", event, ctx)
    }

    private fun User.getBirthdays(now: ZonedDateTime) = teamRepo.getTeamMembersWithBirthdateForUser(id)
        .mapNotNull { user ->
            val localNow: LocalDate = now.toLocalDate()

            var nextBirthday: LocalDate = LocalDate.of(localNow.year, user.birthdate.month, user.birthdate.dayOfMonth)
            if (nextBirthday.isBefore(localNow)) nextBirthday = nextBirthday.plusYears(1)

            val periodUntil = localNow.until(nextBirthday).takeIf { it in REMINDER_PERIOD_DAYS }

            return@mapNotNull if (periodUntil != null) {
                UserBirthday(user.id, user.birthdate, user.includeBirthdateYear, periodUntil)
            } else {
                null
            }
        }

    private fun postBirthdayReminderToUser(
        user: User,
        birthdaysForUser: List<UserBirthday>,
        ctx: Context
    ) {
        val blocks = birthdaysForUser.map { it.getBirthdayMessageBlock() }
        ctx.postChatMessageInChannel(user.id.id, blocks)
    }

    private fun addReactionToOriginalMessage(
        emojiName: String,
        event: EventsApiPayload<MessageEvent>,
        ctx: Context
    ) {
        val channelId = event.event?.channel
        val messageTs = event.event?.ts
        if (channelId.isNullOrEmpty() || messageTs.isNullOrEmpty()) return

        ctx.addReactionToMessage(channelId, messageTs, emojiName)
    }

    private fun removeReactionFromOriginalMessage(
        emojiName: String,
        event: EventsApiPayload<MessageEvent>,
        ctx: Context
    ) {
        val channelId = event.event?.channel
        val messageTs = event.event?.ts
        if (channelId.isNullOrEmpty() || messageTs.isNullOrEmpty()) return

        ctx.removeReactionFromMessage(channelId, messageTs, emojiName)
    }

    private data class UserBirthday(
        val userId: UserId,
        val birthdate: LocalDate,
        val includeBirthdateYear: Boolean,
        val periodUntil: Period
    ) {

        fun getBirthdayMessageBlock(): LayoutBlock {
            val daysUntilMsg = when (periodUntil) {
                PERIOD_DAYS_0 -> "heute"
                PERIOD_DAYS_1 -> "morgen"
                PERIOD_DAYS_3 -> "in 3 Tagen"
                PERIOD_DAYS_7 -> "in einer Woche"
                else -> "bald"
            }

            val age = if (includeBirthdateYear) {
                birthdate.until(LocalDate.now().plusDays(periodUntil.days.toLong()))?.years
            } else {
                null
            }

            val usernameString = userId.usernameString
            val birthdayMsg = if (age != null) {
                "$usernameString wird *$daysUntilMsg* (${birthdate.format(DATE_FORMATTER)}) *$age Jahre* alt!"
            } else {
                "$usernameString hat *$daysUntilMsg* (${birthdate.format(DATE_WITHOUT_YEAR_FORMATTER)}) Geburtstag!"
            }

            val message = ":birthday::cake: $birthdayMsg :cake::birthday:"

            return markdownSection(message)
        }
    }

    companion object {

        val ID = ScriptId("BIRTHDAY_REMINDER")

        private val MESSAGE_ID_SEND_BIRTHDAY_REMINDER = MessageId.User.Regex("^.*run script: send birthday reminder\\.?$".toRegex(RegexOption.IGNORE_CASE))

        private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.GERMANY) // #18 also localize date format
        private val DATE_WITHOUT_YEAR_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.", Locale.GERMANY) // #18 also localize date format

        private val PERIOD_DAYS_0: Period = Period.ZERO
        private val PERIOD_DAYS_1: Period = Period.ofDays(1)
        private val PERIOD_DAYS_3: Period = Period.ofDays(3)
        private val PERIOD_DAYS_7: Period = Period.ofDays(7)

        private val REMINDER_PERIOD_DAYS = listOf(PERIOD_DAYS_0, PERIOD_DAYS_1, PERIOD_DAYS_3, PERIOD_DAYS_7)
    }
}
