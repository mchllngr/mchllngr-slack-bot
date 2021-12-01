package util.slack.user

import com.slack.api.model.User
import model.user.UserId
import servicelocator.ServiceLocator.config

val User?.isBotAdmin get() = this != null && UserId(id) in config.admin.ids
