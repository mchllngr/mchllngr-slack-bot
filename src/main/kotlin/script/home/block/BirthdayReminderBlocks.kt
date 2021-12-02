package script.home.block

import com.slack.api.model.block.Blocks.actions
import com.slack.api.model.block.Blocks.section
import com.slack.api.model.block.LayoutBlock
import com.slack.api.model.block.composition.BlockCompositions.option
import com.slack.api.model.block.composition.BlockCompositions.plainText
import com.slack.api.model.block.element.BlockElements.button
import com.slack.api.model.block.element.BlockElements.checkboxes
import model.blockaction.BlockActionId
import util.slack.block.headerSection
import util.slack.block.plainTextSection

class BirthdayReminderBlocks {

    fun createBlocks(): List<LayoutBlock> = listOf(
        headerSection(text = ":date: Geburtstagserinnerungen", emoji = true),
        actions(
            listOf(
                checkboxes {
                    it.actionId(BLOCK_ACTION_ID_BIRTHDAY_REMINDER_ENABLED_CHANGED.id)
                    it.options(
                        listOf(
                            option { option ->
                                option.value("TODO_value")
                                option.text(plainText("Geburtstagserinnerungen erhalten"))
                            }
                        )
                    )
                }
            )
        ),
        section { section ->
            section
                .text(plainText("Möchtest du an weitere Geburtstage außerhalb deines Teams erinnert werden?"))
                .accessory(
                    button {
                        it.actionId(BLOCK_ACTION_ID_BIRTHDAY_REMINDER_ADD_ADDITIONAL_SELECTED.id)
                        it.value("TODO_value")
                        it.text(plainText(":alarm_clock: Erinnerung hinzufügen", true))
                    }
                )
        },
        plainTextSection("Weitere Geburtstagserinnerungen:"),
        section {
            it.fields(
                listOf(
                    plainText("Person 0 (dd.MM.yyyy)"),
                    plainText("Person 1 (dd.MM.yyyy)"),
                    plainText("Person 2 (dd.MM.yyyy)"),
                    plainText("Person 3 (dd.MM.yyyy)"),
                    plainText("Person 4 (dd.MM.yyyy)")
                )
            )
        },
        plainTextSection(text = ":warning: TODO Figure out where to put the button to remove an additional person :warning:", emoji = true)
    )

    companion object {

        val BLOCK_ACTION_ID_BIRTHDAY_REMINDER_ENABLED_CHANGED = BlockActionId.User.Str("ACTION_BIRTHDAY_REMINDER_ENABLED_CHANGED")
        val BLOCK_ACTION_ID_BIRTHDAY_REMINDER_ADD_ADDITIONAL_SELECTED = BlockActionId.User.Str("ACTION_BIRTHDAY_REMINDER_ADD_ADDITIONAL_SELECTED")
    }
}
