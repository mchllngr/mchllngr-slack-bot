package script.home.block

import com.slack.api.model.block.Blocks
import com.slack.api.model.block.LayoutBlock
import com.slack.api.model.block.composition.BlockCompositions
import com.slack.api.model.block.element.BlockElements
import util.slack.block.headerSection

class UserDataBlocks {

    fun createBlocks(): List<LayoutBlock> = listOf(
        headerSection(text = ":information_source: Nutzerdaten", emoji = true),
        Blocks.section { section ->
            section
                .text(BlockCompositions.plainText("Alle von diesem Bot gespeicherten Daten über mich anzeigen"))
                .accessory(
                    BlockElements.button {
                        it.actionId(ACTION_USER_DATA_SHOW_SELECTED)
                        it.value("TODO_value")
                        it.text(BlockCompositions.plainText(":mag: Anzeigen", true))
                    }
                )
        },
        Blocks.actions { action ->
            action.elements(
                listOf(
                    BlockElements.button {
                        it.actionId(ACTION_USER_DATA_REMOVE_ALL_SELECTED)
                        it.value("TODO_value")
                        it.text(BlockCompositions.plainText(":warning: Nutzerdaten löschen", true))
                        it.confirm(
                            BlockCompositions.confirmationDialog { dialog ->
                                dialog.title(BlockCompositions.plainText("Nutzerdaten löschen"))
                                dialog.text(BlockCompositions.plainText("Bist du sicher?\n\n:warning: Diese Aktion kann nicht rückgängig gemacht werden! :warning:", true))
                                dialog.confirm(BlockCompositions.plainText("Alles löschen"))
                                dialog.deny(BlockCompositions.plainText("Abbrechen"))
                            }
                        )
                    }
                )
            )
        }
    )

    companion object {

        const val ACTION_USER_DATA_SHOW_SELECTED = "ACTION_USER_DATA_SHOW_SELECTED"
        const val ACTION_USER_DATA_REMOVE_ALL_SELECTED = "ACTION_USER_DATA_REMOVE_ALL_SELECTED"
    }
}
