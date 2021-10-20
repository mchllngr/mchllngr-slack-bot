package adapter

import com.squareup.sqldelight.ColumnAdapter
import model.UserId

class UserListColumnAdapter : ColumnAdapter<List<UserId>, String> {

    override fun decode(databaseValue: String): List<UserId> =
        if (databaseValue.isBlank()) {
            listOf()
        } else {
            databaseValue
                .split(",")
                .map { UserId(it) }
        }

    override fun encode(value: List<UserId>): String = value.joinToString(separator = ",") { it.id }
}
