package factory

import adapter.LocalDateColumnAdapter
import adapter.ScriptIdColumnAdapter
import adapter.TeamIdColumnAdapter
import adapter.UserIdColumnAdapter
import com.squareup.sqldelight.db.SqlDriver
import db.Database
import db.Script
import db.Team
import db.Team_admin
import db.Team_user
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
        team_adminAdapter = Team_admin.Adapter(
            teamIdAdapter = TeamIdColumnAdapter(),
            userIdAdapter = UserIdColumnAdapter(),
        ),
        team_userAdapter = Team_user.Adapter(
            teamIdAdapter = TeamIdColumnAdapter(),
            userIdAdapter = UserIdColumnAdapter(),
        )
    )
}
