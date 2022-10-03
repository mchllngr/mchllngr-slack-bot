package script.base.config.block

import com.slack.api.model.block.Blocks.input
import com.slack.api.model.block.LayoutBlock
import com.slack.api.model.block.composition.BlockCompositions.plainText
import com.slack.api.model.block.composition.PlainTextObject
import com.slack.api.model.block.element.BlockElements.multiUsersSelect
import model.script.ScriptId
import model.user.UserId

data class ConfigBlockMultiUsersSelect(
    private val scriptId: ScriptId,
    private val id: ConfigBlockId,
    private val label: PlainTextObject,
    private val hint: PlainTextObject? = null,
    private val placeholder: PlainTextObject? = null,
    private val initialUsers: List<UserId>? = null,
    private val maxSelectedItems: Int? = null
) : ConfigBlock(scriptId, id) {

    constructor(
        scriptId: ScriptId,
        id: ConfigBlockId,
        label: String,
        hint: String? = null,
        placeholder: String? = null,
        initialUsers: List<UserId>? = null,
        maxSelectedItems: Int? = null
    ) : this(
        scriptId = scriptId,
        id = id,
        label = plainText(label),
        hint = hint?.let { plainText(it) },
        placeholder = placeholder?.let { plainText(it) },
        initialUsers = initialUsers,
        maxSelectedItems = maxSelectedItems
    )

    override fun getLayoutBlock(): LayoutBlock = input { input ->
        input.blockId(blockId)
        input.label(label)
        input.hint(hint)
        input.element(
            multiUsersSelect { multiUsersSelect ->
                multiUsersSelect.actionId(actionId)
                multiUsersSelect.placeholder(placeholder)
                multiUsersSelect.initialUsers(initialUsers?.map { it.id })
                multiUsersSelect.maxSelectedItems(maxSelectedItems)
            }
        )
    }
}
