package model.script

/**
 * Used to identify a script.
 * Should provide an indicator of what this script does, because this is what the admins see.
 */
@JvmInline
value class ScriptId(val id: String)
