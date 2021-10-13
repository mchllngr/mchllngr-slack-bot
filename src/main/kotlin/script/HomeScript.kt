package script

import com.slack.api.app_backend.events.payload.EventsApiPayload
import com.slack.api.bolt.context.Context
import com.slack.api.bolt.context.builtin.ActionContext
import com.slack.api.bolt.context.builtin.EventContext
import com.slack.api.bolt.request.builtin.BlockActionRequest
import com.slack.api.model.User
import com.slack.api.model.block.Blocks.actions
import com.slack.api.model.block.Blocks.divider
import com.slack.api.model.block.Blocks.section
import com.slack.api.model.block.LayoutBlock
import com.slack.api.model.block.composition.BlockCompositions.confirmationDialog
import com.slack.api.model.block.composition.BlockCompositions.markdownText
import com.slack.api.model.block.composition.BlockCompositions.option
import com.slack.api.model.block.composition.BlockCompositions.plainText
import com.slack.api.model.block.element.BlockElements.button
import com.slack.api.model.block.element.BlockElements.checkboxes
import com.slack.api.model.block.element.BlockElements.datePicker
import com.slack.api.model.event.AppHomeOpenedEvent
import com.slack.api.model.view.Views.view
import script.base.AppHomeOpenedScript
import script.base.BlockActionScript
import util.context.getUser
import util.slack.block.headerSection
import util.slack.block.markdownSection
import util.slack.block.plainTextSection
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalStdlibApi::class)
class HomeScript : AppHomeOpenedScript, BlockActionScript {

    private val dateTimeFooterFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy 'um' HH:mm:ss 'Uhr'")
    private val datePickerFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    override val blockActionIds = listOf(
        ACTION_USER_BIRTHDAY_CHANGED,
        ACTION_USER_BIRTHDAY_INCLUDE_YEAR_CHANGED,
        ACTION_USER_BIRTHDAY_REMOVED,
        ACTION_BIRTHDAY_REMINDER_ENABLED_CHANGED,
        ACTION_BIRTHDAY_REMINDER_ADD_ADDITIONAL_SELECTED,
        ACTION_USER_DATA_SHOW_SELECTED,
        ACTION_USER_DATA_REMOVE_ALL_SELECTED
    )

    override fun onAppHomeOpenedEvent(
        event: EventsApiPayload<AppHomeOpenedEvent>,
        ctx: EventContext
    ) {
        if (event.event.tab != KEY_HOME) return

        ctx.updateHomeView(
            event.event.user,
            event.event.view?.hash.orEmpty()
        )
    }

    override fun onBlockActionEvent(
        blockActionId: String,
        request: BlockActionRequest,
        ctx: ActionContext
    ) {
        ctx.updateHomeView(
            request.payload.user.id,
            request.payload.view.hash
        )
    }

    private fun Context.updateHomeView(
        userId: String,
        viewHash: String
    ) {
        val user = getUser(userId)

        client().viewsPublish { requestBuilder ->
            requestBuilder
                .userId(userId)
                .hash(viewHash) // to protect against possible race conditions
                .view(
                    view { view ->
                        view
                            .type(KEY_HOME)
                            .blocks(createHomeBlocks(user))
                    }
                )
        }
    }

    private fun createHomeBlocks(
        user: User?
    ): List<LayoutBlock> {
        val now: ZonedDateTime = ZonedDateTime.now()

        return buildList {
            addAll(createHelloBlocks(user))
            add(divider())
            addAll(createTeamBlocks())
            add(divider())
            addAll(createBirthdayBlocks())
            add(divider())
            addAll(createBirthdayReminderBlocks())
            add(divider())
            addAll(createUserDataBlocks())
            add(divider())
            addAll(createFooterBlocks(now))
        }
    }

    private fun createHelloBlocks(
        user: User?
    ): List<LayoutBlock> {
        val realName = user?.realName

        return listOf(
            headerSection(
                text = buildString {
                    append(":wave: Hallo")
                    if (realName.isNullOrBlank()) append("") else append(" $realName")
                    append("!")
                },
                emoji = true
            )
        )
    }

    private fun createTeamBlocks(): List<LayoutBlock> = listOf(
        headerSection(text = ":busts_in_silhouette: Team", emoji = true),
        markdownSection("Du gehörst zum Team *TODO*."),
        plainTextSection("Teammitglieder:"),
        section {
            it.fields(
                listOf(
                    markdownText("Person 0 *(Teamadmin)*"),
                    markdownText("Person 1 *(Teamadmin)*"),
                    plainText("Person 2"),
                    plainText("Person 3"),
                    plainText("Person 4")
                )
            )
        }
    )

    private fun createBirthdayBlocks(): List<LayoutBlock> {
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

    private fun createBirthdayReminderBlocks(): List<LayoutBlock> = listOf(
        headerSection(text = ":date: Geburtstagserinnerungen", emoji = true),
        actions { action ->
            action.elements(
                listOf(
                    checkboxes {
                        it.actionId(ACTION_BIRTHDAY_REMINDER_ENABLED_CHANGED)
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
            )
        },
        section { section ->
            section
                .text(plainText("Möchtest du an weitere Geburtstage außerhalb deines Teams erinnert werden?"))
                .accessory(
                    button {
                        it.actionId(ACTION_BIRTHDAY_REMINDER_ADD_ADDITIONAL_SELECTED)
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

    private fun createUserDataBlocks(): List<LayoutBlock> = listOf(
        headerSection(text = ":information_source: Nutzerdaten", emoji = true),
        section { section ->
            section
                .text(plainText("Alle von diesem Bot gespeicherten Daten über mich anzeigen"))
                .accessory(
                    button {
                        it.actionId(ACTION_USER_DATA_SHOW_SELECTED)
                        it.value("TODO_value")
                        it.text(plainText(":mag: Anzeigen", true))
                    }
                )
        },
        actions { action ->
            action.elements(
                listOf(
                    button {
                        it.actionId(ACTION_USER_DATA_REMOVE_ALL_SELECTED)
                        it.value("TODO_value")
                        it.text(plainText(":warning: Nutzerdaten löschen", true))
                        it.confirm(
                            confirmationDialog { dialog ->
                                dialog.title(plainText("Nutzerdaten löschen"))
                                dialog.text(plainText("Bist du sicher?\n\n:warning: Diese Aktion kann nicht rückgängig gemacht werden! :warning:", true))
                                dialog.confirm(plainText("Alles löschen"))
                                dialog.deny(plainText("Abbrechen"))
                            }
                        )
                    }
                )
            )
        }
    )

    private fun createFooterBlocks(now: ZonedDateTime) = listOf(
        markdownSection(":information_source: Fragen und Verbesserungsvorschläge zum Bot bitte an Michael Langer"),
        markdownSection(":clock3: Seite zuletzt aktualisiert am ${now.format(dateTimeFooterFormat)}")
    )

    companion object {

        private const val KEY_HOME = "home"

        private const val ACTION_USER_BIRTHDAY_CHANGED = "ACTION_USER_BIRTHDAY_CHANGED"
        private const val ACTION_USER_BIRTHDAY_INCLUDE_YEAR_CHANGED = "ACTION_USER_BIRTHDAY_INCLUDE_YEAR_CHANGED"
        private const val ACTION_USER_BIRTHDAY_REMOVED = "ACTION_USER_BIRTHDAY_REMOVED"

        private const val ACTION_BIRTHDAY_REMINDER_ENABLED_CHANGED = "ACTION_BIRTHDAY_REMINDER_ENABLED_CHANGED"
        private const val ACTION_BIRTHDAY_REMINDER_ADD_ADDITIONAL_SELECTED = "ACTION_BIRTHDAY_REMINDER_ADD_ADDITIONAL_SELECTED"

        private const val ACTION_USER_DATA_SHOW_SELECTED = "ACTION_USER_DATA_SHOW_SELECTED"
        private const val ACTION_USER_DATA_REMOVE_ALL_SELECTED = "ACTION_USER_DATA_REMOVE_ALL_SELECTED"
    }
}
