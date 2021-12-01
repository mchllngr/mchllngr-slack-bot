package adapter

import com.squareup.sqldelight.ColumnAdapter
import model.script.ScriptId

class ScriptIdColumnAdapter : ColumnAdapter<ScriptId, String> {

    override fun decode(databaseValue: String): ScriptId = ScriptId(databaseValue)

    override fun encode(value: ScriptId): String = value.id
}
