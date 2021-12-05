package adapter

import com.squareup.sqldelight.ColumnAdapter
import model.team.TeamId

class TeamIdColumnAdapter : ColumnAdapter<TeamId, Int> {

    override fun decode(databaseValue: Int): TeamId = TeamId(databaseValue)

    override fun encode(value: TeamId): Int = value.id
}
