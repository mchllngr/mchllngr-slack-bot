package script.home.block

import com.slack.api.bolt.request.builtin.BlockActionRequest
import com.slack.api.model.block.Blocks.actions
import com.slack.api.model.block.LayoutBlock
import com.slack.api.model.block.composition.BlockCompositions.option
import com.slack.api.model.block.composition.BlockCompositions.plainText
import com.slack.api.model.block.element.BlockElements.checkboxes
import db.User
import model.blockaction.BlockActionId
import model.user.UserId
import repository.user.UserRepository
import util.slack.block.headerSection
import util.slack.user.SlackUser

class BirthdayReminderBlocks(
    private val userRepo: UserRepository
) {

    fun createBlocks(
        user: User
    ): List<LayoutBlock> = listOf(
        headerSection(text = ":date: Geburtstagserinnerungen", emoji = true),
        actions(
            listOf(
                checkboxes {
                    val areBirthdateRemindersEnabled = user.enableBirthdateReminders

                    val option = option { option ->
                        option.value(areBirthdateRemindersEnabled.not().toString())
                        option.text(plainText("Geburtstagserinnerungen erhalten"))
                    }

                    it.actionId(BLOCK_ACTION_ID_BIRTHDAY_REMINDER_ENABLED_CHANGED.id)
                    if (areBirthdateRemindersEnabled) it.initialOptions(listOf(option))
                    it.options(listOf(option))
                }
            )
        ),
//        // #10 Build home/birthdayReminder
//        section { section ->
//            section
//                .text(plainText("Möchtest du an weitere Geburtstage außerhalb deines Teams erinnert werden?"))
//                .accessory(
//                    button {
//                        it.actionId(BLOCK_ACTION_ID_BIRTHDAY_REMINDER_ADD_ADDITIONAL_SELECTED.id)
//                        it.value("TODO_value")
//                        it.text(plainText(":alarm_clock: Erinnerung hinzufügen", true))
//                    }
//                )
//        },
//        plainTextSection("Weitere Geburtstagserinnerungen:"),
//        section {
//            it.fields(
//                listOf(
//                    plainText("Person 0 (dd.MM.yyyy)"),
//                    plainText("Person 1 (dd.MM.yyyy)"),
//                    plainText("Person 2 (dd.MM.yyyy)"),
//                    plainText("Person 3 (dd.MM.yyyy)"),
//                    plainText("Person 4 (dd.MM.yyyy)")
//                )
//            )
//        },
//        plainTextSection(text = ":warning: TODO Figure out where to put the button to remove an additional person :warning:", emoji = true)
    )

    fun onActionBirthdayReminderEnabledChanged(
        slackUser: SlackUser,
        request: BlockActionRequest
    ) {
        val birthdayReminderEnabledChangedValue = request.getBirthdayReminderEnabledChangedValue()
        userRepo.updateEnableBirthdateReminders(UserId(slackUser.id), birthdayReminderEnabledChangedValue)
    }

    private fun BlockActionRequest.getBirthdayReminderEnabledChangedValue() = payload?.actions
        ?.find { it?.actionId == BLOCK_ACTION_ID_BIRTHDAY_REMINDER_ENABLED_CHANGED.id }
        ?.selectedOptions?.firstOrNull()
        ?.value?.toBoolean()
        ?: false

    companion object {

        val BLOCK_ACTION_ID_BIRTHDAY_REMINDER_ENABLED_CHANGED = BlockActionId.User.Str("ACTION_BIRTHDAY_REMINDER_ENABLED_CHANGED")
        val BLOCK_ACTION_ID_BIRTHDAY_REMINDER_ADD_ADDITIONAL_SELECTED = BlockActionId.User.Str("ACTION_BIRTHDAY_REMINDER_ADD_ADDITIONAL_SELECTED")
    }
}
