package net.imknown.android.bundletooldevicespecjsongenerator

import android.app.ActivityManager
import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import net.imknown.android.bundletooldevicespecjsongenerator.databinding.ActivityMainBinding
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private var glSurfaceView: GLSurfaceView? = null
    private val flContainer by lazy { binding.flContainer }
    private val tvResult by lazy { binding.tvResult }

    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()

        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(flContainer) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(
                left = insets.left,
                right = insets.right,
                top = insets.top,
                bottom = insets.bottom
            )

            windowInsets
        }

        lifecycleScope.launch {
            viewModel.resultStateFlow.flowWithLifecycle(lifecycle).collect {
                tvResult.text = it
            }
        }

        lifecycleScope.launch {
            val glExtensions = fetchGlExtensions()
            viewModel.fetch(this@MainActivity, glExtensions)
        }
    }

    // dumpsys SurfaceFlinger | grep 'SurfaceFlinger global state:' -A 4
    private suspend fun fetchGlExtensions(): List<String> = suspendCoroutine {
        val glSurfaceView = GLSurfaceView(this)
        flContainer.addView(glSurfaceView)
        this.glSurfaceView = glSurfaceView

        glSurfaceView.setEGLContextClientVersion(fetchOpenGlEsVersionMajor())
        glSurfaceView.setRenderer(object : GLSurfaceView.Renderer {
            override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
                runOnUiThread {
                    glSurfaceView.isVisible = false
                }
                val glExtensionsString = gl.glGetString(GL10.GL_EXTENSIONS)
                val glExtensions = glExtensionsString.trim().split(" ")
                it.resume(glExtensions)
            }

            override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
            }

            override fun onDrawFrame(gl: GL10) {
            }
        })
    }

    private fun fetchOpenGlEsVersionMajor(): Int {
        val activityManager = getSystemService<ActivityManager>()
        val configInfo = activityManager?.deviceConfigurationInfo
        val glEsVersion = configInfo?.glEsVersion
        val major = glEsVersion?.substringBefore(".")
        return major?.toInt() ?: MainViewModel.GL_ES_VERSION_2
    }

    override fun onResume() {
        super.onResume()
        glSurfaceView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        glSurfaceView?.onPause()
    }
}