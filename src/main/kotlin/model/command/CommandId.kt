package model.command

sealed class CommandId {

    sealed class User : CommandId() {

        data class Str(val id: String) : User()

        data class Regex(val idRegex: kotlin.text.Regex) : User()
    }

    sealed class Admin : CommandId() {

        data class Str(val id: String) : Admin()

        data class Regex(val idRegex: kotlin.text.Regex) : Admin()
    }
}
