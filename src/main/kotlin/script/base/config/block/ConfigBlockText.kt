package script.base.config.block

import com.slack.api.model.block.Blocks.input
import com.slack.api.model.block.LayoutBlock
import com.slack.api.model.block.composition.BlockCompositions.plainText
import com.slack.api.model.block.composition.PlainTextObject
import com.slack.api.model.block.element.BlockElements.plainTextInput
import model.script.ScriptId

data class ConfigBlockText(
    private val scriptId: ScriptId,
    private val id: ConfigBlockId,
    private val label: PlainTextObject,
    private val optional: Boolean,
    private val hint: PlainTextObject? = null,
    private val placeholder: PlainTextObject? = null,
    private val initialValue: String? = null,
    private val multiline: Boolean = false,
    private val minLength: Int? = null,
    private val maxLength: Int? = null
) : ConfigBlock(scriptId, id) {

    constructor(
        scriptId: ScriptId,
        id: ConfigBlockId,
        label: String,
        optional: Boolean,
        hint: String? = null,
        placeholder: String? = null,
        initialValue: String? = null,
        multiline: Boolean = false,
        minLength: Int? = null,
        maxLength: Int? = null
    ) : this(
        scriptId = scriptId,
        id = id,
        label = plainText(label),
        optional = optional,
        hint = hint?.let { plainText(it) },
        placeholder = placeholder?.let { plainText(it) },
        initialValue = initialValue,
        multiline = multiline,
        minLength = minLength,
        maxLength = maxLength
    )

    override fun getLayoutBlock(): LayoutBlock = input { input ->
        input.blockId(blockId)
        input.label(label)
        input.hint(hint)
        input.optional(optional)
        input.element(
            plainTextInput { textInput ->
                textInput.actionId(actionId)
                textInput.placeholder(placeholder)
                textInput.initialValue(initialValue)
                textInput.multiline(multiline)
                textInput.minLength(minLength)
                textInput.maxLength(maxLength)
            }
        )
    }
}
