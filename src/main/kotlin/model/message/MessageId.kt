package model.message

import java.util.regex.Pattern

sealed class MessageId {

    sealed class User : MessageId() {

        data class Str(val id: String) : User() {

            /** @see [com.slack.api.bolt.App.message] for pattern */
            val idRegex = "^.*${Pattern.quote(id)}.*$".toRegex()
        }

        data class Regex(val idRegex: kotlin.text.Regex) : User()
    }

    sealed class Admin : MessageId() {

        data class Str(val id: String) : Admin() {

            /** @see [com.slack.api.bolt.App.message] for pattern */
            val idRegex = "^.*${Pattern.quote(id)}.*$".toRegex()
        }

        data class Regex(val idRegex: kotlin.text.Regex) : Admin()
    }
}
