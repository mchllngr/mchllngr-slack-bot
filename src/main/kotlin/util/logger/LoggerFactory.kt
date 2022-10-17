package util.logger

import org.slf4j.Logger
import org.slf4j.LoggerFactory

inline fun <reified T : Any> getLogger(): Logger = LoggerFactory.getLogger(T::class.java)

@Suppress("UnusedReceiverParameter")
inline fun <reified T : Any> T.getLogger(): Logger = LoggerFactory.getLogger(T::class.java)
