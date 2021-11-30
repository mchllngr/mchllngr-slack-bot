package util.slack.user

import com.slack.api.model.User
import model.MemberId
import servicelocator.ServiceLocator.config

val User?.isBotAdmin get() = this != null && MemberId(id) in config.admin.ids
