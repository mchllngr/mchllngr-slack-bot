package script.home.block

import com.slack.api.model.block.Blocks
import com.slack.api.model.block.LayoutBlock
import com.slack.api.model.block.composition.BlockCompositions
import com.slack.api.model.block.element.BlockElements
import util.slack.block.headerSection
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class BirthdayBlocks {

    private val datePickerFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun createBlocks(): List<LayoutBlock> {
        val birthday: LocalDate = LocalDate.of(1970, 1, 1)

        return listOf(
            headerSection(text = ":birthday: Geburtstag", emoji = true),
            Blocks.section { section ->
                section
                    .text(BlockCompositions.plainText("Dein Geburtstag"))
                    .accessory(
                        BlockElements.datePicker {
                            it.actionId(ACTION_USER_BIRTHDAY_CHANGED)
                            it.initialDate(birthday.format(datePickerFormat))
                            it.placeholder(BlockCompositions.plainText("Geburtstag wählen"))
                        }
                    )
            },
            Blocks.actions { action ->
                action.elements(
                    listOf(
                        BlockElements.checkboxes {
                            it.actionId(ACTION_USER_BIRTHDAY_INCLUDE_YEAR_CHANGED)
                            it.options(
                                listOf(
                                    BlockCompositions.option { option ->
                                        option.value("TODO_value")
                                        option.text(BlockCompositions.markdownText("*Geburtsjahr einbeziehen*"))
                                        option.description(BlockCompositions.plainText("Entscheidet ob das Geburtsjahr anderen Nutzern angezeigt und für die Berechnung des Alters benutzt wird."))
                                    }
                                )
                            )
                        }
                    )
                )
            },
            Blocks.section { section ->
                section
                    .text(BlockCompositions.plainText("Geburtsdatum entfernen"))
                    .accessory(
                        BlockElements.button {
                            it.actionId(ACTION_USER_BIRTHDAY_REMOVED)
                            it.value("TODO_value")
                            it.text(BlockCompositions.plainText(":warning: Entfernen", true))
                            it.confirm(
                                BlockCompositions.confirmationDialog { dialog ->
                                    dialog.title(BlockCompositions.plainText("Geburtsdatum entfernen"))
                                    dialog.text(BlockCompositions.plainText("Bist du sicher?"))
                                    dialog.confirm(BlockCompositions.plainText("Entfernen"))
                                    dialog.deny(BlockCompositions.plainText("Abbrechen"))
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
