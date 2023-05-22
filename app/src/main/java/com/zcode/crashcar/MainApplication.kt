package com.zcode.crashcar

import android.app.Application
import com.zcode.crashcar.utils.PrefsSetting

/*
 *    Created by Nono on 20/05/2023.
 */
class MainApplication: Application() {
    companion object {
        lateinit var prefsSetting: PrefsSetting
    }

    override fun onCreate() {
        super.onCreate()
        prefsSetting = PrefsSetting(applicationContext)
    }

    override fun onTerminate() {
        super.onTerminate()
        prefsSetting = PrefsSetting(applicationContext)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        prefsSetting = PrefsSetting(applicationContext)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        prefsSetting = PrefsSetting(applicationContext)
    }
}