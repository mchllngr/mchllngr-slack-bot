package script.home.block

import com.slack.api.model.block.Blocks.actions
import com.slack.api.model.block.Blocks.section
import com.slack.api.model.block.LayoutBlock
import com.slack.api.model.block.composition.BlockCompositions.confirmationDialog
import com.slack.api.model.block.composition.BlockCompositions.plainText
import com.slack.api.model.block.element.BlockElements.button
import util.slack.block.headerSection

class UserDataBlocks {

    fun createBlocks(): List<LayoutBlock> = listOf(
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
        actions(
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
    )

    companion object {

        const val ACTION_USER_DATA_SHOW_SELECTED = "ACTION_USER_DATA_SHOW_SELECTED"
        const val ACTION_USER_DATA_REMOVE_ALL_SELECTED = "ACTION_USER_DATA_REMOVE_ALL_SELECTED"
    }
}
