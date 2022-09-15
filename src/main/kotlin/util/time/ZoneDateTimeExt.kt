// ktlint-disable filename

package util.time

import util.slack.user.SlackUser
import util.slack.user.tzId
import java.time.ZonedDateTime

fun getZoneDateTimeFromSlackUser(slackUser: SlackUser?): ZonedDateTime = slackUser?.tzId?.let { ZonedDateTime.now(it) } ?: ZonedDateTime.now()
