package no.nav.pgi.skatt.inntekt

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag

class Counters(val meterRegistry: MeterRegistry) {

    private val hendelserToinntektProcessedTotal = meterRegistry.counter(
        "pgi_hendelse_to_inntekt_processed_total",
        listOf(Tag.of("help", "Antall hendelser hvor det er hentet inntekt totalt"))
    )

    private fun hendelserToinntektProcessedByYear(year: String): Counter {
        return meterRegistry.counter(
            "pgi_hendelse_to_inntekt_processed_by_year",
            listOf(
                Tag.of("year", year),
                Tag.of("help", "Antall hendelser  hvor det er hentet inntekt per år")
            )
        )
    }

    private fun pgiLesInntektSkattResponseCounter(statusCode: String): Counter {
        return meterRegistry.counter(
            "pgi_les_inntekt_skatt_response_counter",
            listOf(
                Tag.of("statusCode", statusCode),
                Tag.of("help", "Count response status codes from popp")
            )
        )
    }


    fun increaseHendelserToinntektProcessedTotal() {
        hendelserToinntektProcessedTotal.increment()
    }

    fun increaseHendelserToInntektProcessedByYear(year: String) {
        hendelserToinntektProcessedByYear(year)
    }

    fun increasePgiLesInntektSkattResponseCounter(statusCode: String) {
        pgiLesInntektSkattResponseCounter(statusCode)
    }
}