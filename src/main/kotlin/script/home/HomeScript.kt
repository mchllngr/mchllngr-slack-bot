package script.home

import com.slack.api.app_backend.events.payload.EventsApiPayload
import com.slack.api.bolt.context.Context
import com.slack.api.bolt.context.builtin.ActionContext
import com.slack.api.bolt.context.builtin.EventContext
import com.slack.api.bolt.context.builtin.ViewSubmissionContext
import com.slack.api.bolt.request.builtin.BlockActionRequest
import com.slack.api.bolt.request.builtin.ViewSubmissionRequest
import com.slack.api.model.block.Blocks.divider
import com.slack.api.model.block.LayoutBlock
import com.slack.api.model.event.AppHomeOpenedEvent
import com.slack.api.model.view.Views.view
import model.blockaction.BlockActionId
import model.script.ScriptId
import model.user.UserId
import model.view.submission.ViewSubmissionId
import script.base.AppHomeOpenedScript
import script.base.BlockActionScript
import script.base.ViewSubmissionScript
import script.home.block.AdminBlocks
import script.home.block.BirthdayBlocks
import script.home.block.BirthdayReminderBlocks
import script.home.block.FooterBlocks
import script.home.block.HelloBlocks
import script.home.block.ScriptConfigBlocks
import script.home.block.TeamBlocks
import script.home.block.UserDataBlocks
import servicelocator.ServiceLocator.Admin
import servicelocator.ServiceLocator.Team
import servicelocator.ServiceLocator.User
import servicelocator.ServiceLocator.scriptHandler
import util.slack.context.getUser
import util.slack.user.SlackUser
import util.time.getZoneDateTimeFromSlackUser
import java.time.ZonedDateTime

class HomeScript : AppHomeOpenedScript, BlockActionScript, ViewSubmissionScript {

    private val helloBlocks by lazy { HelloBlocks() }
    private val teamBlocks by lazy { TeamBlocks(Team.repo) }
    private val birthdayBlocks by lazy { BirthdayBlocks(User.repo) }
    private val birthdayReminderBlocks by lazy { BirthdayReminderBlocks(User.repo) }
    private val userDataBlocks by lazy { UserDataBlocks() }
    private val adminBlocks by lazy { AdminBlocks(Admin.repo) }
    private val scriptConfigBlocks by lazy { ScriptConfigBlocks(Admin.repo, scriptHandler) }
    private val footerBlocks by lazy { FooterBlocks() }

    override val id = ID

    override val blockActionIds = listOf(
        BirthdayBlocks.BLOCK_ACTION_ID_BIRTHDAY_CHANGED,
        BirthdayBlocks.BLOCK_ACTION_ID_BIRTHDAY_INCLUDE_YEAR_CHANGED,
        BirthdayBlocks.BLOCK_ACTION_ID_BIRTHDAY_REMOVED,
        BirthdayReminderBlocks.BLOCK_ACTION_ID_BIRTHDAY_REMINDER_ENABLED_CHANGED,
        BirthdayReminderBlocks.BLOCK_ACTION_ID_BIRTHDAY_REMINDER_ADD_ADDITIONAL_SELECTED,
        UserDataBlocks.BLOCK_ACTION_ID_USER_DATA_SHOW_SELECTED,
        UserDataBlocks.BLOCK_ACTION_ID_USER_DATA_REMOVE_ALL_SELECTED,
        AdminBlocks.BLOCK_ACTION_ID_BOT_ENABLED_SELECTED,
        ScriptConfigBlocks.BLOCK_ACTION_ID_SCRIPT_ENABLED_SELECTED,
        ScriptConfigBlocks.BLOCK_ACTION_SCRIPT_CONFIG_ID
    )

    override val viewSubmissionIds = listOf(
        ScriptConfigBlocks.VIEW_SUBMISSION_SCRIPT_CONFIG_ID
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
        ctx.getUser(request)?.let { user ->
            when (blockActionId) {
                BirthdayBlocks.BLOCK_ACTION_ID_BIRTHDAY_CHANGED -> birthdayBlocks.onActionUserBirthdayChanged(user, request)
                BirthdayBlocks.BLOCK_ACTION_ID_BIRTHDAY_INCLUDE_YEAR_CHANGED -> birthdayBlocks.onActionBirthdayIncludeYearChanged(user, request)
                BirthdayReminderBlocks.BLOCK_ACTION_ID_BIRTHDAY_REMINDER_ENABLED_CHANGED -> birthdayReminderBlocks.onActionBirthdayReminderEnabledChanged(user, request)
                AdminBlocks.BLOCK_ACTION_ID_BOT_ENABLED_SELECTED -> adminBlocks.onActionBotEnabledSelected(user, request)
                ScriptConfigBlocks.BLOCK_ACTION_ID_SCRIPT_ENABLED_SELECTED -> scriptConfigBlocks.onActionScriptEnabledSelected(user, request)
                ScriptConfigBlocks.BLOCK_ACTION_SCRIPT_CONFIG_ID -> scriptConfigBlocks.onActionShowScriptConfig(user, request, ctx)
                else -> Unit
            }
        }

        ctx.updateHomeView(
            UserId(request.payload.user.id),
            request.payload.view.hash
        )
    }

    override fun onViewSubmissionEvent(
        viewSubmissionId: ViewSubmissionId,
        request: ViewSubmissionRequest,
        ctx: ViewSubmissionContext
    ) {
        ctx.getUser(request)?.let { user ->
            when (viewSubmissionId) {
                ScriptConfigBlocks.VIEW_SUBMISSION_SCRIPT_CONFIG_ID -> scriptConfigBlocks.onViewSubmissionEvent(user, request)
                else -> Unit
            }
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
        slackUser: SlackUser?
    ): List<LayoutBlock> {
        val now: ZonedDateTime = getZoneDateTimeFromSlackUser(slackUser)
        val user = slackUser?.id?.let { User.repo.select(UserId(it)) }

        return buildList {
            addAll(helloBlocks.createBlocks(slackUser))
            add(divider())

            slackUser?.let {
                addAll(teamBlocks.createBlocks(it))
                add(divider())
            }

            user?.let {
                addAll(birthdayBlocks.createBlocks(it))
                add(divider())

                addAll(birthdayReminderBlocks.createBlocks(it))
                add(divider())
            }

            // #9 Build home/userData
//            addAll(userDataBlocks.createBlocks())
//            add(divider())

            slackUser?.let { adminBlocks.createBlocks(slackUser) }?.let { blocks ->
                addAll(blocks)
                add(divider())
            }

            slackUser?.let { scriptConfigBlocks.createBlocks(slackUser) }?.let { blocks ->
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
