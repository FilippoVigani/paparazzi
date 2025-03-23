package app.cash.paparazzi.junit.jupiter

import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.InvocationInterceptor
import org.junit.jupiter.api.extension.ReflectiveInvocationContext
import java.io.File
import java.lang.reflect.Method
import java.nio.file.Files
import java.util.ArrayList

class PaparazziTestExtension : BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback, InvocationInterceptor {

  private lateinit var tmpFolder: File
  private val reportDirKey = "paparazzi.snapshot.dir"
  private var oldReportDir: String? = null

  internal lateinit var paparazzi: PaparazziExtension

  override fun beforeAll(context: ExtensionContext) {
    tmpFolder = Files.createTempDirectory("paparazzi-test").toFile()
    tmpFolder.deleteOnExit()

    oldReportDir = System.getProperty(reportDirKey)
    val reportDir = File(tmpFolder, "reports").apply { mkdirs() }
    System.setProperty(reportDirKey, reportDir.path)

    paparazzi = PaparazziExtension()
  }

  override fun afterAll(context: ExtensionContext) {
    tmpFolder.deleteRecursively()

    if (oldReportDir == null) {
      System.clearProperty(reportDirKey)
    } else {
      System.setProperty(reportDirKey, oldReportDir!!)
    }
  }

  override fun beforeEach(context: ExtensionContext) {
    paparazzi.beforeEach(context)
  }

  override fun afterEach(context: ExtensionContext) {
    paparazzi.afterEach(context)
  }

  override fun interceptTestMethod(
    invocation: InvocationInterceptor.Invocation<Void>,
    invocationContext: ReflectiveInvocationContext<Method>,
    extensionContext: ExtensionContext
  ) {
    val errors: MutableList<Throwable> = ArrayList()

    try {
      invocation.proceed()
    } catch (t: Throwable) {
      errors.add(t)
    }

    if (errors.isNotEmpty()) {
      if (errors.size == 1) {
        throw errors[0]
      } else {
        val exception = RuntimeException("Multiple failures")
        errors.forEach { exception.addSuppressed(it) }
        throw exception
      }
    }
  }
}
