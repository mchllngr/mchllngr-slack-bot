package util.slack.user

import com.slack.api.model.User
import model.user.UserId
import servicelocator.ServiceLocator.config
import java.time.ZoneId

val User?.isBotAdmin get() = this != null && UserId(id) in config.admin.ids

val User.tzId: ZoneId get() = ZoneId.of(tz)
