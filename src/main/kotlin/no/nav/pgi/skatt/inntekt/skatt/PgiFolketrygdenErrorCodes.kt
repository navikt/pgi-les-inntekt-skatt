package no.nav.pgi.skatt.inntekt.skatt

/**
 * @see <a href="https://skatteetaten.github.io/api-dokumentasjon/api/pgi_folketrygden?tab=Feilkoder">Feilkoder</>
 */
internal class PgiFolketrygdenErrorCodes {
    companion object {
        internal val pgiFolketrygdenErrorCodes
            get() = listOf(
                "PGIF-001",
                "PGIF-002",
                "PGIF-003",
                "PGIF-004",
                "PGIF-005",
                "PGIF-006",
                "PGIF-007",
                "PGIF-008",
                "PGIF-009",
            ) + skattCommonErrorCodes

        private val skattCommonErrorCodes = listOf(
            "DAS-001",
            "DAS-002",
            "DAS-003",
            "DAS-004",
            "DAS-005",
            "DAS-006",
            "DAS-007",
            "DAS-008"
        )
    }
}

internal infix fun String.getFirstMatch(codes: List<String>): String? = codes.find { contains(it) }
internal infix fun String.containOneOf(codes: List<String>): Boolean = this.getFirstMatch(codes) != null