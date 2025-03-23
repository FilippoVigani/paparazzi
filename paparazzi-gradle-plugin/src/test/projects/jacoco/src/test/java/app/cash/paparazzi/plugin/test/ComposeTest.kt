package app.cash.paparazzi.plugin.test

import app.cash.paparazzi.junit4.PaparazziRule
import org.junit.Rule
import org.junit.Test

class ComposeTest {
  @get:Rule
  val paparazzi = PaparazziRule()

  @Test
  fun compose() {
    paparazzi.snapshot {
      HelloPaparazzi()
    }
  }
}
