package script.base

import model.script.ScriptId

interface Script {

    /**
     * Used for allowing admins to enable/disable a script from the bot-home.
     * Should provide an indicator of what this script does, because this is what the admins see.
     *
     * MUST be unique.
     */
    val id: ScriptId
}
