package script.home.block

import com.slack.api.model.block.Blocks
import com.slack.api.model.block.LayoutBlock
import com.slack.api.model.block.composition.BlockCompositions
import com.slack.api.model.block.element.BlockElements
import util.slack.block.headerSection
import util.slack.block.plainTextSection

class BirthdayReminderBlocks {

    fun createBlocks(): List<LayoutBlock> = listOf(
        headerSection(text = ":date: Geburtstagserinnerungen", emoji = true),
        Blocks.actions { action ->
            action.elements(
                listOf(
                    BlockElements.checkboxes {
                        it.actionId(ACTION_BIRTHDAY_REMINDER_ENABLED_CHANGED)
                        it.options(
                            listOf(
                                BlockCompositions.option { option ->
                                    option.value("TODO_value")
                                    option.text(BlockCompositions.plainText("Geburtstagserinnerungen erhalten"))
                                }
                            )
                        )
                    }
                )
            )
        },
        Blocks.section { section ->
            section
                .text(BlockCompositions.plainText("Möchtest du an weitere Geburtstage außerhalb deines Teams erinnert werden?"))
                .accessory(
                    BlockElements.button {
                        it.actionId(ACTION_BIRTHDAY_REMINDER_ADD_ADDITIONAL_SELECTED)
                        it.value("TODO_value")
                        it.text(BlockCompositions.plainText(":alarm_clock: Erinnerung hinzufügen", true))
                    }
                )
        },
        plainTextSection("Weitere Geburtstagserinnerungen:"),
        Blocks.section {
            it.fields(
                listOf(
                    BlockCompositions.plainText("Person 0 (dd.MM.yyyy)"),
                    BlockCompositions.plainText("Person 1 (dd.MM.yyyy)"),
                    BlockCompositions.plainText("Person 2 (dd.MM.yyyy)"),
                    BlockCompositions.plainText("Person 3 (dd.MM.yyyy)"),
                    BlockCompositions.plainText("Person 4 (dd.MM.yyyy)")
                )
            )
        },
        plainTextSection(text = ":warning: TODO Figure out where to put the button to remove an additional person :warning:", emoji = true)
    )

    companion object {

        const val ACTION_BIRTHDAY_REMINDER_ENABLED_CHANGED = "ACTION_BIRTHDAY_REMINDER_ENABLED_CHANGED"
        const val ACTION_BIRTHDAY_REMINDER_ADD_ADDITIONAL_SELECTED = "ACTION_BIRTHDAY_REMINDER_ADD_ADDITIONAL_SELECTED"
    }
}
