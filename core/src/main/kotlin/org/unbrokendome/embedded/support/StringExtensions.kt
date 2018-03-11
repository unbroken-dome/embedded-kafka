package org.unbrokendome.embedded.support


fun String.splitAt(delimiter: Char, ignoreCase: Boolean = false, trim: Boolean = false,
                   missingDelimiterValue: String = ""): Pair<String, String> {
    val delimiterIndex = indexOf(delimiter, ignoreCase = ignoreCase)
    val pair = if (delimiterIndex >= 0) {
        substring(0, delimiterIndex) to substring(delimiterIndex + 1)
    } else {
        this to missingDelimiterValue
    }
    return if (trim) {
        pair.first.trim() to pair.second.trim()
    } else {
        pair
    }
}


