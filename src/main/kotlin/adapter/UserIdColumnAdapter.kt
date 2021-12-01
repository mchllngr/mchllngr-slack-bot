package adapter

import com.squareup.sqldelight.ColumnAdapter
import model.user.UserId

class UserIdColumnAdapter : ColumnAdapter<UserId, String> {

    override fun decode(databaseValue: String): UserId = UserId(databaseValue)

    override fun encode(value: UserId): String = value.id
}
