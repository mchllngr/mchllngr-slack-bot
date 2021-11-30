package script.home.block

import com.slack.api.model.block.Blocks.actions
import com.slack.api.model.block.Blocks.section
import com.slack.api.model.block.LayoutBlock
import com.slack.api.model.block.composition.BlockCompositions.confirmationDialog
import com.slack.api.model.block.composition.BlockCompositions.markdownText
import com.slack.api.model.block.composition.BlockCompositions.option
import com.slack.api.model.block.composition.BlockCompositions.plainText
import com.slack.api.model.block.element.BlockElements.button
import com.slack.api.model.block.element.BlockElements.checkboxes
import com.slack.api.model.block.element.BlockElements.datePicker
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
                            it.actionId(ACTION_USER_BIRTHDAY_CHANGED)
                            it.initialDate(birthday.format(datePickerFormat))
                            it.placeholder(plainText("Geburtstag wählen"))
                        }
                    )
            },
            actions { action ->
                action.elements(
                    listOf(
                        checkboxes {
                            it.actionId(ACTION_USER_BIRTHDAY_INCLUDE_YEAR_CHANGED)
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
                )
            },
            section { section ->
                section
                    .text(plainText("Geburtsdatum entfernen"))
                    .accessory(
                        button {
                            it.actionId(ACTION_USER_BIRTHDAY_REMOVED)
                            it.value("TODO_value")
                            it.text(plainText(":warning: Entfernen", true))
                            it.confirm(
                                confirmationDialog { dialog ->
                                    dialog.title(plainText("Geburtsdatum entfernen"))
                                    dialog.text(plainText("Bist du sicher?"))
                                    dialog.confirm(plainText("Entfernen"))
                                    dialog.deny(plainText("Abbrechen"))
                                }
                            )
                        }
                    )
            }
        )
    }

    companion object {

        const val ACTION_USER_BIRTHDAY_CHANGED = "ACTION_USER_BIRTHDAY_CHANGED"
        const val ACTION_USER_BIRTHDAY_INCLUDE_YEAR_CHANGED = "ACTION_USER_BIRTHDAY_INCLUDE_YEAR_CHANGED"
        const val ACTION_USER_BIRTHDAY_REMOVED = "ACTION_USER_BIRTHDAY_REMOVED"
    }
}
