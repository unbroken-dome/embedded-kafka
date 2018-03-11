package org.unbrokendome.embedded.support


/**
 * Parses properties from a list of strings where the keys and values are separated by an equals sign,
 * similar to the Java properties file syntax.
 *
 * Empty and blank strings in the input are ignored. Input items that do not contain an equals sign will lead to
 * an entry that has the whole input string as its key and an empty string as its value.
 *
 * @param propertiesAsList a list of strings in `key=value` syntax, where each item corresponds to one entry in
 *                         the properties map. Whitespace is trimmed at the beginning and end, and around the
 *                         equals sign.
 * @return a [Map] with String keys and values
 */
fun parsePropertiesFromList(propertiesAsList: Iterable<String>): Map<String, String> =
        propertiesAsList
                .filter { it.isNotBlank() }
                .associate {
                    it.splitAt('=', trim = true)
                }
