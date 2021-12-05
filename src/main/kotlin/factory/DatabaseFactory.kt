package factory

import adapter.LocalDateColumnAdapter
import adapter.ScriptIdColumnAdapter
import adapter.TeamIdColumnAdapter
import adapter.TeamUserIdColumnAdapter
import adapter.UserIdColumnAdapter
import com.squareup.sqldelight.db.SqlDriver
import db.Database
import db.Script
import db.Team
import db.TeamUser
import db.User

object DatabaseFactory {

    fun create(driver: SqlDriver): Database = Database(
        driver = driver,
        scriptAdapter = Script.Adapter(
            idAdapter = ScriptIdColumnAdapter()
        ),
        userAdapter = User.Adapter(
            idAdapter = UserIdColumnAdapter(),
            birthdateAdapter = LocalDateColumnAdapter()
        ),
        teamAdapter = Team.Adapter(
            idAdapter = TeamIdColumnAdapter(),
        ),
        teamUserAdapter = TeamUser.Adapter(
            idAdapter = TeamUserIdColumnAdapter(),
            teamIdAdapter = TeamIdColumnAdapter(),
            userIdAdapter = UserIdColumnAdapter(),
        )
    )
}
