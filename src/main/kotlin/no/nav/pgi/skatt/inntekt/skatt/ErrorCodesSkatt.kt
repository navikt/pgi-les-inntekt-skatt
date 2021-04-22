package no.nav.pgi.skatt.inntekt.skatt


internal class ErrorCodesSkatt {
    companion object {
        internal val skattAllErrorCodes get() = skattDiscardErrorCodes + throwExceptionSkattErrorCodes
        internal val skattDiscardErrorCodes = listOf("PGIF-005", "PGIF-006", "PGIF-007", "PGIF-008", "PGIF-009")
        private val throwExceptionSkattErrorCodes = listOf("DAS-001", "DAS-002", "DAS-003", "DAS-004", "DAS-005", "DAS-006", "DAS-007", "DAS-008")
    }
}

internal infix fun String.getFirstMatch(codes: List<String>): String? = codes.find { contains(it) }
internal infix fun String.containOneOf(codes: List<String>): Boolean = this.getFirstMatch(codes) != null