package script.base

import com.slack.api.bolt.context.builtin.ViewSubmissionContext
import com.slack.api.bolt.request.builtin.ViewSubmissionRequest
import model.view.submission.ViewSubmissionId

interface ViewSubmissionScript : Script {

    val viewSubmissionIds: List<ViewSubmissionId>

    fun onViewSubmissionEvent(
        viewSubmissionId: ViewSubmissionId,
        request: ViewSubmissionRequest,
        ctx: ViewSubmissionContext
    )
}
