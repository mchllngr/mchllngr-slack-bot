package script.home.block

import util.slack.block.markdownSection
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class FooterBlocks {

    private val dateTimeFooterFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy 'um' HH:mm:ss 'Uhr'")

    fun createBlocks(now: ZonedDateTime) = listOf(
        markdownSection(":information_source: Fragen und Verbesserungsvorschläge zum Bot bitte an Michael Langer"),
        markdownSection(":clock3: Seite zuletzt aktualisiert am ${now.format(dateTimeFooterFormat)}")
    )
}
