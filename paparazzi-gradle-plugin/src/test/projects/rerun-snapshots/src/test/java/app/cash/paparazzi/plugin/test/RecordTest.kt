package app.cash.paparazzi.plugin.test

import android.view.View
import app.cash.paparazzi.junit4.PaparazziRule
import org.junit.Rule
import org.junit.Test

class RecordTest {
  @get:Rule
  val paparazzi = PaparazziRule()

  @Test
  fun record() {
    paparazzi.snapshot(View(paparazzi.context))
  }
}
