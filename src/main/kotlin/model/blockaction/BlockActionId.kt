package model.blockaction

import kotlin.text.Regex as RegexKt

sealed class BlockActionId {

    sealed class User : BlockActionId() {

        data class Str(val id: String) : User()

        data class Regex(val idRegex: RegexKt) : User()
    }

    sealed class Admin : BlockActionId() {

        data class Str(val id: String) : Admin()

        data class Regex(val idRegex: RegexKt) : Admin()
    }
}
