package script.home.block

import com.slack.api.bolt.request.builtin.BlockActionRequest
import com.slack.api.model.block.Blocks.actions
import com.slack.api.model.block.Blocks.section
import com.slack.api.model.block.LayoutBlock
import com.slack.api.model.block.composition.BlockCompositions.option
import com.slack.api.model.block.composition.BlockCompositions.plainText
import com.slack.api.model.block.element.BlockElements.checkboxes
import com.slack.api.model.block.element.BlockElements.datePicker
import db.User
import model.blockaction.BlockActionId
import model.user.UserId
import repository.user.UserRepository
import util.slack.block.headerSection
import util.slack.user.SlackUser
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class BirthdayBlocks(
    private val userRepo: UserRepository
) {

    private val datePickerFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun createBlocks(
        user: User
    ): List<LayoutBlock> = listOf(
        headerSection(text = ":birthday: Geburtstag", emoji = true),
        section { section ->
            section
                .text(plainText("Dein Geburtstag"))
                .accessory(
                    datePicker {
                        it.actionId(BLOCK_ACTION_ID_BIRTHDAY_CHANGED.id)
                        user.birthdate?.let { birthdate -> it.initialDate(birthdate.format(datePickerFormat)) }
                        it.placeholder(plainText("Geburtstag wählen"))
                    }
                )
        },
        actions(
            listOf(
                checkboxes {
                    val isBirthdayYearIncluded = user.includeBirthdateYear

                    val option = option { option ->
                        option.value(isBirthdayYearIncluded.not().toString())
                        option.text(plainText("Geburtsjahr einbeziehen"))
                        option.description(plainText("Entscheidet ob das Geburtsjahr anderen Nutzern angezeigt und für die Berechnung des Alters benutzt wird."))
                    }

                    it.actionId(BLOCK_ACTION_ID_BIRTHDAY_INCLUDE_YEAR_CHANGED.id)
                    if (isBirthdayYearIncluded) it.initialOptions(listOf(option))
                    it.options(listOf(option))
                }
            )
        ),
//        // #11 Allow user to remove own birthday
//        section { section ->
//            section
//                .text(plainText("Geburtsdatum entfernen"))
//                .accessory(
//                    button {
//                        it.actionId(BLOCK_ACTION_ID_BIRTHDAY_REMOVED.id)
//                        it.value("TODO_value")
//                        it.text(plainText(":warning: Entfernen", true))
//                        it.confirm(
//                            confirmationDialog { dialog ->
//                                dialog.title(plainText("Geburtsdatum entfernen"))
//                                dialog.text(plainText("Bist du sicher?"))
//                                dialog.confirm(plainText("Entfernen"))
//                                dialog.deny(plainText("Abbrechen"))
//                            }
//                        )
//                    }
//                )
//        }
    )

    fun onActionUserBirthdayChanged(
        slackUser: SlackUser,
        request: BlockActionRequest
    ) {
        val birthdayChangedValue = request.getBirthdayChangedValue()
        if (birthdayChangedValue != null) userRepo.updateBirthdate(UserId(slackUser.id), birthdayChangedValue)
    }

    fun onActionBirthdayIncludeYearChanged(
        slackUser: SlackUser,
        request: BlockActionRequest
    ) {
        val birthdayIncludeYearChangedValue = request.getBirthdayIncludeYearChangedValue()
        userRepo.updateIncludeBirthdateYear(UserId(slackUser.id), birthdayIncludeYearChangedValue)
    }

    private fun BlockActionRequest.getBirthdayChangedValue() = payload?.actions
        ?.find { it?.actionId == BLOCK_ACTION_ID_BIRTHDAY_CHANGED.id }
        ?.selectedDate
        ?.runCatching { LocalDate.parse(this) }?.getOrNull()

    private fun BlockActionRequest.getBirthdayIncludeYearChangedValue() = payload?.actions
        ?.find { it?.actionId == BLOCK_ACTION_ID_BIRTHDAY_INCLUDE_YEAR_CHANGED.id }
        ?.selectedOptions?.firstOrNull()
        ?.value?.toBoolean()
        ?: false

    companion object {

        val BLOCK_ACTION_ID_BIRTHDAY_CHANGED = BlockActionId.User.Str("ACTION_BIRTHDAY_CHANGED")
        val BLOCK_ACTION_ID_BIRTHDAY_INCLUDE_YEAR_CHANGED = BlockActionId.User.Str("ACTION_BIRTHDAY_INCLUDE_YEAR_CHANGED")
        val BLOCK_ACTION_ID_BIRTHDAY_REMOVED = BlockActionId.User.Str("ACTION_BIRTHDAY_REMOVED")
    }
}
