package model.view.submission

import kotlin.text.Regex as RegexKt

sealed class ViewSubmissionId {

    sealed class User : ViewSubmissionId() {

        data class Str(val id: String) : User()

        data class Regex(val idRegex: RegexKt) : User()
    }

    sealed class Admin : ViewSubmissionId() {

        data class Str(val id: String) : Admin()

        data class Regex(val idRegex: RegexKt) : Admin()
    }
}
