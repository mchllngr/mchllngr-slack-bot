package script.home

import com.slack.api.app_backend.events.payload.EventsApiPayload
import com.slack.api.bolt.context.Context
import com.slack.api.bolt.context.builtin.ActionContext
import com.slack.api.bolt.context.builtin.EventContext
import com.slack.api.bolt.request.builtin.BlockActionRequest
import com.slack.api.model.User
import com.slack.api.model.block.Blocks.divider
import com.slack.api.model.block.LayoutBlock
import com.slack.api.model.event.AppHomeOpenedEvent
import com.slack.api.model.view.Views.view
import model.blockaction.BlockActionId
import model.script.ScriptId
import model.user.UserId
import script.base.AppHomeOpenedScript
import script.base.BlockActionScript
import script.home.block.AdminBlocks
import script.home.block.BirthdayBlocks
import script.home.block.BirthdayReminderBlocks
import script.home.block.FooterBlocks
import script.home.block.HelloBlocks
import script.home.block.TeamBlocks
import script.home.block.UserDataBlocks
import servicelocator.ServiceLocator.adminRepo
import servicelocator.ServiceLocator.scriptHandler
import servicelocator.ServiceLocator.teamRepo
import util.slack.context.getUser
import java.time.ZonedDateTime

@OptIn(ExperimentalStdlibApi::class)
class HomeScript : AppHomeOpenedScript, BlockActionScript {

    private val helloBlocks by lazy { HelloBlocks() }
    private val teamBlocks by lazy { TeamBlocks(teamRepo) }
    private val birthdayBlocks by lazy { BirthdayBlocks() }
    private val birthdayReminderBlocks by lazy { BirthdayReminderBlocks() }
    private val userDataBlocks by lazy { UserDataBlocks() }
    private val adminBlocks by lazy { AdminBlocks(adminRepo, scriptHandler) }
    private val footerBlocks by lazy { FooterBlocks() }

    override val id = ID

    override val blockActionIds = listOf(
        BirthdayBlocks.BLOCK_ACTION_ID_USER_BIRTHDAY_CHANGED,
        BirthdayBlocks.BLOCK_ACTION_ID_USER_BIRTHDAY_INCLUDE_YEAR_CHANGED,
        BirthdayBlocks.BLOCK_ACTION_ID_USER_BIRTHDAY_REMOVED,
        BirthdayReminderBlocks.BLOCK_ACTION_ID_BIRTHDAY_REMINDER_ENABLED_CHANGED,
        BirthdayReminderBlocks.BLOCK_ACTION_ID_BIRTHDAY_REMINDER_ADD_ADDITIONAL_SELECTED,
        UserDataBlocks.BLOCK_ACTION_ID_USER_DATA_SHOW_SELECTED,
        UserDataBlocks.BLOCK_ACTION_ID_USER_DATA_REMOVE_ALL_SELECTED,
        AdminBlocks.BLOCK_ACTION_ID_BOT_ENABLED_SELECTED,
        AdminBlocks.BLOCK_ACTION_ID_SCRIPT_ENABLED_SELECTED
    )

    override fun onAppHomeOpenedEvent(
        event: EventsApiPayload<AppHomeOpenedEvent>,
        ctx: EventContext
    ) {
        if (event.event.tab != KEY_HOME) return

        ctx.updateHomeView(
            UserId(event.event.user),
            event.event.view?.hash.orEmpty()
        )
    }

    override fun onBlockActionEvent(
        blockActionId: BlockActionId,
        request: BlockActionRequest,
        ctx: ActionContext
    ) {
        when (blockActionId) {
            AdminBlocks.BLOCK_ACTION_ID_BOT_ENABLED_SELECTED -> adminBlocks.onActionBotEnabledSelected(request, ctx)
            AdminBlocks.BLOCK_ACTION_ID_SCRIPT_ENABLED_SELECTED -> adminBlocks.onActionScriptEnabledSelected(request, ctx)
        }

        ctx.updateHomeView(
            UserId(request.payload.user.id),
            request.payload.view.hash
        )
    }

    private fun Context.updateHomeView(
        userId: UserId,
        viewHash: String
    ) {
        val user = getUser(userId)

        client().viewsPublish { requestBuilder ->
            requestBuilder
                .userId(userId.id)
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

    private fun Context.createHomeBlocks(
        user: User?
    ): List<LayoutBlock> {
        val now: ZonedDateTime = ZonedDateTime.now()

        return buildList {
            addAll(helloBlocks.createBlocks(user))
            add(divider())

            addAll(teamBlocks.createBlocks(user))
            add(divider())

            addAll(birthdayBlocks.createBlocks())
            add(divider())

            addAll(birthdayReminderBlocks.createBlocks())
            add(divider())

            // #9 Build home/userData
//            addAll(userDataBlocks.createBlocks())
//            add(divider())

            adminBlocks.createBlocks(user)?.let { blocks ->
                addAll(blocks)
                add(divider())
            }

            addAll(footerBlocks.createBlocks(now, this@createHomeBlocks))
        }
    }

    companion object {

        private const val KEY_HOME = "home"

        val ID = ScriptId("HOME")
    }
}
