package adapter

import com.squareup.sqldelight.ColumnAdapter
import model.team.TeamUserId

class TeamUserIdColumnAdapter : ColumnAdapter<TeamUserId, Int> {

    override fun decode(databaseValue: Int): TeamUserId = TeamUserId(databaseValue)

    override fun encode(value: TeamUserId): Int = value.id
}
