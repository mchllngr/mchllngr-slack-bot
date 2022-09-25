package script.base.config

import com.slack.api.model.block.Blocks.input
import com.slack.api.model.block.LayoutBlock
import com.slack.api.model.block.composition.BlockCompositions.plainText
import com.slack.api.model.block.composition.PlainTextObject
import com.slack.api.model.block.element.BlockElements.plainTextInput
import com.slack.api.model.block.composition.DispatchActionConfig as SlackDispatchActionConfig

data class ConfigBlockText(
    val scriptId: String,
    val id: String,
    val label: PlainTextObject,
    val hint: PlainTextObject? = null,
    val placeholder: PlainTextObject? = null,
    val initialValue: String? = null,
    val multiline: Boolean = false,
    val minLength: Int? = null,
    val maxLength: Int? = null,
    val dispatchActionConfig: DispatchActionConfig = DispatchActionConfig.ON_ENTER_PRESSED,
    val onChange: (String) -> Unit
) : ConfigBlock {

    override val actionId = "${scriptId}_text_$id"

    constructor(
        scriptId: String,
        id: String,
        label: String,
        hint: String? = null,
        placeholder: String? = null,
        initialValue: String? = null,
        multiline: Boolean = false,
        minLength: Int? = null,
        maxLength: Int? = null,
        dispatchActionConfig: DispatchActionConfig = DispatchActionConfig.ON_ENTER_PRESSED,
        onChange: (String) -> Unit
    ) : this(
        scriptId = scriptId,
        id = id,
        label = plainText(label),
        hint = hint?.let { plainText(it) },
        placeholder = placeholder?.let { plainText(it) },
        initialValue = initialValue,
        multiline = multiline,
        minLength = minLength,
        maxLength = maxLength,
        dispatchActionConfig = dispatchActionConfig,
        onChange = onChange,
    )

    override fun getLayoutBlock(): LayoutBlock = input { input ->
        input.blockId(actionId)
        input.label(label)
        input.hint(hint)
        input.dispatchAction(true)
        input.element(
            plainTextInput { textInput ->
                textInput.actionId(actionId)
                textInput.placeholder(placeholder)
                textInput.initialValue(initialValue)
                textInput.multiline(multiline)
                textInput.minLength(minLength)
                textInput.maxLength(maxLength)
                textInput.dispatchActionConfig(dispatchActionConfig.config)
            }
        )
    }

    enum class DispatchActionConfig(val config: SlackDispatchActionConfig) {
        ON_ENTER_PRESSED(SlackDispatchActionConfig(listOf("on_enter_pressed"))),
        ON_CHARACTER_ENTERED(SlackDispatchActionConfig(listOf("on_character_entered"))),
        ON_ENTER_PRESSED_OR_CHARACTER_ENTERED(SlackDispatchActionConfig(listOf("on_enter_pressed", "on_character_entered")))
    }
}
