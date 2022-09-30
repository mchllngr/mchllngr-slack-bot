package model.user

@JvmInline
value class UserId(val id: String)

val UserId.usernameString get() = "<@$id>"
