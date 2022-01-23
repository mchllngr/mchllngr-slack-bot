package util.time

import com.slack.api.model.User
import util.slack.user.tzId
import java.time.ZonedDateTime

fun getZoneDateTimeFromUser(user: User?): ZonedDateTime = user?.tzId?.let { ZonedDateTime.now(it) } ?: ZonedDateTime.now()
