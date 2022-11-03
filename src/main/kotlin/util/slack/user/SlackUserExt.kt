package util.slack.user

import com.slack.api.model.User
import model.user.UserId
import servicelocator.ServiceLocator.config
import java.time.ZoneId
import java.util.*

typealias SlackUser = User

val SlackUser?.isBotAdmin get() = this != null && UserId(id) in config.admin.ids

val SlackUser.tzId: ZoneId get() = ZoneId.of(tz)

val SlackUser?.userLocale: Locale get() = this?.locale?.let { Locale.forLanguageTag(it) } ?: Locale.getDefault()

val SlackUser.usernameString get() = "<@$id>"
