package app.cash.paparazzi.plugin.test

import android.view.View
import app.cash.paparazzi.junit4.Paparazzi
import org.junit.Rule
import org.junit.Test

class RecordTest {
  @get:Rule
  val paparazzi = Paparazzi()

  @Test
  fun record() {
    paparazzi.snapshot(View(paparazzi.context))
  }
}
