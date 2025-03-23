package app.cash.paparazzi.plugin.test

import androidx.compose.ui.platform.ComposeView
import app.cash.paparazzi.junit4.PaparazziRule
import org.junit.AfterClass
import org.junit.Rule
import org.junit.Test
import java.lang.ref.WeakReference

class ComposeReferenceLeakTest {
  @get:Rule
  val paparazzi = PaparazziRule()

  @Test
  fun cleansUpComposeReferences() {
    composeView = ComposeView(paparazzi.context).apply {
      setContent {
        HelloPaparazzi()
      }

      paparazzi.snapshot(this)
    }
  }

  companion object {
    private var composeView: ComposeView? = null

    @AfterClass
    @JvmStatic
    fun teardown() {
      assert(composeView != null)
      val weakComposeView = WeakReference(composeView)

      composeView = null
      System.gc()

      assert(weakComposeView.get() == null)
    }
  }
}
