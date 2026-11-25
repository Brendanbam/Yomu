package tachiyomi.domain.source.model

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class PinsTest {

    @Test
    fun `removing the actual source flag from the last used copy preserves pin status`() {
        (Pins.unpinned - Pin.Actual) shouldBe Pins.unpinned
        (Pins.pinned - Pin.Actual) shouldBe Pins(Pin.Pinned)
        Pins {
            +Pin.Pinned
            -Pin.Actual
        } shouldBe Pins(Pin.Pinned)
    }
}
