package app.cash.paparazzi.plugin.test

import androidx.compose.material.Text
import app.cash.paparazzi.junit4.PaparazziRule
import org.junit.Rule
import org.junit.Test

class SimpleTest {
  @get:Rule
  val paparazzi = PaparazziRule(maxPercentDifference = 0.0)

  @Test
  fun compose() {
    paparazzi.snapshot {
      Text("Hello Paparazzi!")
    }
  }
}
