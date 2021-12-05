package script.home.block

import com.slack.api.model.block.Blocks.actions
import com.slack.api.model.block.Blocks.section
import com.slack.api.model.block.LayoutBlock
import com.slack.api.model.block.composition.BlockCompositions.markdownText
import com.slack.api.model.block.composition.BlockCompositions.option
import com.slack.api.model.block.composition.BlockCompositions.plainText
import com.slack.api.model.block.element.BlockElements.checkboxes
import com.slack.api.model.block.element.BlockElements.datePicker
import model.blockaction.BlockActionId
import util.slack.block.headerSection
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class BirthdayBlocks {

    private val datePickerFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun createBlocks(): List<LayoutBlock> {
        val birthday: LocalDate = LocalDate.of(1970, 1, 1)

        return listOf(
            headerSection(text = ":birthday: Geburtstag", emoji = true),
            section { section ->
                section
                    .text(plainText("Dein Geburtstag"))
                    .accessory(
                        datePicker {
                            it.actionId(BLOCK_ACTION_ID_USER_BIRTHDAY_CHANGED.id)
                            it.initialDate(birthday.format(datePickerFormat))
                            it.placeholder(plainText("Geburtstag wählen"))
                        }
                    )
            },
            actions(
                listOf(
                    checkboxes {
                        it.actionId(BLOCK_ACTION_ID_USER_BIRTHDAY_INCLUDE_YEAR_CHANGED.id)
                        it.options(
                            listOf(
                                option { option ->
                                    option.value("TODO_value")
                                    option.text(markdownText("*Geburtsjahr einbeziehen*"))
                                    option.description(plainText("Entscheidet ob das Geburtsjahr anderen Nutzern angezeigt und für die Berechnung des Alters benutzt wird."))
                                }
                            )
                        )
                    }
                )
            ),
            // #11 Allow user to remove own birthday
//            section { section ->
//                section
//                    .text(plainText("Geburtsdatum entfernen"))
//                    .accessory(
//                        button {
//                            it.actionId(BLOCK_ACTION_ID_USER_BIRTHDAY_REMOVED.id)
//                            it.value("TODO_value")
//                            it.text(plainText(":warning: Entfernen", true))
//                            it.confirm(
//                                confirmationDialog { dialog ->
//                                    dialog.title(plainText("Geburtsdatum entfernen"))
//                                    dialog.text(plainText("Bist du sicher?"))
//                                    dialog.confirm(plainText("Entfernen"))
//                                    dialog.deny(plainText("Abbrechen"))
//                                }
//                            )
//                        }
//                    )
//            }
        )
    }

    companion object {

        val BLOCK_ACTION_ID_USER_BIRTHDAY_CHANGED = BlockActionId.User.Str("ACTION_USER_BIRTHDAY_CHANGED")
        val BLOCK_ACTION_ID_USER_BIRTHDAY_INCLUDE_YEAR_CHANGED = BlockActionId.User.Str("ACTION_USER_BIRTHDAY_INCLUDE_YEAR_CHANGED")
        val BLOCK_ACTION_ID_USER_BIRTHDAY_REMOVED = BlockActionId.User.Str("ACTION_USER_BIRTHDAY_REMOVED")
    }
}
