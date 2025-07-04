package net.imknown.android.bundletooldevicespecjsongenerator

import android.app.ActivityManager
import android.content.Context
import android.content.pm.FeatureInfo
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class MainViewModel : ViewModel() {

    companion object Companion {
        const val GL_ES_VERSION_2 = 2
    }

    private val json = Json { prettyPrint = true }

    private val _resultStateFlow = MutableStateFlow("Loading...")
    val resultStateFlow = _resultStateFlow.asStateFlow()

    suspend fun fetch(context: Context, glExtensions: List<String>) {
        val deviceSpecJson = withContext(Dispatchers.Default) {
            val supportedAbis = fetchSupportedAbis()
            val supportedLocales = fetchSupportedLocales()
            val deviceFeatures = fetchDeviceFeatures(context)
            val screenDensity = fetchScreenDensity(context)
            val sdkVersion = fetchSdkVersion()
            val sdkRuntime = fetchSdkRuntime()
            val ramBytes = fetchRamBytes(context)
            val buildBrand = fetchBuildBrand()
            val buildDevice = fetchBuildDevice()
            val socManufacturer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                fetchSocManufacturer()
            } else {
                null
            }
            val socModel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                fetchSocModel()
            } else {
                null
            }

            DeviceSpecJson(
                supportedAbis, supportedLocales, deviceFeatures,
                glExtensions, screenDensity, sdkVersion,
                sdkRuntime, ramBytes.toString(), buildBrand, buildDevice,
                socManufacturer, socModel
            )
        }

        _resultStateFlow.value =
            """
            |Device Spec Json:
            |${json.encodeToString(deviceSpecJson)}
            |
            |Context Configuration:
            |${context.resources.configuration}
            """.trimMargin()
    }

    // getprop ro.product.cpu.abilist
    private fun fetchSupportedAbis(): List<String> = Build.SUPPORTED_ABIS.toList()

    // dumpsys window | grep Configuration
    private fun fetchSupportedLocales(): List<String> = mutableListOf<String>().apply {
        val adjustedDefault = LocaleListCompat.getAdjustedDefault()
        for (i in 0 until adjustedDefault.size()) {
            val locale = adjustedDefault[i]
            locale?.let {
                add("${it.language}-${it.country}")
            }
        }
    }

    // pm list features
    private fun fetchDeviceFeatures(context: Context): List<String> = context.packageManager
        .systemAvailableFeatures
        .sortedBy(FeatureInfo::name)
        .map {
            when {
                it.name == null -> {
                    // val openGlEsVersion = it.glEsVersion.substringBefore(".").toInt()
                    "${it::reqGlEsVersion.name}=0x${Integer.toHexString(it.reqGlEsVersion)}"
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && it.version != 0 -> {
                    "${it.name}=${it.version}"
                }
                else -> it.name
            }
        }

    // wm density
    private fun fetchScreenDensity(context: Context): Int = context.resources.configuration.densityDpi

    // getprop ro.build.version.sdk
    private fun fetchSdkVersion(): Int = Build.VERSION.SDK_INT

    @Serializable
    class SdkRuntime(val supported: Boolean)

    // getprop | grep build.version.extensions
    private fun fetchSdkRuntime(): SdkRuntime {
        val supported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
        return SdkRuntime(supported)
    }

    private fun fetchRamBytes(context: Context): Long {
        val activityManager = context.getSystemService<ActivityManager>()
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memoryInfo)
        return memoryInfo.totalMem
    }

    private fun fetchBuildBrand(): String = Build.BRAND

    private fun fetchBuildDevice(): String = Build.DEVICE

    @RequiresApi(Build.VERSION_CODES.S)
    private fun fetchSocManufacturer(): String = Build.SOC_MANUFACTURER

    @RequiresApi(Build.VERSION_CODES.S)
    private fun fetchSocModel(): String = Build.SOC_MODEL
}