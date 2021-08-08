package exception

class MandatoryEnvironmentVariableMissingException(name: String) : BotException("Environment variable '$name' is missing. See README.md for more information.")
