package factory

import adapter.LocalDateColumnAdapter
import adapter.TeamIdColumnAdapter
import adapter.UserIdColumnAdapter
import adapter.UserListColumnAdapter
import com.squareup.sqldelight.db.SqlDriver
import db.Database
import db.Team
import db.User

object DatabaseFactory {

    fun create(driver: SqlDriver): Database = Database(
        driver = driver,
        teamAdapter = Team.Adapter(
            idAdapter = TeamIdColumnAdapter(),
            adminAdapter = UserListColumnAdapter(),
            userAdapter = UserListColumnAdapter()
        ),
        userAdapter = User.Adapter(
            idAdapter = UserIdColumnAdapter(),
            birthdateAdapter = LocalDateColumnAdapter()
        )
    )
}
