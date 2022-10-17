// ktlint-disable filename

package util.charsequence

fun <T : CharSequence> Iterable<T>.joinToString(
    separator: CharSequence,
    lastSeparator: CharSequence
): String {
    if (count() <= 1) return joinToString(separator = separator)

    val list = toMutableList()
    val last = list.removeLast()
    return list.joinToString(separator = separator) + lastSeparator + last
}
