package app.cash.paparazzi

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.LayoutRes
import androidx.compose.runtime.Composable
import com.android.ide.common.rendering.api.SessionParams.RenderingMode
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.util.Date

/**
 * JUnit 5 extension for Paparazzi screenshot testing.
 *
 * This is a direct port of the JUnit 4 TestRule implementation,
 * adapted to work as a JUnit 5 extension.
 */
public class PaparazziExtension @JvmOverloads constructor(
  private val environment: Environment = detectEnvironment(),
  private val deviceConfig: DeviceConfig = DeviceConfig.NEXUS_5,
  private val theme: String = "android:Theme.Material.NoActionBar.Fullscreen",
  private val renderingMode: RenderingMode = RenderingMode.NORMAL,
  private val appCompatEnabled: Boolean = true,
  private val maxPercentDifference: Double = 0.1,
  private val snapshotHandler: SnapshotHandler = determineHandler(maxPercentDifference),
  private val renderExtensions: Set<RenderExtension> = setOf(),
  private val supportsRtl: Boolean = false,
  private val showSystemUi: Boolean = false,
  private val validateAccessibility: Boolean = false,
  private val useDeviceResolution: Boolean = false,
) : BeforeEachCallback, AfterEachCallback {

  private lateinit var sdk: PaparazziSdk
  private lateinit var frameHandler: SnapshotHandler.FrameHandler
  private var testName: TestName? = null

  public val layoutInflater: LayoutInflater
    get() = sdk.layoutInflater

  public val resources: Resources
    get() = sdk.resources

  public val context: Context
    get() = sdk.context

  override fun beforeEach(context: ExtensionContext) {
    sdk = PaparazziSdk(
      environment = environment,
      deviceConfig = deviceConfig,
      theme = theme,
      renderingMode = renderingMode,
      appCompatEnabled = appCompatEnabled,
      renderExtensions = renderExtensions,
      supportsRtl = supportsRtl,
      showSystemUi = showSystemUi,
      validateAccessibility = validateAccessibility,
      onNewFrame = { frameHandler.handle(it) },
      useDeviceResolution = useDeviceResolution,
    )
    sdk.setup()
    prepare(context)
  }

  override fun afterEach(context: ExtensionContext) {
    close()
  }

  public fun prepare(context: ExtensionContext) {
    testName = context.toTestName()
    sdk.prepare()
  }

  public fun close() {
    testName = null
    sdk.teardown()
    snapshotHandler.close()
  }

  public fun <V : View> inflate(@LayoutRes layoutId: Int): V = sdk.inflate(layoutId)

  @JvmOverloads
  public fun snapshot(name: String? = null, composable: @Composable () -> Unit) {
    createFrameHandler(name).use { handler ->
      frameHandler = handler
      sdk.snapshot(composable)
    }
  }

  @JvmOverloads
  public fun snapshot(view: View, name: String? = null, offsetMillis: Long = 0L) {
    createFrameHandler(name).use { handler ->
      frameHandler = handler
      sdk.snapshot(view, offsetMillis)
    }
  }

  @JvmOverloads
  public fun gif(
    view: View,
    name: String? = null,
    start: Long = 0L,
    end: Long = 500L,
    fps: Int = 30,
  ) {
    // Add one to the frame count so we get the last frame. Otherwise a 1 second, 60 FPS animation
    // our 60th frame will be at time 983 ms, and we want our last frame to be 1,000 ms. This gets
    // us 61 frames for a 1 second animation, 121 frames for a 2 second animation, etc.
    val durationMillis = (end - start).toInt()
    val frameCount = (durationMillis * fps) / 1000 + 1
    createFrameHandler(name, frameCount, fps).use { handler ->
      frameHandler = handler
      sdk.gif(view, start, end, fps)
    }
  }

  public fun unsafeUpdateConfig(
    deviceConfig: DeviceConfig? = null,
    theme: String? = null,
    renderingMode: RenderingMode? = null,
  ) {
    sdk.unsafeUpdateConfig(deviceConfig, theme, renderingMode)
  }

  private fun createFrameHandler(
    name: String? = null,
    frameCount: Int = 1,
    fps: Int = -1,
  ): SnapshotHandler.FrameHandler {
    val testNameValue = testName
      ?: throw IllegalStateException("Test name not initialized. Make sure the extension is registered properly.")
    val snapshot = Snapshot(name, testNameValue, Date())
    return snapshotHandler.newFrameHandler(snapshot, frameCount, fps)
  }

  private fun ExtensionContext.toTestName(): TestName {
    val fullQualifiedName = requiredTestClass.name
    val packageName = fullQualifiedName.substringBeforeLast('.', missingDelimiterValue = "")
    val className = fullQualifiedName.substringAfterLast('.')
    return TestName(packageName, className, displayName)
  }

  private companion object {
    private val isVerifying: Boolean = System.getProperty("paparazzi.test.verify")?.toBoolean() == true

    private fun determineHandler(maxPercentDifference: Double): SnapshotHandler = if (isVerifying) {
      SnapshotVerifier(maxPercentDifference)
    } else {
      HtmlReportWriter()
    }
  }
}
