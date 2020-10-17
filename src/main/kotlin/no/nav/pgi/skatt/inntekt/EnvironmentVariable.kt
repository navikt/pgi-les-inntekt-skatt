package no.nav.pgi.skatt.inntekt

internal fun Map<String, String>.getVal(key: String) =
        this[key] ?: throw MissingEnvironmentVariable("""$key, is not found in environment""")

internal class MissingEnvironmentVariable(message: String) : RuntimeException(message)