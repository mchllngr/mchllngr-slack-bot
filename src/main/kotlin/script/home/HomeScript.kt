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
import script.base.AppHomeOpenedScript
import script.base.BlockActionScript
import script.home.block.BirthdayBlocks
import script.home.block.BirthdayReminderBlocks
import script.home.block.FooterBlocks
import script.home.block.HelloBlocks
import script.home.block.TeamBlocks
import script.home.block.UserDataBlocks
import util.context.getUser
import java.time.ZonedDateTime

@OptIn(ExperimentalStdlibApi::class)
class HomeScript : AppHomeOpenedScript, BlockActionScript {

    private val helloBlocks by lazy { HelloBlocks() }
    private val teamBlocks by lazy { TeamBlocks() }
    private val birthdayBlocks by lazy { BirthdayBlocks() }
    private val birthdayReminderBlocks by lazy { BirthdayReminderBlocks() }
    private val userDataBlocks by lazy { UserDataBlocks() }
    private val footerBlocks by lazy { FooterBlocks() }

    override val blockActionIds = listOf(
        BirthdayBlocks.ACTION_USER_BIRTHDAY_CHANGED,
        BirthdayBlocks.ACTION_USER_BIRTHDAY_INCLUDE_YEAR_CHANGED,
        BirthdayBlocks.ACTION_USER_BIRTHDAY_REMOVED,
        BirthdayReminderBlocks.ACTION_BIRTHDAY_REMINDER_ENABLED_CHANGED,
        BirthdayReminderBlocks.ACTION_BIRTHDAY_REMINDER_ADD_ADDITIONAL_SELECTED,
        UserDataBlocks.ACTION_USER_DATA_SHOW_SELECTED,
        UserDataBlocks.ACTION_USER_DATA_REMOVE_ALL_SELECTED
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
            addAll(helloBlocks.createBlocks(user))
            add(divider())

            addAll(teamBlocks.createBlocks())
            add(divider())

            addAll(birthdayBlocks.createBlocks())
            add(divider())

            addAll(birthdayReminderBlocks.createBlocks())
            add(divider())

            addAll(userDataBlocks.createBlocks())
            add(divider())

            addAll(footerBlocks.createBlocks(now))
        }
    }

    companion object {

        private const val KEY_HOME = "home"
    }
}
