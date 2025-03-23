package app.cash.paparazzi.plugin.test

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.junit4.PaparazziRule
import org.junit.Rule
import org.junit.Test

class ComposeTest {
  @get:Rule
  val paparazzi = PaparazziRule(
    deviceConfig = DeviceConfig.WEAR_OS_SMALL_ROUND,
    theme = "android:ThemeOverlay.Material.Dark"
  )

  @Test
  fun compose() {
    paparazzi.snapshot {
      HelloPaparazzi()
    }
  }
}
