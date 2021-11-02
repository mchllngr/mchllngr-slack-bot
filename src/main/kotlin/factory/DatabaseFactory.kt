package factory

import adapter.LocalDateColumnAdapter
import adapter.TeamIdColumnAdapter
import adapter.UserIdColumnAdapter
import com.squareup.sqldelight.db.SqlDriver
import db.Database
import db.Team
import db.Team_admin
import db.Team_user
import db.User

object DatabaseFactory {

    fun create(driver: SqlDriver): Database = Database(
        driver = driver,
        teamAdapter = Team.Adapter(
            idAdapter = TeamIdColumnAdapter(),
        ),
        userAdapter = User.Adapter(
            idAdapter = UserIdColumnAdapter(),
            birthdateAdapter = LocalDateColumnAdapter()
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
