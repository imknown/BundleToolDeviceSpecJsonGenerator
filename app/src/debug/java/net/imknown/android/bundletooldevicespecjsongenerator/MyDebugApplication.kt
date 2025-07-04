package net.imknown.android.bundletooldevicespecjsongenerator

import android.os.StrictMode

class MyDebugApplication : MyApplication() {
    override fun onCreate() {
        super.onCreate()

        StrictMode.enableDefaults()
    }
}