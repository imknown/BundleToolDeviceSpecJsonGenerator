package net.imknown.android.bundletooldevicespecjsongenerator

import android.app.ActivityManager
import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.getSystemService
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()

        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val resultText by viewModel.resultStateFlow.collectAsStateWithLifecycle()
                    MainScreen(
                        resultText = resultText,
                        onExtensionsFetched = { glExtensions ->
                            viewModel.viewModelScope.launch {
                                viewModel.fetch(this@MainActivity, glExtensions)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

@Composable
fun MainScreen(
    resultText: String,
    onExtensionsFetched: (List<String>) -> Unit
) {
    var glExtensionsFetched by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (!glExtensionsFetched) {
            GlExtensionsFetcher(
                onExtensionsFetched = { extensions ->
                    onExtensionsFetched(extensions)
                    glExtensionsFetched = true
                }
            )
        }

        ResultDisplay(text = resultText)
    }
}

@Composable
fun GlExtensionsFetcher(
    modifier: Modifier = Modifier,
    onExtensionsFetched: (List<String>) -> Unit
) {
    var glSurfaceView by remember { mutableStateOf<GLSurfaceView?>(null) }

    LifecycleResumeEffect(glSurfaceView) {
        glSurfaceView?.onResume()
        onPauseOrDispose {
            glSurfaceView?.onPause()
        }
    }

    AndroidView(
        factory = { context ->
            GLSurfaceView(context).apply {
                setEGLContextClientVersion(fetchOpenGlEsVersionMajor(context))
                setRenderer(object : GLSurfaceView.Renderer {
                    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
                        val glExtensionsString = gl.glGetString(GL10.GL_EXTENSIONS)
                        val glExtensions = glExtensionsString.trim().split(" ")

                        onExtensionsFetched(glExtensions)
                    }

                    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {}

                    override fun onDrawFrame(gl: GL10) {}
                })
                glSurfaceView = this
            }
        },
        modifier = modifier,
        onRelease = {
            it.onPause()
            glSurfaceView = null
        }
    )
}

@Composable
fun ResultDisplay(text: String, modifier: Modifier = Modifier) {
    val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()
    SelectionContainer(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = text,
                modifier = Modifier
                    .padding(systemBarsPadding),
                fontSize = 12.sp
            )
        }
    }
}

private fun fetchOpenGlEsVersionMajor(context: Context): Int {
    val activityManager = context.getSystemService<ActivityManager>()
    val configInfo = activityManager?.deviceConfigurationInfo
    val glEsVersion = configInfo?.glEsVersion
    val major = glEsVersion?.substringBefore(".")
    return major?.toInt() ?: MainViewModel.GL_ES_VERSION_2
}