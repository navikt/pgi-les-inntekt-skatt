package no.nav.pgi.skatt.inntekt.skatt

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

class RateLimitTest {

    @Test
    fun test() {
        var count = 0
        val rate = 100
        val timeInterval = 1.seconds

        val limiter = RateLimit(rate = rate, timeInterval = timeInterval)

        var elapsed = 0L

        while (elapsed <= timeInterval.inWholeMilliseconds) {
            val start = System.currentTimeMillis()
            limiter.limit { count++ }
            elapsed += System.currentTimeMillis() - start
        }

        assertTrue(count <= rate)
    }
}