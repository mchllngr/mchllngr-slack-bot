package util.slack.block

import com.slack.api.model.block.Blocks.header
import com.slack.api.model.block.Blocks.section
import com.slack.api.model.block.HeaderBlock
import com.slack.api.model.block.SectionBlock
import com.slack.api.model.block.composition.BlockCompositions.markdownText
import com.slack.api.model.block.composition.BlockCompositions.plainText

fun headerSection(
    text: String,
    emoji: Boolean = false
): HeaderBlock = header { header ->
    header.text(
        plainText(text, emoji)
    )
}

fun plainTextSection(
    text: String,
    emoji: Boolean = false
): SectionBlock = section { section ->
    section.text(
        plainText(text, emoji)
    )
}

fun markdownSection(
    text: String,
    verbatim: Boolean = false
): SectionBlock = section { section ->
    section.text(
        markdownText {
            it.text(text)
            it.verbatim(verbatim)
        }
    )
}
