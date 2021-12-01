package script.home.block

import com.slack.api.bolt.context.Context
import com.slack.api.model.block.Blocks.context
import com.slack.api.model.block.ContextBlock
import com.slack.api.model.block.composition.BlockCompositions.markdownText
import servicelocator.ServiceLocator.config
import util.charsequence.joinToString
import util.slack.context.userExists
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class FooterBlocks {

    private val dateTimeFooterFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy 'um' HH:mm:ss 'Uhr'")

    fun createBlocks(
        now: ZonedDateTime,
        ctx: Context
    ): List<ContextBlock> {
        val admins = config.admin.ids
            .filter { ctx.userExists(it) }
            .map { "<@${it.id}>" }
            .joinToString(separator = ", ", lastSeparator = " oder ")

        val helpText = buildString {
            append(":information_source: Für Fragen und Verbesserungsvorschläge zum Bot erstelle bitte ein ")
            append("<https://github.com/mchllngr/mchllngr-slack-bot/issues|Issue>")
            if (admins.isNotBlank()) {
                append(" oder wende dich an ")
                append(admins)
            }
            append(".")
        }

        return listOf(
            context(
                listOf(
                    markdownText(helpText)
                )
            ),
            context(
                listOf(
                    markdownText(":clock3: Seite zuletzt aktualisiert am ${now.format(dateTimeFooterFormat)}.")
                )
            )
        )
    }
}
