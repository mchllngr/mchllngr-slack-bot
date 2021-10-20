package adapter

import com.squareup.sqldelight.ColumnAdapter
import java.time.LocalDate

class LocalDateColumnAdapter : ColumnAdapter<LocalDate, String> {

    override fun decode(databaseValue: String): LocalDate = LocalDate.parse(databaseValue)

    override fun encode(value: LocalDate): String = value.toString()
}
