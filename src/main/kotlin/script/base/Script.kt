package script.base

interface Script {

    /**
     * Used for allowing admins to enable/disable a script from the bot-home.
     * Should provide an indicator of what this script does, because this is what the admins see.
     *
     * MUST be a unique name.
     */
    val name: String
}
