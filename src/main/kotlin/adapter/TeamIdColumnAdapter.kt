package adapter

import com.squareup.sqldelight.ColumnAdapter
import model.TeamId

class TeamIdColumnAdapter : ColumnAdapter<TeamId, String> {

    override fun decode(databaseValue: String): TeamId = TeamId(databaseValue)

    override fun encode(value: TeamId): String = value.id
}
